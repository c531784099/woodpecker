package cn.bidlink.framework.search.engine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

import cn.bidlink.framework.search.engine.connection.FileConnectionPool;
import cn.bidlink.framework.util.holder.PropertiesHolder;

public class CheckCreateDocumentIdScheduler extends AbstractScheduler{
	
	public static final String CHECKDOCUMENTWATCHER_PROPERTIES_ISEXECUTE_KEY = "watcher.checkDocumentTask.isExecute";
	public static final String CHECKDOCUMENTWATCHER_PROPERTIES_FILEPATH_KEY = "watcher.checkDocumentTask.filePath";
	
	public static final Integer DEFAULT_SENDTOSOLRNUMONCE = 100;
	
	private DatabaseDialect databaseDialect;
	private CloudSolrServer solrServer;
	
	private final String filePath;
	private Integer sendToSolrNumOnce;
	
	public CheckCreateDocumentIdScheduler(){
		filePath = PropertiesHolder.getProperty(CHECKDOCUMENTWATCHER_PROPERTIES_FILEPATH_KEY, "");
		sendToSolrNumOnce = DEFAULT_SENDTOSOLRNUMONCE;
	}
	
	public DatabaseDialect getDatabaseDialect() {
		return databaseDialect;
	}
	public void setDatabaseDialect(DatabaseDialect databaseDialect) {
		this.databaseDialect = databaseDialect;
	}
	public CloudSolrServer getSolrServer() {
		return solrServer;
	}
	public void setSolrServer(CloudSolrServer solrServer) {
		this.solrServer = solrServer;
	}

	@Override
	public long getCompletedCount() {
		return 0;
	}

	@Override
	public void shutdown() {
		
	}

	public boolean isExecute() {
		return PropertiesHolder.getBoolean(CHECKDOCUMENTWATCHER_PROPERTIES_ISEXECUTE_KEY, false);
	}
	
	@Override
	public void execute() throws Exception {
		if(this.isExecute() && solrServer != null){
			BufferedReader breader = FileConnectionPool.getFileConnection(filePath+indexName).getBreader();;
			BufferedWriter errorBwriter = FileConnectionPool.getFileConnection(filePath+indexName+"_error").getBwriter();;
			String documentId = null;
			StringBuilder param = new StringBuilder();
			int i = 1;
			while((documentId = breader.readLine()) != null){
				param.append("id:"+documentId);
				if(i % sendToSolrNumOnce == 0){
					checkCreateDocumentId(param.toString(), sendToSolrNumOnce, errorBwriter);
					param = new StringBuilder();
				}else{
					param.append(" OR ");
				}
				i++;
			}
			if(param.length() != 0 && i % sendToSolrNumOnce != 0){
				param.delete(param.lastIndexOf(" OR "), param.length());
				checkCreateDocumentId(param.toString(), i % sendToSolrNumOnce, errorBwriter);
				param = new StringBuilder();
			}
			FileConnectionPool.closeFileConnection(filePath+indexName);
			FileConnectionPool.closeFileConnection(filePath+indexName+"_error");
		}
	}

	//递归折半查找
	private void checkCreateDocumentId(String queryString, int checkNum, BufferedWriter errorBwriter){
		if(checkNum == 0){
			return;
		}
		SolrQuery query = new SolrQuery(queryString);
		QueryResponse resp = null;
		try {
			resp = solrServer.query(query);
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		if(resp != null){
			SolrDocumentList list = resp.getResults();
			if(list != null){
				if(list.getNumFound() != checkNum){
					//如果一个都没查到，就把查询的documentId放入error文件
					if(list.getNumFound() == 0){
						try {
							if(checkNum == 1){
								errorBwriter.write(queryString.split(":")[1]+"\r\n");
							}else{
								String[] documentIds = queryString.split(" OR ");
								for(String documentId : documentIds){
									errorBwriter.write(documentId.split(":")[1]+"\r\n");
								}
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					/*
					if(checkNum == 1 && list.size() == 0){
						try {
							errorBwriter.write(queryString.split(":")[1]+"\r\n");
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					*/
					String[] documentIds = queryString.split(" OR ");
					int size = documentIds.length;
					int halfSize = size / 2;
					StringBuilder firstHalfQueryString = new StringBuilder();
					for(int i=0;i<halfSize;i++){
						firstHalfQueryString.append(documentIds[i]);
						if(i != halfSize - 1){
							firstHalfQueryString.append(" OR ");
						}
					}
					StringBuilder secondHalfQueryString = new StringBuilder();
					for(int i=halfSize;i<size;i++){
						secondHalfQueryString.append(documentIds[i]);
						if(i != size - 1){
							secondHalfQueryString.append(" OR ");
						}
					}
					checkCreateDocumentId(firstHalfQueryString.toString(), halfSize, errorBwriter);
					checkCreateDocumentId(secondHalfQueryString.toString(), size - halfSize, errorBwriter);
				}
			}
		}
	}
}
