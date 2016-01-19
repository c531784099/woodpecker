package cn.bidlink.framework.search.web.action;

import cn.bidlink.framework.common.Paging;
import cn.bidlink.framework.i18n.MessageCollector;
import cn.bidlink.framework.search.dao.impl.FieldDaoImpl;
import cn.bidlink.framework.search.dao.impl.IndexDaoImpl;
import cn.bidlink.framework.search.dao.impl.TaskDaoImpl;
import cn.bidlink.framework.search.engine.AbstractScheduler;
import cn.bidlink.framework.search.engine.AppendDocumentScheduler;
import cn.bidlink.framework.search.engine.Scheduler;
import cn.bidlink.framework.search.model.*;
import cn.bidlink.framework.search.service.IndexConfigService;
import cn.bidlink.framework.search.service.IndexService;
import cn.bidlink.framework.search.service.IndexTaskService;
import cn.bidlink.framework.util.CollectionUtils;
import cn.bidlink.framework.util.DateUtils;
import cn.bidlink.framework.web.Action;
import net.sf.json.JSONObject;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexerAction extends Action {

    private IndexDaoImpl indexDao;

    private FieldDaoImpl fieldDao;

    private TaskDaoImpl taskDao;

    private IndexService indexService;

    private IndexConfigService indexConfigService;
    
    private IndexTaskService indexTaskService;

    public void setIndexDao(IndexDaoImpl indexDao) {
        this.indexDao = indexDao;
    }

    public void setIndexService(IndexService indexService) {
        this.indexService = indexService;
    }

    public void setIndexConfigService(IndexConfigService indexConfigService) {
        this.indexConfigService = indexConfigService;
    }

    public void setFieldDao(FieldDaoImpl fieldDao) {
        this.fieldDao = fieldDao;
    }

    public void setTaskDao(TaskDaoImpl taskDao) {
        this.taskDao = taskDao;
    }
    
    public void setIndexTaskService(IndexTaskService indexTaskService) {
		this.indexTaskService = indexTaskService;
	}

	public ModelAndView validateBeforeSaveOrUpdateIndex(HttpServletRequest request,
                                                        HttpServletResponse response, Index index) throws Exception {
        ModelAndView mav = new ModelAndView("saveOrUpdateIndex");
        mav.getModel().put("index", index);
        MessageCollector collector = createMessageCollector(mav);

        if (!StringUtils.hasLength(index.getName())) {
            collector.addError("name", "invalidName", null, null);
        }
        if (!StringUtils.hasLength(index.getDbDriverClass())) {
            collector.addError("dbDriverClass", "index.invalidDbDriverClass", null, null);
        }
        if (!StringUtils.hasLength(index.getDbUrl())) {
            collector.addError("dbUrl", "index.invalidDbUrl", null, null);
        }
        if (!StringUtils.hasLength(index.getDbUsername())) {
            collector.addError("dbUsername", "index.invalidDbUsername", null, null);
        }
        if (!StringUtils.hasLength(index.getDbPassword())) {
            collector.addError("dbPassword", "index.invalidDbPassword", null, null);
        }
        if (!StringUtils.hasLength(index.getSolrServerUrl())) {
            collector.addError("solrServerUrl", "index.invalidSolrServerUrl", null, null);
        }
        if (index.getId() == null && !CollectionUtils.isEmpty(indexDao.getByProperty("name", index.getName()))) {
            collector.addError("sameName", "existSameName", new String[]{"index"}, null);
        }
        return collector.getErrors().size() > 0 ? mav : null;
    }

    public ModelAndView saveOrUpdateIndex(HttpServletRequest request,
                                          HttpServletResponse response, Index index) throws Exception {

        indexDao.saveOrUpdate(index);
        String url = request.getContextPath() + "/indexer/findIndex.htm?id=" + index.getId();
        return new ModelAndView(new RedirectView(url));
    }

    public ModelAndView copyIndex(HttpServletRequest request,
                                  HttpServletResponse response, Index index) throws Exception {
        Index origidx = indexDao.get(index.getId());
        origidx.setId(null);
        origidx.setName(origidx.getName() + "untitle");
        indexDao.saveOrUpdate(origidx);
        String url = request.getContextPath() + "/indexer/findIndexList.htm";
        return new ModelAndView(new RedirectView(url));
    }

    public ModelAndView deleteIndex(HttpServletRequest request,
                                    HttpServletResponse response, Index index) throws Exception {
        indexDao.delete(index.getId());
        String url = request.getContextPath() + "/indexer/findIndexList.htm";
        return new ModelAndView(new RedirectView(url));
    }

    public ModelAndView findIndexList(HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("indexList");
        mav.getModel().put("indexPacks", indexService.findAllIndexSchedulsers());
        return mav;
    }

    public ModelAndView findIndex(HttpServletRequest request,
                                  HttpServletResponse response, Index index) throws Exception {
    	ModelAndView mav = new ModelAndView("indexView");
        mav.getModel().put("index", indexDao.get(index.getId()));
        return mav;
    }
    
    public ModelAndView generateCreatTask(HttpServletRequest request,
                                          HttpServletResponse response, Index index) throws Exception {
        indexConfigService.generateCreateTask(index.getId());
        String url = request.getContextPath() + "/indexer/findIndex.htm?id=" + index.getId();
        return new ModelAndView(new RedirectView(url));
    }

    /**
     * 开始执行调度器
     * */
    public ModelAndView startScheduler(HttpServletRequest request,
                                       HttpServletResponse response, Index index) throws Exception {
        indexService.startScheduler(index.getId());
        return null;
    }

    public ModelAndView shutdowScheduler(HttpServletRequest request,
                                         HttpServletResponse response, Index index) throws Exception {
        indexService.shutdownScheduler(index.getName());
        return null;
    }

    public ModelAndView findIndexForUpdate(HttpServletRequest request,
                                           HttpServletResponse response, Index index) throws Exception {
        ModelAndView mav = new ModelAndView("saveOrUpdateIndex");
        mav.getModel().put("index", indexDao.get(index.getId()));
        return mav;
    }

    public ModelAndView findFieldForUpdate(HttpServletRequest request,
                                           HttpServletResponse response, Field field) throws Exception {
        ModelAndView mav = new ModelAndView("saveOrUpdateField");
        Field finalField = fieldDao.get(field.getId());
        mav.getModel().put("field", finalField);
        mav.getModel().put("index", indexDao.get(finalField.getIndexId()));
        return mav;
    }

    public ModelAndView validateBeforeSaveOrUpdateField(HttpServletRequest request,
                                                        HttpServletResponse response, Field field) throws Exception {
        ModelAndView mav = new ModelAndView("saveOrUpdateField");
        mav.getModel().put("field", field);
        MessageCollector collector = createMessageCollector(mav);

        if (field.getIndexId() == null) {
            collector.addError("indexId", "invalidIndex", null, null);
        }

        if (!StringUtils.hasLength(field.getName())) {
            collector.addError("name", "invalidName", null, null);
        }
        if (!StringUtils.hasLength(field.getColumnNames())) {
            collector.addError("columnNames", "field.invalidColumnNames", null, null);
        }
        if (field.getType() == null) {
            collector.addError("type", "invalidType", null, null);
        }
        if (field.getId() == null) {
            Map<String, Object> checkParams = new HashMap<String, Object>();
            checkParams.put("indexId", field.getIndexId());
            checkParams.put("name", field.getName());
            if (!CollectionUtils.isEmpty(fieldDao.getByProperties(checkParams))) {
                collector.addError("sameName", "existSameName", new String[]{"field"}, null);
            }
        }
        return collector.getErrors().size() > 0 ? mav : null;
    }

    public ModelAndView saveOrUpdateField(HttpServletRequest request,
                                          HttpServletResponse response, Field field) throws Exception {
        if (field.getUnique() == null) {
            field.setUnique(Boolean.FALSE);
        }
        fieldDao.saveOrUpdate(field);
        String url = request.getContextPath() + "/indexer/findIndexForUpdate.htm?id=" + field.getIndexId();
        return new ModelAndView(new RedirectView(url));
    }

    public ModelAndView deleteField(HttpServletRequest request,
                                    HttpServletResponse response, Field field) throws Exception {
        fieldDao.delete(field.getId());
        String url = request.getContextPath() + "/indexer/findIndexForUpdate.htm?id=" + field.getIndexId();
        return new ModelAndView(new RedirectView(url));
    }

    public ModelAndView findTask(HttpServletRequest request,
                                 HttpServletResponse response, Task task) throws Exception {
        ModelAndView mav = new ModelAndView("taskView");
        Task finalTask = taskDao.get(task.getId());
        mav.getModel().put("task", finalTask);
        return mav;
    }

    public ModelAndView findTaskForUpdate(HttpServletRequest request,
                                          HttpServletResponse response, Task task) throws Exception {
        ModelAndView mav = new ModelAndView("saveOrUpdateTask");
        Task finalTask = taskDao.get(task.getId());
        mav.getModel().put("task", finalTask);
        mav.getModel().put("index", indexDao.get(finalTask.getIndexId()));
        return mav;
    }

    public ModelAndView validateBeforeSaveOrUpdateTask(HttpServletRequest request,
                                                       HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView("saveOrUpdateTask");
        MessageCollector collector = createMessageCollector(mav);
        String type = request.getParameter("type");
        if (!StringUtils.hasLength(type) || (!type.equals("create") && !type.equals("maintain"))) {
            collector.addError("type", "invalidType", null, null);
            return mav;
        }

        Task task = null;

        if (type.equals("create")) {
            CreateTask createTask = new CreateTask();
            task = createTask;
            bind(request, createTask);
            if (createTask.getDividable() != null && createTask.getDividable()) {
                if (!StringUtils.hasLength(createTask.getTableName())) {
                    collector.addError("TableName", "task.tableName", null, null);
                }
            }
            mav.getModel().put("task", createTask);
        } else if (type.equals("maintain")) {
            MaintainTask maintainTask = new MaintainTask();
            task = maintainTask;
            bind(request, maintainTask);
            if (!StringUtils.hasLength(maintainTask.getQueueTableName())) {
                if (!StringUtils.hasLength(maintainTask.getQueueTableName())) {
                    collector.addError("queueTableName", "task.queueTableName", null, null);
                }
            }
            mav.getModel().put("task", maintainTask);

        }

        if (task.getIndexId() == null) {
            collector.addError("indexId", "invalidIndex", null, null);
        }

        if (!StringUtils.hasLength(task.getName())) {
            collector.addError("name", "invalidName", null, null);
        }
        if (!StringUtils.hasLength(task.getContentSql())) {
            collector.addError("contentSql", "task.invalidContentSql", null, null);
        }
        if (task.getId() == null) {
            Map<String, Object> checkParams = new HashMap<String, Object>();
            checkParams.put("indexId", task.getIndexId());
            checkParams.put("name", task.getName());
            if (!CollectionUtils.isEmpty(taskDao.getByProperties(checkParams))) {
                collector.addError("sameName", "existSameName", new String[]{"task"}, null);
            }
        }
        return collector.getErrors().size() > 0 ? mav : null;
    }

    public ModelAndView saveOrUpdateTask(HttpServletRequest request,
                                         HttpServletResponse response) throws Exception {
        String type = request.getParameter("type");
        Task task = null;
        if (type.equals("create")) {
            CreateTask createTask = new CreateTask();
            task = createTask;
            createTask.setOriginal(Boolean.TRUE);
        } else if (type.equals("maintain")) {
            task = new MaintainTask();
        }
        bind(request, task);
        taskDao.saveOrUpdate(task);
        String url = request.getContextPath() + "/indexer/findIndexForUpdate.htm?id=" + task.getIndexId();
        return new ModelAndView(new RedirectView(url));
    }

    public ModelAndView deleteTask(HttpServletRequest request,
                                   HttpServletResponse response, Task task) throws Exception {
        taskDao.delete(task.getId());
        String url = request.getContextPath() + "/indexer/findIndexForUpdate.htm?id=" + task.getIndexId();
        return new ModelAndView(new RedirectView(url));
    }

    public ModelAndView findScheduler(HttpServletRequest request,
                                      HttpServletResponse response, Index index) throws Exception {
    	Paging paging = new Paging();
    	paging.setPageSize(20);
    	String pageNo = request.getParameter("pageNo");
    	if(pageNo != null){
    		paging.setCurrentPageNum(Integer.parseInt(pageNo));
    	}else{
    		paging.setCurrentPageNum(1);
    	}
        Scheduler scheduler = indexService.findScheduler(index.getName());
        List<Scheduler> schedulers = new ArrayList<Scheduler>();
        schedulers.add(scheduler);
        schedulerIndex = 0;
        response.getWriter().println(getSchedulerTreeJson(schedulers, paging));
        return null;
    }

    int schedulerIndex = 0;

    public String getSchedulerTreeJson(List<Scheduler> schedulers, Paging paging) {
        if (!CollectionUtils.isEmpty(schedulers)) {
        	int startIndex = 0;
        	int endIndex = schedulers.size()-1;
        	if(schedulers.get(0) instanceof AppendDocumentScheduler){
            	paging.setAutoCalculateTotal(false);
            	paging.setRecordTotal(schedulers.size());
            	startIndex = paging.getFirstIndex();
            	endIndex = paging.getFirstIndex()+paging.getPageSize()-1;
            }
            StringBuffer schedulerTreeJson = new StringBuffer("[");
            
            for (int i = startIndex; i <= endIndex; i++) {
                Scheduler scheduler = schedulers.get(i);
                if (scheduler == null) {
                    continue;
                }
                schedulerTreeJson.append("{");
                schedulerTreeJson.append("\"id\":" + (++schedulerIndex) + ",\n");
                schedulerTreeJson.append("\"name\":\"" + (scheduler.getName() == null ? "" : scheduler.getName()) + "\",\n");
                String startTime = "";
                if (scheduler.getStartTime() != null) {
                    startTime = DateUtils.format(scheduler.getStartTime(), "yyyy-MM-dd HH:mm:ss.SSS");
                }
                schedulerTreeJson.append("\"startTime\":\"" + startTime + "\",\n");
                schedulerTreeJson.append("\"terminated\":\"" + scheduler.isTerminated() + "\",\n");
                schedulerTreeJson.append("\"terminatedTime\":\"" + scheduler.getTerminatedTime() + "\",\n");
                schedulerTreeJson.append("\"completed\":\"" + scheduler.isCompleted() + "\",\n");
                schedulerTreeJson.append("\"completedTime\":\"" + scheduler.getCompletedTime() + "\",\n");
                schedulerTreeJson.append("\"completedCount\":\"" + scheduler.getCompletedCount() + "\",\n");
                schedulerTreeJson.append("\"exception\":\"" + (scheduler.getException() == null ? "" : scheduler.getException().getMessage()) + "\"");
                if (!CollectionUtils.isEmpty(scheduler.getSchedulers())) {
                    schedulerTreeJson.append(",\n");
                    schedulerTreeJson.append("\"children\":" + getSchedulerTreeJson(scheduler.getSchedulers(), paging));
                }
                schedulerTreeJson.append("}" + (i < endIndex ? ",\n" : ""));
            }
            schedulerTreeJson.append("]");
            schedulerTreeJson.append("\n");
            return schedulerTreeJson.toString();
        }
        return "";
    }

    public ModelAndView validateBeforeShutdownServer(HttpServletRequest request,
                                                     HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView();
        MessageCollector collector = createMessageCollector(mav);
        List<Scheduler> schedulers = indexService.findAllSchedulers();
        if (!CollectionUtils.isEmpty(schedulers) && !AbstractScheduler.isTerminated(schedulers)) {
            collector.addError("activeScheduler", "Have running scheduler");
        }
        return collector.getErrors().size() > 0 ? mav : null;
    }

    public ModelAndView shutdownServer(HttpServletRequest request,
                                       HttpServletResponse response, Index index) throws Exception {
        System.exit(0);
        response.getWriter().println("Shutdown");
        return null;
    }

    public ModelAndView validateBeforeClearDocument(HttpServletRequest request,
                                                    HttpServletResponse response, Index index) throws Exception {
        ModelAndView mav = new ModelAndView();
        MessageCollector collector = createMessageCollector(mav);
        Scheduler scheduler = indexService.findScheduler(index.getName());
        if (scheduler != null && !scheduler.isTerminated()) {
            collector.addError("activeScheduler", "[" + index.getName() + "] scheduler is running");
        }
        return collector.getErrors().size() > 0 ? mav : null;
    }


    public ModelAndView createIndex(HttpServletRequest request,
                                    HttpServletResponse response, Index index) throws Exception {
        ModelAndView mav = new ModelAndView();
        MessageCollector collector = createMessageCollector(mav);
        try {
            indexService.createIndex(index.getId());
            collector.addInfo("success", "Create index [" + index.getName() + "] success");
        } catch (Exception e) {
            String errorMsg = "create index [" + index.getName() + "] failed";
            logger.error(errorMsg, e);
            collector.addError("createFailed", errorMsg + "\n " + e.getCause().getMessage());
        }
        return mav;
    }

    public ModelAndView recreateIndex(HttpServletRequest request,
    		HttpServletResponse response, Index index) throws Exception {
        ModelAndView mav = new ModelAndView();
        MessageCollector collector = createMessageCollector(mav);
        try {
            indexService.recreateIndex(index.getId());
            collector.addInfo("success", "Recreate index [" + index.getName() + "] success");
        } catch (Exception e) {
            String errorMsg = "recreate index [" + index.getName() + "] failed";
            logger.error(errorMsg, e);
            collector.addError("recreateFailed", errorMsg + "\n " + e.getCause().getMessage());
        }
        return mav;
    }
    /**
     * 不进行分区创建索引
     * */
	public ModelAndView createIndexWithoutDivide(HttpServletRequest request,
			HttpServletResponse response, Index index) throws Exception {
    	ModelAndView mav = new ModelAndView();
        MessageCollector collector = createMessageCollector(mav);
        try {
            indexService.createIndexWithoutDivide(index.getId());
            collector.addInfo("success", "Create index [" + index.getName() + "] success");
        } catch (Exception e) {
            String errorMsg = "create index [" + index.getName() + "] failed";
            logger.error(errorMsg, e);
            collector.addError("createFailed", errorMsg + "\n " + e.getCause().getMessage());
        }
        return mav;
    }
    /**
     * 任务列表分页
     * */
    public void getTaskByPage(HttpServletRequest request, HttpServletResponse response, Paging paging) throws Exception{
    	paging.setPageSize(20);
    	paging.setCurrentPageNum(Integer.parseInt(request.getParameter("pageNo")));
    	JSONObject json = JSONObject.fromObject(indexTaskService.findCreateTaskPaging(paging, Long.parseLong(request.getParameter("indexId"))));
    	response.setContentType("application/json");
    	response.getWriter().print(json);
    }
    /**
     * 重新开始执行任务
     * */
    public void restartTask(HttpServletRequest request, HttpServletResponse response) throws IOException{
    	response.setContentType("text/html");
    	try{
    		indexTaskService.startCreateTask(Long.parseLong(request.getParameter("taskId")));
    		response.getWriter().print("1");
    	}catch(Exception e){
    		e.printStackTrace();
			response.getWriter().print("0");
    	}
    }
    /**
     * 复制任务
     * */
    public ModelAndView copyNewIndex(HttpServletRequest request, HttpServletResponse response){
    	ModelAndView mav = new ModelAndView();
        MessageCollector collector = createMessageCollector(mav);
        String newIndexName = request.getParameter("newIndexName");
        try {
            indexService.copyIndex(Long.parseLong(request.getParameter("indexId")), newIndexName);
            collector.addInfo("success", "Copy index [" + newIndexName + "] success");
        } catch (Exception e) {
            String errorMsg = "Copy index [" + newIndexName + "] failed";
            logger.error(errorMsg, e);
            collector.addError("CopyFailed", errorMsg + "\n " + e.getCause().getMessage());
        }
        return mav;
    }

    /**
     * 浅复制任务
     * */
    public ModelAndView shallowCopyNewIndex(HttpServletRequest request, HttpServletResponse response){
        ModelAndView mav = new ModelAndView();
        MessageCollector collector = createMessageCollector(mav);
        String newIndexName = request.getParameter("newIndexName");
        try {
            indexService.shallowCopyIndex(Long.parseLong(request.getParameter("indexId")), newIndexName);
            collector.addInfo("success", "Copy index [" + newIndexName + "] success");
        } catch (Exception e) {
            String errorMsg = "Copy index [" + newIndexName + "] failed";
            logger.error(errorMsg, e);
            collector.addError("CopyFailed", errorMsg + "\n " + e.getCause().getMessage());
        }
        return mav;
    }
    /**
     * Ajax判断Index名字是否重复
     * */
    public void existsIndexName(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	String newIndexName = request.getParameter("newIndexName");
    	response.setContentType("text/html");
    	if(indexService.existsIndexName(newIndexName)){
    		response.getWriter().print("0");
    	}else{
    		response.getWriter().print("1");
    	}
    }
    /**
     * Ajax获得维护任务的执行数据数
     * */
    public void getMaintainCount(HttpServletRequest request, HttpServletResponse response) throws IOException{
    	response.setContentType("text/html");
    	Long indexId = Long.parseLong(request.getParameter("indexId"));
    	if(indexId != null){
    		try {
				response.getWriter().print(indexTaskService.getMaintainTaskCount(indexId));
			} catch (SQLException e) {
				response.getWriter().print("-1");
			}
    	}else{
    		response.getWriter().print("-1");
    	}
    }
    /**
     * Ajax获得创建任务的条数
     * */
    public void getCreateTaskCount(HttpServletRequest request, HttpServletResponse response) throws IOException{
    	response.setContentType("text/html");
    	String indexName = request.getParameter("indexName");
    	if(indexName != null){
    		response.getWriter().print(indexTaskService.getCreateTaskCount(indexName));
    	}else{
    		response.getWriter().print("-1");
    	}
    }
    /**
     * 执行分任务
     * */
     public ModelAndView divideCreateTask(HttpServletRequest request,
			HttpServletResponse response, Index index) throws Exception {
		ModelAndView mav = new ModelAndView();
		MessageCollector collector = createMessageCollector(mav);
		try {
			indexTaskService.divideCreatTask(index.getId());
			collector.addInfo("success", "Divide task [" + index.getName() + "] success");
		} catch (Exception e) {
			String errorMsg = "divide task [" + index.getName() + "] failed";
			logger.error(errorMsg, e);
			collector.addError("divideTaskFailed", errorMsg + "\n " + e.getCause().getMessage());
		}
		return mav;
	}
     /**
      * 删除维护队列任务
      * */
     public ModelAndView deleteMaintainTaskQueue(HttpServletRequest request,
 			HttpServletResponse response, Index index) throws Exception {
 		ModelAndView mav = new ModelAndView();
 		MessageCollector collector = createMessageCollector(mav);
 		String createtime = request.getParameter("createtime");
 		String type = request.getParameter("type");
 		try {
 			indexTaskService.deleteMaintainTaskQueue(index.getId(), createtime, type);
 			collector.addInfo("success", "Delete queue [" + index.getName() + "] success");
 		} catch (Exception e) {
 			String errorMsg = "delete queue [" + index.getName() + "] failed";
 			logger.error(errorMsg, e);
 			collector.addError("deleteQueueFailed", errorMsg + "\n " + e.getCause().getMessage());
 		}
 		return mav;
 	}
     /**
      * 设置createTask任务为未完成状态
      * */
     public ModelAndView resetCreateTasks(HttpServletRequest request,
  			HttpServletResponse response, Index index) throws Exception {
  		ModelAndView mav = new ModelAndView();
  		MessageCollector collector = createMessageCollector(mav);
  		try {
  			indexTaskService.updateCreateTasksUnCompleted(index.getId());
  			collector.addInfo("success", "Reset createTask [" + index.getName() + "] success");
  		} catch (Exception e) {
  			String errorMsg = "reset createTask [" + index.getName() + "] failed";
  			logger.error(errorMsg, e);
  			collector.addError("resetTaskFailed", errorMsg + "\n " + e.getCause().getMessage());
  		}
  		return mav;
  	}
     /**
      * 修改 CreateTask
      * */
     public void changeSqlCreateTask(HttpServletRequest request, HttpServletResponse response, CreateTask createTask) throws Exception {
   		String sql = request.getParameter("sql");
   		indexTaskService.updateCreateTask(createTask.getId(), sql);
   		response.getWriter().print("1");
   	}
     /**
      * 删除 CreateTask
      * */
     public void deleteCreateTask(HttpServletRequest request, HttpServletResponse response, CreateTask createTask) throws Exception {
   		indexTaskService.deleteCreateTask(createTask.getId());
   		response.getWriter().print("1");
   	}
     /**
      * 设置createTask任务为未完成状态
      * */
     public void addCreateTask(HttpServletRequest request, HttpServletResponse response, Index index) throws Exception {
    	response.setContentType("text/html");
    	String newName = indexTaskService.addCreateTask(index.getId());
  		response.getWriter().print(newName);
  	}
    
    @Override
    public String[] getUrls() {
        return new String[]{"/indexer/*"};
    }

}
