package cn.bidlink.framework.search.service.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bidlink.framework.common.Paging;
import cn.bidlink.framework.dao.CommonDao.Sort;
import cn.bidlink.framework.search.dao.impl.IndexDaoImpl;
import cn.bidlink.framework.search.dao.impl.TaskDaoImpl;
import cn.bidlink.framework.search.engine.AppendDocumentScheduler;
import cn.bidlink.framework.search.engine.DatabaseDialect;
import cn.bidlink.framework.search.engine.DocumentField;
import cn.bidlink.framework.search.engine.DocumentScheduler;
import cn.bidlink.framework.search.engine.Scheduler;
import cn.bidlink.framework.search.engine.SolrInputDocumentMapper;
import cn.bidlink.framework.search.engine.Scheduler.TerminatedCallback;
import cn.bidlink.framework.search.model.CreateTask;
import cn.bidlink.framework.search.model.Index;
import cn.bidlink.framework.search.model.MaintainTask;
import cn.bidlink.framework.search.model.Task;
import cn.bidlink.framework.search.service.IndexConfigService;
import cn.bidlink.framework.search.service.IndexTaskService;
import cn.bidlink.framework.util.ExceptionUtils;

public class IndexTaskServiceImpl implements IndexTaskService {
	
	private TaskDaoImpl taskDao;
	private IndexDaoImpl indexDao;
	private IndexConfigService indexConfigService;
	
	public void setTaskDao(TaskDaoImpl taskDao) {
		this.taskDao = taskDao;
	}
	public void setIndexDao(IndexDaoImpl indexDao) {
		this.indexDao = indexDao;
	}
	public void setIndexConfigService(IndexConfigService indexConfigService) {
		this.indexConfigService = indexConfigService;
	}
	
	@Override
	public Paging findCreateTaskPaging(Paging paging, Long indexId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("indexId", indexId);
		Map<String, Sort> sorts = new HashMap<String, Sort>();
		sorts.put("id", Sort.ASC);
		List<CreateTask> tasks = taskDao.getCommonDao().find(CreateTask.class, params, sorts, paging);//("FROM CreateTask WHERE indexId=:indexId", params, paging);
		paging.setResult(tasks);
		return paging;
	}

	@Override
	public void startCreateTask(Long createTaksId) {
		CreateTask task = taskDao.getCommonDao().get(CreateTask.class, createTaksId);
		final Index index = indexDao.get(task.getIndexId());
		boolean isManyThreads = false;
		//@Refact这地方有机会改成执行SQL的
        String tableName = null;
		for (CreateTask taskTemp : index.getCreateTasks()){
        	if (taskTemp.getOriginal() != null && taskTemp.getOriginal()){
                isManyThreads = (taskTemp.getDividable()==(null)?false:taskTemp.getDividable());
                tableName = taskTemp.getTableName();
        	}
        }
        SolrInputDocumentMapper mapper = new SolrInputDocumentMapper();
        mapper.addField(new ArrayList<DocumentField>(index.getFields()));
		if ((task.getOriginal() == null || !task.getOriginal())) {
            DocumentScheduler createDocumentScheduler = new AppendDocumentScheduler();
            createDocumentScheduler.setName(task.getName());
            createDocumentScheduler.setIndexName(index.getName());
            createDocumentScheduler.setQueryString(task.getContentSql());
            createDocumentScheduler.setBatchSize(index.getDbBatchSize());
            createDocumentScheduler.setDatabaseDialect(getDatabaseDialectByIndex(index));
            createDocumentScheduler.setUniqueField(index.getUniqueField());
            createDocumentScheduler.setRowMapper(mapper);
            createDocumentScheduler.setSolrServer(indexConfigService.createSolrServer(index));
            createDocumentScheduler.setManyThreads(isManyThreads);
            createDocumentScheduler.setTableName(tableName);
            createDocumentScheduler.setCallback(new TerminatedCallback() {
                @Override
                public void call(Scheduler scheduler) throws Exception {
                    //保存当前的task状态
                    saveResult(index, scheduler);
                }
            });
            createDocumentScheduler.run();
        }
	}
	
	protected void saveResult(Index index, Scheduler scheduler) {
        if (scheduler == null) {
            return;
        }
        //保存完成的task信息
        List<Task> results = new ArrayList<Task>();
        Task task = null;
        for (Task perTask : index.getTasks()) {
            if (scheduler.getName().equals(perTask.getName())) {
                task = new Task();
                task.setId(perTask.getId());
                break;
            }
        }
        if (task == null) {
            return;
        }
        task.setStartTime(scheduler.getStartTime());
        task.setCompleted(scheduler.isCompleted());
        task.setCompletedTime(scheduler.getCompletedTime());
        task.setCompletedCount(scheduler.getCompletedCount());
        task.setException(ExceptionUtils.getExceptionStack(scheduler.getException()));
        results.add(task);
        if (results.size() > 0) {
            indexConfigService.saveTaskStatus(results.toArray(new Task[results.size()]));
        }
    }
	
