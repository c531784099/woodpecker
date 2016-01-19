package cn.bidlink.framework.search.engine;

import cn.bidlink.framework.search.Constants;
import cn.bidlink.framework.search.engine.fitter.score.SolrDocumentCompanyScoreFitter;
import cn.bidlink.framework.search.util.DataSourceUtils;
import cn.bidlink.framework.search.util.ValidatorIndexUtils;
import cn.bidlink.framework.util.CollectionUtils;
import cn.bidlink.framework.util.holder.PropertiesHolder;
import cn.bidlink.search.solr.fitter.SolrDocumentFitter;
import cn.bidlink.search.solr.fitter.score.SolrDocumentScoreFitter;

import org.apache.solr.common.SolrInputDocument;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MaintainDocumentScheduler extends DocumentScheduler {

    private String queueTableName;

    private int intervalOfCheck;

    private long completedCount;

    private ScheduledExecutorService scheduledExecutorService;

    private void superExecute() throws Exception {
        super.execute();
    }

	@Override
	public void execute() throws Exception {
		scheduledExecutorService = Executors.newScheduledThreadPool(1);
		scheduledExecutorService.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				try {
					superExecute();
				} catch (Exception e) {
					logger.error("maintain scheduler [" + getName() + "] error", e);
					exception = e;
				}
			}
		}, 0, intervalOfCheck, TimeUnit.MILLISECONDS);
	}
     //demo
	/**/
	protected void spProduct(List<SolrInputDocument> data) throws Exception {
		OracleDatabaseDialect dialect = DataSourceUtils.createOracleDatabaseDialect();
		MysqlDatabaseDialect creditDialect = DataSourceUtils.createCreditMysqlDatabaseDialect();
		Connection connection = null;
		Connection creditConnection = null;
		try {
			connection = dialect.getConnection();
			creditConnection = creditDialect.getConnection();
			//connection.setAutoCommit(false);
			
			String sql = "SELECT CC.STATE companystate,RWA.STATUS matestate," +
					"CASE WHEN RC.BIDAUTH_STATUS=0 THEN 1 WHEN RC.BIDAUTH_STATUS=1 THEN 3 WHEN RC.BIDAUTH_STATUS=0 THEN 1 ELSE 0 END authStatus," +
					"RC.AUTHEN_NUMBER authenNumber,SC.ENTERPRISE_TRADE_NUMBER eNumber,nvl(SC.ENTERPRISE_TRADE_AMOUNT,0) ePrice," +
					"nvl(SC.BID_TRAD_NUMBER,0) bNumber,nvl(SC.BID_TRADE_AMOUNT,0) bPrice," +
					"nvl(sc.enterprise_trade_number,0)+nvl(sc.bid_trad_number,0) tradeNumber," +
					"nvl(sc.enterprise_trade_amount,0)+nvl(sc.bid_trade_amount,0) tradePrice," +
					"crr.name zoneName,T_UIC_COMPANY_STATUS.CREDIT_MEDAL_STATUS core_i" +
					" FROM T_REG_COMPANY RC LEFT JOIN T_REG_WEB_ACTIVE RWA ON RWA.USER_ID=?" +
					" LEFT JOIN EPSIII.CORP_COMPANYS CC ON RC.ID=CC.ID" +
					" LEFT JOIN T_STATISTIC_CREDIT SC ON SC.COMPANY_ID=RC.ID" +
					" LEFT JOIN CORP_RESOURCE_REGIONS crr on crr.id=?" +
					" LEFT JOIN T_UIC_COMPANY_STATUS ON RC.ID=T_UIC_COMPANY_STATUS.COMP_ID" +
					" where rc.id=?";

			String dialectSql = "SELECT cs.SCORE,cs.RATING FROM CREDIT_SCORE cs WHERE cs.COMPANY_ID=?";
			
			PreparedStatement statement = connection.prepareStatement(sql);
			PreparedStatement creditStatement = creditConnection.prepareStatement(dialectSql);
			for (SolrInputDocument document : data) {
				Object key = document.getFieldValue("key");
				if(key != null){
					document.removeField("key");
				}
				Object zone = document.getFieldValue("zone");
				Object key1 = document.getFieldValue("key1");
				if(key1 != null){
					document.removeField("key1");
				}
				statement.setString(1, (String)key1);
				statement.setString(2, (String)zone);
				statement.setString(3, (String)key);
				creditStatement.setObject(1, key);
				ResultSet resultSet = statement.executeQuery();
				ResultSet creditResultSet = creditStatement.executeQuery();
				if(creditResultSet.next()){
					document.setField("weight_score_d", creditResultSet.getLong(1));
					document.setField("rating_i", creditResultSet.getLong(2));
				}
				while (resultSet != null && resultSet.next()) {
					rowMapper.map(document, resultSet, "other");
				}
			}
			connection.commit();
			creditConnection.commit();
		} catch (Exception e) {
			connection.rollback();
			creditConnection.rollback();
			throw e;
		} finally {
			if (connection != null && !connection.isClosed()){
				connection.close();
			}
			if (creditConnection != null){
				creditConnection.close();
			}
		}
	}
	
	//与前台的sql做关联
	protected void spCompany(List<SolrInputDocument> data) throws Exception {
		SolrDocumentFitter solrDocumentScoreFitter = null;
		if(PropertiesHolder.getLong("index.score.weightDefault", 0L) == 0L){
			solrDocumentScoreFitter = new SolrDocumentCompanyScoreFitter();
		}
		MysqlDatabaseDialect mysqlDia = DataSourceUtils.createMysqlDatabaseDialect();
		Connection mysqlconnection = mysqlDia.getConnection();
		
		MysqlDatabaseDialect dialect = DataSourceUtils.createMysqlDatabaseDialect();
		MysqlDatabaseDialect creditDialect = DataSourceUtils.createCreditMysqlDatabaseDialect();
		
		Connection connection = null;
		Connection creditConnection = null;
		try {
			connection = dialect.getConnection();
			creditConnection = creditDialect.getConnection();
			//connection.setAutoCommit(true);
			
			String sql = "SELECT " +
					" si.state spacestate,si.filter_state noisestate,si.create_date createDate,si.logo_pic logo"+
					" FROM SPACE_INFO si" +
					" where si.company_id=?";
			String dialectSql = "SELECT cs.SCORE,cs.RATING FROM CREDIT_SCORE cs WHERE cs.COMPANY_ID=?";
			
			PreparedStatement statement = connection.prepareStatement(sql);
			PreparedStatement creditStatement = creditConnection.prepareStatement(dialectSql);
			
			String insertsql = "INSERT INTO INDEXER_QUEUE_Q_PRODUCT_CLONE(business_id,operation_type,createtime) VALUES(?,?,now())";
			String selectsql = "select id from SPACE_PRODUCT where companyid=? limit 0,1000";
			
			for (SolrInputDocument document : data) {
				Object key = document.getFieldValue("key");
				if(key != null){
					document.removeField("key");
				}
				statement.setObject(1, key);
				creditStatement.setObject(1, key);
				
				ResultSet resultSet = statement.executeQuery();
				ResultSet creditResultSet = creditStatement.executeQuery();
				if(creditResultSet.next()){
					document.setField("weight_score_d", creditResultSet.getLong(1));
					document.setField("rating_i", creditResultSet.getLong(2));
				}
				if(resultSet != null && resultSet.next()){
					rowMapper.map(document, resultSet, "other");
				}else {
					String sqlNull ="SELECT 0 spacestate,1 noisestate,null createDate,null logo";
					PreparedStatement statementNull = connection.prepareStatement(sqlNull);
					ResultSet resultSetNull = statementNull.executeQuery();
					while (resultSetNull != null && resultSetNull.next()) {
						rowMapper.map(document, resultSetNull, "other");
					}
				}
				
				resultSet.close();
				creditResultSet.close();
				Monitor fieMonitor = null;
				if(PropertiesHolder.getBoolean(Constants.JAMON_GATE_ISOPEN, false)){
					fieMonitor = MonitorFactory.start("fitSolrDocument-"+getClass());
				}
				if(solrDocumentScoreFitter != null){
					solrDocumentScoreFitter.fit(document);
				}else{
					document.setField(SolrDocumentScoreFitter.SOLRDOCUMENT_FIELDNAME_SCORE, PropertiesHolder.getLong("index.score.weightDefault", 0L));
				}
				if(fieMonitor != null){
					fieMonitor.stop();
				}
				PreparedStatement mysqlSelectPS = mysqlconnection.prepareStatement(selectsql);
				PreparedStatement mysqlInsertPS = mysqlconnection.prepareStatement(insertsql);
				mysqlSelectPS.setObject(1, key);
				ResultSet mysqlSelectRS = mysqlSelectPS.executeQuery();
				while(mysqlSelectRS.next()){
					Long newid = mysqlSelectRS.getLong("id");
					mysqlInsertPS.setLong(1, newid);
					mysqlInsertPS.setInt(2, 2);
					mysqlInsertPS.addBatch();
				}
				mysqlSelectRS.close();
				mysqlInsertPS.executeBatch();
				mysqlInsertPS.close();
			}
			statement.close();
			creditStatement.close();
			connection.commit();
			creditConnection.commit();
			mysqlconnection.commit();
			//this.productQueue();
		} catch (Exception e) {
			connection.rollback();
			creditConnection.rollback();
			mysqlconnection.rollback();
			e.printStackTrace();
			//throw e;
		} finally {
			if (connection != null){
				connection.close();
			}
			if(creditConnection != null){
				creditConnection.close();
			}
			if(mysqlconnection != null){
				mysqlconnection.close();
			}
			solrDocumentScoreFitter = null;
		}
	}
	
	//黑词过滤
	protected void spCompanySpaceInfo(List<SolrInputDocument> data) throws SQLException{
		OracleDatabaseDialect orcaleDia = DataSourceUtils.createOracleDatabaseDialect();
		Connection orcaleconnection = orcaleDia.getConnection();
		try {
			if(orcaleconnection != null){
				String insertsql = "INSERT INTO INDEXER_QUEUE_Q_COMPANY_CLONE(queue_id,business_id,operation_type) VALUES(unireg.SEQ_INDEXER_QUEUE_Q_COMPANY.NEXTVAL,?,?)";
				PreparedStatement insertstatement = orcaleconnection.prepareStatement(insertsql);
				if(insertstatement != null){
					for(SolrInputDocument document : data){
						QueuedSolrInputDocument queueSolrInputDocument = (QueuedSolrInputDocument)document;
						insertstatement.setLong(1, Long.parseLong(queueSolrInputDocument.getBusinessId()));
						insertstatement.setInt(2, queueSolrInputDocument.getOperationType().ordinal());
						insertstatement.addBatch();
					}
					insertstatement.executeBatch();
					insertstatement.close();
					orcaleconnection.commit();
				}
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
			orcaleconnection.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
			orcaleconnection.rollback();
		} finally{
			if(orcaleconnection != null){
				orcaleconnection.close();
			}
		}
	}
	
	protected void productQueue() throws Exception {
		OracleDatabaseDialect orcaleDia = DataSourceUtils.createOracleDatabaseDialect();
		MysqlDatabaseDialect mysqlDia = DataSourceUtils.createMysqlDatabaseDialect();
		Connection orcaleconnection = null;
		Connection mysqlconnection = null;
		try {
			orcaleconnection = orcaleDia.getConnection();
			mysqlconnection = mysqlDia.getConnection();
			
			String orasql = "SELECT queue_id,business_id,operation_type FROM INDEXER_QUEUE_MYSQL_COM_CLONE";
			orasql = orcaleDia.getLimitString("queue_id", "desc", orasql, 0, 1);
			String insertsql = "INSERT INTO INDEXER_QUEUE_Q_PRODUCT_CLONE(business_id,operation_type,createtime) VALUES(?,?,now())";
			String selectsql = "select id from SPACE_PRODUCT where companyid=? limit 0,1000";
			String delsql = "delete from INDEXER_QUEUE_MYSQL_COM_CLONE where queue_id=?";
			
			PreparedStatement orastatement = orcaleconnection.prepareStatement(orasql);
			ResultSet resultSet = orastatement.executeQuery();
			if( resultSet != null && resultSet.next() ){
				Long queue_id = resultSet.getLong("queue_id");
				Long business_id = resultSet.getLong("business_id");
				Integer operation_type = resultSet.getInt("operation_type");
				
				PreparedStatement selectstatement = mysqlconnection.prepareStatement(selectsql);
				selectstatement.setLong(1,business_id);
				ResultSet mysqlresultSet = selectstatement.executeQuery();
				if( mysqlresultSet != null ){
					PreparedStatement insertstatement = mysqlconnection.prepareStatement(insertsql);
					while( mysqlresultSet.next() ) {
						Long newid = mysqlresultSet.getLong("id");
						insertstatement.setLong(1, newid);
						insertstatement.setInt(2, operation_type);
						insertstatement.addBatch();
					}
					mysqlresultSet.close();
					insertstatement.executeBatch();
					insertstatement.close();
				}
				
				PreparedStatement delstatement = orcaleconnection.prepareStatement(delsql);
				delstatement.setLong(1, queue_id);
				delstatement.execute();
				delstatement.close();
			}
			//resultSet.close();
			//orastatement.close();
			orcaleconnection.commit();
			mysqlconnection.commit();
		} catch (Exception e) {
			orcaleconnection.rollback();
			mysqlconnection.rollback();
			e.printStackTrace();
			throw e;
		} finally {
			if (orcaleconnection != null){
				orcaleconnection.close();
			}
			if (mysqlconnection != null){
				mysqlconnection.close();
			}
		}
	}

	protected void otherDocument(List<SolrInputDocument> data) throws Exception {
        assert queueTableName != null;
        if(queueTableName.contains("PRODUCT")){
            this.spProduct(data);
        }else if (queueTableName.contains("COMPANY") && !queueTableName.equals(Constants.QUEUE_SP_COMPANY_MYSQL_TABLE_NAME) && !queueTableName.equals(Constants.QUEUE_SP_COMPANY_MYSQL_TABLE_NAME_TEST)){
            this.spCompany(data);
        }else if(queueTableName.equals(Constants.QUEUE_SP_COMPANY_MYSQL_TABLE_NAME) || queueTableName.equals(Constants.QUEUE_SP_COMPANY_MYSQL_TABLE_NAME_TEST)){
        	this.spCompanySpaceInfo(data);
        }
	}

    @Override
    public void handleDocument(List<SolrInputDocument> data) throws Exception {
        // 使用新的连接
    	Connection connection = databaseDialect.getConnection();
    	try {
    		this.otherDocument(data);
        	// 提取数据
        	DocumentPack pack = extractDocumentPack(data);
    		if(!Constants.QUEUE_SP_COMPANY_MYSQL_TABLE_NAME.equals(this.queueTableName.toLowerCase()) && !Constants.QUEUE_SP_COMPANY_MYSQL_TABLE_NAME_TEST.equals(this.queueTableName.toLowerCase())){
    			// 处理文档
    			solrServer.deleteById(pack.ids);
    			Collection<SolrInputDocument> collection = ValidatorIndexUtils.validateDocument(pack.documents);
    			if (!CollectionUtils.isEmpty(collection)) {
    				solrServer.add(collection);
    			}
    		}
			//String deleteSql = "delete from " + queueTableName + " where queue_id >= ? and queue_id <= ?";
			String updateSql = "update " + queueTableName + " set updatetime="+this.databaseDialect.getCurrentTime()+" where queue_id >= ? and queue_id <= ?";
			PreparedStatement statement = connection.prepareStatement(updateSql);
			statement.setLong(1, pack.minQueueId);
			statement.setLong(2, pack.maxQueueId);
			statement.execute();
			// 分别提交
			statement.close();
			if(solrServer != null){
				solrServer.commit();
			}
			connection.commit();
			this.completedCount += data.size();
		} catch (Exception e) {
			e.printStackTrace();
			if (connection != null) {
				connection.rollback();
			}
			if (solrServer != null) {
				solrServer.rollback();
			}
			throw e;
		} finally {
			if (connection != null && !connection.isClosed()) {
				connection.close();
			}
		}
	}

	private class DocumentPack {
		private long minQueueId = -1;
		private long maxQueueId = -1;
		private List<String> ids = new ArrayList<String>();
		private List<SolrInputDocument> documents = new ArrayList<SolrInputDocument>();
	}

	private DocumentPack extractDocumentPack(List<SolrInputDocument> data) {
		DocumentPack pack = new DocumentPack();
		Map<String, String> existDocument = new HashMap<String, String>();
		for (int i = data.size() - 1; i >= 0; i--) {
			QueuedSolrInputDocument document = (QueuedSolrInputDocument) data.get(i);
			pack.minQueueId = pack.minQueueId == -1 ? document.getQueueId() : Math.min(pack.minQueueId, document.getQueueId());
			pack.maxQueueId = Math.max(pack.maxQueueId, document.getQueueId());
			pack.ids.add(document.getBusinessId());
			if ((document.getOperationType().equals(OperationType.ADD) || document.getOperationType().equals(OperationType.UPDATE))
					&& existDocument.get(document.getBusinessId()) == null) {
				pack.documents.add(document);
				existDocument.put(document.getBusinessId(), document.getBusinessId());
			}
		}
		return pack;
	}

	@Override
	protected String createBatchQueryString(String queryString, int batchNum) {
		StringBuffer sql = new StringBuffer();
		if (queueTableName!=null && queueTableName.contains("PRODUCT")){
            queryString = queryString.replaceFirst("select", "select q.*,");
            sql.append(queryString);
            sql.append(" right join "+queueTableName+" q");
            sql.append(" on q."+ QueuedSolrInputDocumentMapper.COLUMN_NAME_BUSINESS_ID +"=SPACE_PRODUCT."+ uniqueField.getColumnNames());
            sql.append(" where q.UPDATETIME is null");
            sql.append(" order by q.queue_id limit 0,");
            sql.append(PropertiesHolder.getInt("maintainTask.product.exeNum", 5000));
                                           /**
//
		//	queryString=queryString + " where  sp."+uniqueField.getColumnNames()+"=(select "+QueuedSolrInputDocumentMapper.COLUMN_NAME_BUSINESS_ID+" from "+queueTableName+" limit 0,1)";
			sql.append(" select * from " + queueTableName + " q left outer join (" + queryString + ") t ");
			sql.append(" on q." + QueuedSolrInputDocumentMapper.COLUMN_NAME_BUSINESS_ID + " = t." + uniqueField.getColumnNames());
			sql.append(" where q.updatetime is null");
			sql.append(" order by q." + QueuedSolrInputDocumentMapper.COLUMN_NAME_QUEUE_ID + " asc");
			sql.append(" limit 0,");
			sql.append(PropertiesHolder.getInt("maintainTask.product.exeNum", 500));
                                            */
			//queryString = sql.toString();
         //   System.out.println(queryString);
//			queryString = "SELECT iqs.QUEUE_ID QUEUE_ID,iqs.BUSINESS_ID BUSINESS_ID,iqs.OPERATION_TYPE OPERATION_TYPE,"+queryString.substring(6);
//			return super.createBatchQueryString(queryString, 1);
			return sql.toString();

		}
		else if("INDEXER_QUEUE_COMPANY_CM_CLONE".equals(queueTableName))
		{
			sql.append(" select * from epsiii." +queueTableName + " q left outer join (" + queryString + ") t ");
			sql.append(" on q." + QueuedSolrInputDocumentMapper.COLUMN_NAME_BUSINESS_ID + " = t." + uniqueField.getColumnNames());
			sql.append(" where q.UPDATETIME is null");
			sql.append(" order by q." + QueuedSolrInputDocumentMapper.COLUMN_NAME_QUEUE_ID + " asc");
		}
		else if(Constants.QUEUE_SP_COMPANY_MYSQL_TABLE_NAME.equals(queueTableName.toLowerCase()) || Constants.QUEUE_SP_COMPANY_MYSQL_TABLE_NAME_TEST.equals(queueTableName.toLowerCase())){
			sql.append(" select * from " + queueTableName + " q ");
			sql.append(" where q.UPDATETIME is null");
			sql.append(" order by q." + QueuedSolrInputDocumentMapper.COLUMN_NAME_QUEUE_ID + " asc");
		}
		else
		{
			sql.append(" select * from " + queueTableName + " q left outer join (" + queryString + ") t ");
			sql.append(" on q." + QueuedSolrInputDocumentMapper.COLUMN_NAME_BUSINESS_ID + " = t." + uniqueField.getColumnNames());
			sql.append(" where q.UPDATETIME is null");
			sql.append(" order by q." + QueuedSolrInputDocumentMapper.COLUMN_NAME_QUEUE_ID + " asc");
		}
		//System.out.println(sql.toString());
		//@Refact此处先做成与父类不相同方式
		//return super.createBatchQueryString(sql.toString(), 1);
		String s = sql.toString();

		if (batchNum <= 1) {
			s = databaseDialect.getLimitString(null, "", s, 0, batchSize);
		} else {
			s = databaseDialect.getLimitString(null, "", s, (batchNum - 1) * batchSize, batchSize);
		}
      //  System.out.println(s);
		return s;
	}
	
	public String getQueueTableName() {
		return queueTableName;
	}

	public void setQueueTableName(String queueTableName) {
		this.queueTableName = queueTableName;
	}

	@Override
	public boolean isTerminated() {
		return super.isTerminated() && scheduledExecutorService != null
				&& scheduledExecutorService.isTerminated();
	}

	@Override
	public long getTerminatedTime() {
		return 0;
	}

	@Override
	public boolean isCompleted() {
		return isTerminated();
	}

	@Override
	public long getCompletedTime() {
		return 0;
	}

	@Override
	public long getCompletedCount() {
		return this.completedCount;
	}

	@Override
	public void shutdown() {
		super.shutdown();
		if (scheduledExecutorService != null) {
			scheduledExecutorService.shutdownNow();
		}
	}

	public int getIntervalOfCheck() {
		return intervalOfCheck;
	}

	public void setIntervalOfCheck(int intervalOfCheck) {
		this.intervalOfCheck = intervalOfCheck;
	}

}