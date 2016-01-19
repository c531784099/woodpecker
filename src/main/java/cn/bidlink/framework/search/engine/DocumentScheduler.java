package cn.bidlink.framework.search.engine;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.util.StringUtils;

import cn.bidlink.framework.search.Constants;
//import cn.bidlink.framework.search.engine.connection.FileConnectionPool;
import cn.bidlink.framework.util.holder.PropertiesHolder;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
//import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public abstract class DocumentScheduler extends AbstractScheduler {
	
	protected static final String SOLR_CONNECTION_GET_REGETNUM_KEY = "solr.connection.get.regetNum";
	
    public final Logger logger = Logger.getLogger(getClass());

    /**Solr cloud server*/
    protected CloudSolrServer solrServer;
    /**数据库信息，用于获取数据库连接*/
    protected DatabaseDialect databaseDialect;
    /**数据库表名*/
    protected String tableName;
    /**数据库表唯一字段，部分结果集按其排序*/
    protected DocumentField uniqueField;
    /**数据库查询语句*/
    protected String queryString;
    /**实现数据库ResultSet和SolrDocument数据转换*/
    protected DocumentMapper rowMapper;
    /**完成数目*/
    protected long completedCount = 0;
    /**默认分页大小*/
    protected int batchSize = 10000;
    /**关闭标识*/
    protected boolean shutdown;

    /**
     * !isManyThreads 代表单线程
     * */
	@Override
	public void execute() throws Exception {
		// check
		if (!StringUtils.hasLength(queryString) || rowMapper == null
				|| databaseDialect == null || solrServer == null
				|| uniqueField == null) {
			// throw new
			// GeneralException("queryString , FieldMapper, dataSource or solrServer is null");
		}

		// initialize
		completedCount = 0;
		int batchNum = 0;

		// fetch document
		Connection connection = null;
		//PreparedStatement statement = null;
		try {
			do {
				if (shutdown) {
					break;
				}
				batchNum++;
				connection = databaseDialect.getConnection();
				//statement = connection.createStatement();
				List<SolrInputDocument> data = fetchDocument(batchNum, connection);
				//statement.close();
				//System.out.print("statement close.");
				connection.commit();
				//connection.close();
				System.out.println("connection close.");
				//如果是单线程并且数据量为0说明该任务分页已经跑完了
				if (data == null || data.size() == 0) {
					break;
				}
				handleDocument(data);
				completedCount += data.size();
				if(!isManyThreads){
					logger.info("[" + name + "] success document:" + batchNum + "/" + completedCount);
				}
				//如果是单线程并且数据量小于分页数据量，说明是任务最后一次分页
				if (!isManyThreads && (data.size() < batchSize)) {
					break;
				}
			//只有单线程才进行循环，多线程按任务执行，不进行分页	
			}while (true && !isManyThreads);
		} catch(Exception e){
			e.printStackTrace();
			if(connection != null && !connection.isClosed()){
				connection.rollback();
			}
		} finally {
			//if (statement != null && !statement.isClosed()) {
			//	statement.close();
			//}
			if (connection != null && !connection.isClosed()) {
				connection.close();
			}
			if(solrServer != null){
				solrServer.getLbServer().getHttpClient().getConnectionManager().closeExpiredConnections();
				if(!isMaintainThread()){
					solrServer.getLbServer().getHttpClient().getConnectionManager().shutdown();
					solrServer.shutdown();
				}
			}
		}
	}

	protected List<SolrInputDocument> fetchDocument(int batchNum, Connection conn) throws Exception {
		List<SolrInputDocument> documents = new ArrayList<SolrInputDocument>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			String batchQueryString = null;
			//如果使用多线程，直接只用数据库任务表存储的SQL就行，不用再分页了
			if(isManyThreads){
				batchQueryString = queryString;
			}else{
				batchQueryString = createBatchQueryString(queryString, batchNum);
			}
			statement = conn.prepareStatement(batchQueryString);
			//jamonapi 数据库查询
			Monitor batchQueryMonitor = null;
			if(PropertiesHolder.getBoolean(Constants.JAMON_GATE_ISOPEN, false)){
				batchQueryMonitor = MonitorFactory.start("BatchQueryDocumentFromDB-"+getClass());
			}
			resultSet = statement.executeQuery();
			if(batchQueryMonitor != null){
				batchQueryMonitor.stop();
			}
			//jamonapi 生成document
			Monitor createDocumentMonitor = null;
			if(PropertiesHolder.getBoolean(Constants.JAMON_GATE_ISOPEN, false)){
				createDocumentMonitor = MonitorFactory.start("CreateDocumentByResult-"+getClass());
			}
			int rowNum = -1;
			while (resultSet.next()) {
				SolrInputDocument document = rowMapper.map(resultSet, ++rowNum);
				if (document != null) {
					documents.add(document);
					//将查到的ID存储在文件中，用于对比是否有建立不成功的Document
					//FileConnectionPool.write(PropertiesHolder.getProperty(CheckCreateDocumentIdScheduler.CHECKDOCUMENTWATCHER_PROPERTIES_FILEPATH_KEY, "")+indexName, ""+document.getFieldValue("id")+"\r\n");
				}
			}
			resultSet.close();
			statement.close();
			if(createDocumentMonitor != null){
				createDocumentMonitor.stop();
			}
		} finally {
			if (resultSet != null && !resultSet.isClosed()) {
				resultSet.close();
			}
			if(statement != null && !statement.isClosed()) {
				statement.close();
			}
		}
		return documents;
	}

	protected abstract void handleDocument(List<SolrInputDocument> data) throws Exception;

	@Override
	public void shutdown() {
		shutdown = true;
	}

	protected String createBatchQueryString(String queryString, int batchNum) {
		String s = null;
		if (batchNum <= 1) {
			s = databaseDialect.getLimitString(tableName+"."+uniqueField.getName(), "", queryString, 0, batchSize);
		} else {
			s = databaseDialect.getLimitString(tableName+"."+uniqueField.getName(), "", queryString, (batchNum - 1) * batchSize, batchSize);
		}
		return s;
	}

	@Override
	public long getCompletedCount() {
		return completedCount;
	}

    public void setRowMapper(DocumentMapper rowMapper) {
        this.rowMapper = rowMapper;
    }
    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
    public void setSolrServer(CloudSolrServer solrServer) {
        this.solrServer = solrServer;
    }
    public void setDatabaseDialect(DatabaseDialect databaseDialect) {
        this.databaseDialect = databaseDialect;
    }
    public void setUniqueField(DocumentField uniqueField) {
        this.uniqueField = uniqueField;
    }
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public static void main(String[] args) {
        /**
		ClouldHttpSolrServer solrServer = null;
		try {
			solrServer = new CommonsHttpSolrServer(
					"http://211.151.182.231:6086/spproduct");
			// solrServer = new
			// CommonsHttpSolrServer("http://211.151.182.231:5083/bulletin");
			// solrServer = new
			// CommonsHttpSolrServer("http://211.151.182.231:6085/spcompany");
			// 856148
			List<String> ids = new ArrayList<String>();
			int[] idArray = new int[] { 190592312 };
			for (int id : idArray) {
				String idStr = String.valueOf(id);
				ids.add(idStr);
			}
			solrServer.deleteById(ids);
			solrServer.commit();
		} catch (MalformedURLException e) {
			// throw new GeneralException("create solrServer error", e);
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         */
	}


}