	public DatabaseDialect getDatabaseDialectByIndex(Index index){
    	DatabaseDialect dialect = index.getDatabaseDialect();
        dialect.setDriver(index.getDbDriverClass());
        dialect.setUrl(index.getDbUrl());
        dialect.setUser(index.getDbUsername());
        dialect.setPassword(index.getDbPassword());
        return dialect;
    }
	
	@Override
	public Long getMaintainTaskCount(Long indexId) throws SQLException{
		Index index = indexDao.get(indexId);
    	List<MaintainTask> maintainTasks = index.getMaintainTasks();
    	if(maintainTasks == null || maintainTasks.size() == 0){
    		return 0L;
    	}
    	MaintainTask maintainTask = maintainTasks.get(0);
    	String sql = "select count(*) from "+maintainTask.getQueueTableName()+ " where UPDATETIME IS NULL";
    	DatabaseDialect dialect = getDatabaseDialectByIndex(index);
    	Connection conn = dialect.getConnection();
        //System.out.println(dialect.setUrl());
		Statement statement = conn.createStatement();
		ResultSet rs = null;
        //System.out.println(sql);
		try{
			rs = statement.executeQuery(sql);
			while(rs != null && rs.next()){
                //System.out.println("******"+rs.getLong(1));
				return rs.getLong(1);
			}
		}catch(SQLException e){
			e.printStackTrace();
		}finally{
			if(rs != null && !rs.isClosed()){
				rs.close();
			}
			if(statement != null && !statement.isClosed()){
				statement.close();
			}
			if(conn != null && !conn.isClosed()){
				conn.close();
			}
		}
		return 0L;
	}
	
	@Override
	public Long getCreateTaskCount(String indexName) {
		Index index = indexDao.getUnique("name", indexName);
		index = indexDao.get(index.getId());
		List<CreateTask> createTasks = index.getCreateTasks();
		if(createTasks == null || createTasks.size() == 0){
			return 0L;
		}
		return (long) createTasks.size();
	}
	
	@Override
	public void divideCreatTask(Long indexId) {
        //重新产生task
        indexConfigService.generateCreateTask(indexId);
	}
	
	@Override
	public void deleteMaintainTaskQueue(Long indexId, String createtime, String type) {
		Index index = indexDao.get(indexId);
		List<MaintainTask> tasks = index.getMaintainTasks();
		for(MaintainTask task : tasks) {
			String sql = null;
			
			DatabaseDialect dialect = index.getDatabaseDialect();
			dialect.setDriver(index.getDbDriverClass());
			dialect.setUrl(index.getDbUrl());
			dialect.setUser(index.getDbUsername());
			dialect.setPassword(index.getDbPassword());
			Connection conn = dialect.getConnection();
			if(createtime != null){
				if("1".equals(type)){
					sql = "update " + task.getQueueTableName() + " set UPDATETIME=null where CREATETIME >= "+dialect.getTimeFormat(createtime);
				}else if("2".equals(type)){
					sql = "delete from " + task.getQueueTableName() + " where CREATETIME <= "+dialect.getTimeFormat(createtime) + " and UPDATETIME is not null";
				}
			}else{
				if("1".equals(type)){
					sql = "update " + task.getQueueTableName() + " set UPDATETIME=null where UPDATETIME is not null";
				}else if("2".equals(type)){
					sql = "delete from " + task.getQueueTableName() + " where UPDATETIME is not null";
				}
			}
			Statement stam = null;
			try {
				stam = conn.createStatement();
				stam.execute(sql);
				conn.commit();
			} catch (SQLException e) {
				try {
					conn.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			} finally{
				if(stam != null){
					try {
						stam.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				if(conn != null){
					try {
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	@Override
	public void updateCreateTasksUnCompleted(Long indexId) {
		Index index = indexDao.get(indexId);
		if(index != null){
			List<CreateTask> createTasks = index.getCreateTasks();
			if(createTasks != null){
				for(CreateTask createTask : createTasks){
					if(createTask.getCompleted() != null && createTask.getCompleted()){
						createTask.setCompleted(false);
						createTask.setCompletedCount(0L);
						createTask.setCompletedTime(null);
						taskDao.update(createTask);
					}
				}
			}
		}
	}
	@Override
	public void updateCreateTask(Long createTaskId, String sql) {
		CreateTask createTask = (CreateTask) taskDao.get(createTaskId);
		if(createTask != null){
			createTask.setContentSql(sql);
			taskDao.update(createTask);
		}
	}
	@Override
	public void deleteCreateTask(Long createTaskId) {
		CreateTask createTask = (CreateTask) taskDao.get(createTaskId);
		if(createTask != null){
			taskDao.delete(createTask);
		}
	}
	@Override
	public String addCreateTask(Long indexId) {
		String queryString = "FROM CreateTask WHERE indexId="+indexId+" ORDER BY id DESC LIMIT 0,1";
		CreateTask createTask = indexDao.getCommonDao().findUnique(queryString);
		String newName = "create" + (Integer.parseInt(createTask.getName().replace("create", "")) + 1);
		if(!createTask.getOriginal()){
			indexDao.getCommonDao().evict(createTask);
			createTask.setId(null);
			createTask.setName(""+newName);
			taskDao.save(createTask);
			return newName;
		}else{
			return "-1";
		}
	}
}
