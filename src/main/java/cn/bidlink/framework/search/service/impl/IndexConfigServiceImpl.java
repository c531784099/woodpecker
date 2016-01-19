package cn.bidlink.framework.search.service.impl;

import cn.bidlink.framework.exception.GeneralException;
import cn.bidlink.framework.search.dao.impl.IndexDaoImpl;
import cn.bidlink.framework.search.dao.impl.TaskDaoImpl;
import cn.bidlink.framework.search.engine.DatabaseDialect;
//import cn.bidlink.framework.search.engine.solr.SolrConnectionPoolFactory;
import cn.bidlink.framework.search.model.CreateTask;
import cn.bidlink.framework.search.model.Index;
import cn.bidlink.framework.search.model.Task;
import cn.bidlink.framework.search.service.IndexConfigService;
import org.apache.http.client.HttpClient;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.HttpClientUtil;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.springframework.util.CollectionUtils;

import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class IndexConfigServiceImpl implements IndexConfigService {

    private IndexDaoImpl indexDao;
    private TaskDaoImpl taskDao;


    public void setIndexDao(IndexDaoImpl indexDao) {
        this.indexDao = indexDao;
    }
    public void setTaskDao(TaskDaoImpl taskDao) {
        this.taskDao = taskDao;
    }

    public void generateCreateTask(Long indexId) {
        Index index = indexDao.get(indexId);
        List<CreateTask> finalCreatTasks = new ArrayList<CreateTask>();
        for (CreateTask createTask : index.getCreateTasks()) {
            if (createTask.getOriginal() != null && createTask.getOriginal()) {
                List<String> finalQueryString = new ArrayList<String>();
                if (createTask.getDividable() != null && createTask.getDividable()) {
                    TaskDivider taskDivider = new TaskDivider();
                    taskDivider.setContentQueryString(createTask.getContentSql());
                    taskDivider.setDivideQueryString(createTask.getDivideSql());
                    taskDivider.setDivideSize(createTask.getDivideSize());
                    taskDivider.setTableName(createTask.getTableName());
                    taskDivider.setUniqueField(index.getUniqueField());
                    DatabaseDialect dialect = index.getDatabaseDialect();
                    dialect.setDriver(index.getDbDriverClass());
                    dialect.setUrl(index.getDbUrl());
                    dialect.setUser(index.getDbUsername());
                    dialect.setPassword(index.getDbPassword());
                    taskDivider.setDatabaseDialect(dialect);
                    finalQueryString.addAll(taskDivider.divide());
                } else {
                    finalQueryString.add(createTask.getContentSql());
                }
                for (int i = 0; i < finalQueryString.size(); i++) {
                    CreateTask finalCreateTask = new CreateTask();
                    finalCreateTask.setName(createTask.getName() != null ? createTask.getName() + i : String.valueOf(i));
                    finalCreateTask.setContentSql(finalQueryString.get(i));
                    finalCreateTask.setOriginal(Boolean.FALSE);
                    finalCreateTask.setCompleted(Boolean.FALSE);
                    finalCreateTask.setDividable(Boolean.FALSE);
                    finalCreateTask.setIndexId(createTask.getIndexId());
                    finalCreatTasks.add(finalCreateTask);
                }
            }
        }
        if (finalCreatTasks.size() == 0) {
            return;
        }
        //删除以前的
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("original", Boolean.FALSE);
        params.put("indexId", indexId);

        List<Task> generatedTasks = null;
        taskDao.getByProperties(params);

        if (!CollectionUtils.isEmpty(generatedTasks)) {
            for (Task task : generatedTasks) {
                taskDao.delete(task.getId());
            }
        }
        //保存现在的
        if (finalCreatTasks.size() > 0) {
            for (CreateTask fianlCreateTask : finalCreatTasks) {
                taskDao.save(fianlCreateTask);
            }
        }
    }

    public void saveTaskStatus(Task[] tasks) {
    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (Task task : tasks) {
            StringBuffer ql = new StringBuffer();
            ql.append(" update INDEXER_TASK t set ");
            if (task.getStartTime() == null) {
                ql.append(" t.start_time = null, ");
            } else {
                ql.append(" t.start_time = '"+format.format(task.getStartTime())+"', ");
            }
            ql.append(" t.completed = "+((task.getCompleted() == null ? false : task.getCompleted()) ? 1 : 0)+", ");
            ql.append(" t.completed_time = '"+(task.getCompletedTime() == null ? 0 : task.getCompletedTime())+"', ");
            ql.append(" t.completed_count = "+(task.getCompletedCount() == null ? 0 : task.getCompletedCount())+", ");
            if (task.getException() == null) {
                ql.append(" t.exception = null ");
            } else {
                ql.append(" t.exception = '"+task.getException()+"' ");
            }
            ql.append(" where t.id = "+task.getId()+" ");
            taskDao.getCommonDao().executeSQL(ql.toString());
        }
    }

    public void clearQueue(Long indexId) {
        indexDao.clearQueue(indexId);
    }

    public CloudSolrServer createSolrServer(Index index) {
       // return SolrConnectionPoolFactory.getInstance().getConnectionPool(index);

        ModifiableSolrParams params = new ModifiableSolrParams();
        params.set(HttpClientUtil.PROP_MAX_CONNECTIONS, 1000);
        params.set(HttpClientUtil.PROP_MAX_CONNECTIONS_PER_HOST, 200);
        params.set(HttpClientUtil.PROP_CONNECTION_TIMEOUT, 1000*60*5);
        //params.set(HttpClientUtil.PROP_SO_TIMEOUT, 1000*60*1);
        // Generally setting SO_TIMEOUT is not a good idea, but in some
        // situations it might be required. Here's how you do it:
        //params.set(HttpClientUtil.PROP_CONNECTION_TIMEOUT, 120000);
        CloudSolrServer solrServer;
        HttpClient client = HttpClientUtil.createClient(params);
        try{
            LBHttpSolrServer lbServer = new LBHttpSolrServer(client);
            solrServer = new CloudSolrServer(index.getSolrServerUrl(), lbServer);

            solrServer.setDefaultCollection(index.getSolrServerCollection());
            solrServer.setZkClientTimeout(5*60000);
            solrServer.setZkClientTimeout(5*60000);
        } catch (MalformedURLException e) {
            throw new GeneralException("create solrServer error", e);
        }
        return solrServer;

        /**
         CloudSolrServer solrServer = null;
         try {

         solrServer = new CloudSolrServer(index.getSolrServerUrl());
         solrServer.setDefaultCollection(index.getSolrServerCollection());

         solrServer.setZkClientTimeout(zkClientTimeout);
         solrServer.setZkConnectTimeout(zkConnectTimeout);
         } catch (MalformedURLException e) {
         throw new GeneralException("create solrServer error", e);
         }
         return solrServer;
         */
    }


}
