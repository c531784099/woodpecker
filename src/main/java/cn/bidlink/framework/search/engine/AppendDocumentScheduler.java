package cn.bidlink.framework.search.engine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import cn.bidlink.framework.search.Constants;
import cn.bidlink.framework.search.engine.fitter.score.SolrDocumentCompanyScoreFitter;
import cn.bidlink.framework.search.util.DataSourceUtils;
import cn.bidlink.framework.util.holder.PropertiesHolder;
import cn.bidlink.search.solr.fitter.SolrDocumentFitter;
import cn.bidlink.search.solr.fitter.score.SolrDocumentScoreFitter;

public class AppendDocumentScheduler extends DocumentScheduler {
	
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
             //   System.out.println("query stringproduct ************************2013/6/13"+(String)key1+":"+(String)zone+":"+(String)key);
				Monitor batchQueryMonitor = MonitorFactory.start("BatchQuery-spProduct-FromOracle-"+getClass());
				ResultSet resultSet = statement.executeQuery();
				ResultSet creditResultSet = creditStatement.executeQuery();
				if(batchQueryMonitor != null){
					batchQueryMonitor.stop();
				}
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
		} catch(Exception e){
			connection.rollback();
			creditConnection.rollback();
			e.printStackTrace();
			throw e;
		} finally {
			if (connection != null){
				connection.close();
			}
			if (creditConnection != null){
				creditConnection.close();
			}
		}
	}

	protected void spCompany(List<SolrInputDocument> data) throws Exception {
		SolrDocumentFitter solrDocumentScoreFitter = null;
		if(PropertiesHolder.getLong("index.score.weightDefault", 0L) == 0L){
			solrDocumentScoreFitter = new SolrDocumentCompanyScoreFitter();
		}
		MysqlDatabaseDialect dialect = DataSourceUtils.createMysqlDatabaseDialect();
		MysqlDatabaseDialect creditDialect = DataSourceUtils.createCreditMysqlDatabaseDialect();
		Connection connection = null;
		Connection creditConnection = null;
		try {
			connection = dialect.getConnection();
			creditConnection = creditDialect.getConnection();
			String sql = "SELECT " +
					" si.state spacestate,si.filter_state noisestate,si.create_date createDate,si.logo_pic logo"+
					" FROM SPACE_INFO si" +
					" where si.company_id=?";
			
			String dialectSql = "SELECT cs.SCORE,cs.RATING FROM CREDIT_SCORE cs WHERE cs.COMPANY_ID=?";
			PreparedStatement statement = connection.prepareStatement(sql);
			PreparedStatement creditStatement = creditConnection.prepareStatement(dialectSql);
			for (SolrInputDocument document : data) {
				Object key = document.getFieldValue("key");
				if(key != null){
					document.removeField("key");
				}
				statement.setObject(1, key);
				creditStatement.setObject(1, key);
				Monitor batchQueryMonitor = MonitorFactory.start("BatchQuery-spCompany-FromMySQL-"+getClass());
				ResultSet resultSet = statement.executeQuery();
				ResultSet creditResultSet = creditStatement.executeQuery();
				if(batchQueryMonitor != null){
					batchQueryMonitor.stop();
				}
				if(creditResultSet.next()){
					document.setField("weight_score_d", creditResultSet.getLong(1));
					document.setField("rating_i", creditResultSet.getLong(2));
				}
				
				if(resultSet != null && resultSet.first()){
					rowMapper.map(document, resultSet, "other");
				}else{
					String sqlNull ="SELECT 0 spacestate,1 noisestate,null createDate,null logo";
					PreparedStatement statementNull = connection.prepareStatement(sqlNull);
					ResultSet resultSetNull = statementNull.executeQuery();
					while (resultSetNull != null && resultSetNull.next()) {
						rowMapper.map(document, resultSetNull, "other");
					}
				}
				
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
			}
			connection.commit();
			creditConnection.commit();
		} catch (Exception e) {
			connection.rollback();
			creditConnection.rollback();
			e.printStackTrace();
			throw e;
		} finally {
			if (connection != null){
				connection.close();
			}
			if(creditConnection != null){
				creditConnection.close();
			}
			solrDocumentScoreFitter = null;
		}
	}
	
	protected void otherDocument(List<SolrInputDocument> data) throws Exception {
        if (queryString.indexOf("SPACE_PRODUCT")!=-1) {
			this.spProduct(data);
		}else if (queryString.indexOf("t_reg_company")!=-1){
			this.spCompany(data);
		}
	}

	@Override
	protected void handleDocument(List<SolrInputDocument> data) throws Exception {
		boolean isWrong = true;
		this.otherDocument(data);
		List<String> ids = getIds(data);
		while(isWrong){
			try {
				//先删后添加避免重复数据
				solrServer.deleteById(ids);
				//jamonapi 添加document
				Monitor addDocumentMonitor = null;
				if(PropertiesHolder.getBoolean(Constants.JAMON_GATE_ISOPEN, false)){
					addDocumentMonitor = MonitorFactory.start("AddDocumentFromSolr-"+getClass());
				}
				solrServer.add(data);
				solrServer.commit();
				if(addDocumentMonitor != null){
					addDocumentMonitor.stop();
				}
				isWrong = false;
			}catch (Exception e) {
				try {
					Thread.sleep(5*60*1000);
                    logger.info(e.getMessage());
					logger.error("访问Solr服务器异常，当前线程等待5m");
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
		}
	}
	
	private List<String> getIds(List<SolrInputDocument> data) {
		List<String> ids = new ArrayList<String>();
		for(SolrInputDocument document : data) {
			ids.add(document.getFieldValue(uniqueField.getName()).toString());
		}
		return ids;
	}
}
