package cn.bidlink.framework.search.service.impl;

import cn.bidlink.framework.search.Constants;
import cn.bidlink.framework.search.engine.*;
import cn.bidlink.framework.search.engine.Scheduler.TerminatedCallback;
import cn.bidlink.framework.search.model.CreateTask;
import cn.bidlink.framework.search.model.Index;
import cn.bidlink.framework.search.model.MaintainTask;
import cn.bidlink.framework.search.model.Task;
import cn.bidlink.framework.search.service.IndexConfigService;
import cn.bidlink.framework.search.service.SchedulerService;
import cn.bidlink.framework.util.ExceptionUtils;

import java.util.ArrayList;
import java.util.List;

public class DefaultSchedulerService implements SchedulerService {

    protected IndexConfigService indexConfigService;
    
    public void setIndexConfigService(IndexConfigService indexConfigService) {
        this.indexConfigService = indexConfigService;
    }

    @Override
    public SchedulerChain createScheduler(Index index) {

        SchedulerChain chain = new SchedulerChain();
        chain.setName(index.getName());
        //TODO
        chain.setTerminatedCallback(null);
        Scheduler createScheduler = createCreatorScheduler(index);
        if (createScheduler != null) {
            chain.add(createScheduler);
            //Scheduler checkCreateDocumentIdScheduler = createCheckCreateDocumentIdScheduler(index);
            //chain.add(checkCreateDocumentIdScheduler);
        }
        Scheduler maintainScheduler = createMaintainScheduler(index);
        if (maintainScheduler != null) {
            chain.add(maintainScheduler);
        }
        Scheduler optimizeScheduler = createOptimizeScheduler(index);
        if (optimizeScheduler != null) {
            chain.add(optimizeScheduler);
        }
        return chain;
    }


    protected Scheduler createCreatorScheduler(final Index index) {

        SolrInputDocumentMapper mapper = new SolrInputDocumentMapper();
        mapper.addField(new ArrayList<DocumentField>(index.getFields()));

        List<Scheduler> schedulers = new ArrayList<Scheduler>();
        boolean isManyThreads = false;
        //@Refact这地方有机会改成执行SQL的
        String tableName = null;
        for (CreateTask task : index.getCreateTasks()){
        	if (task.getOriginal() != null && task.getOriginal()){
                isManyThreads = (task.getDividable()==(null)?false:task.getDividable());
                tableName = task.getTableName();
            	//isManyThreads = task.getDividable();
        	}
        }
        for (CreateTask task : index.getCreateTasks()) {
            //非原始配置且未完成的Task
            if ((task.getOriginal() == null || !task.getOriginal())
                    && (task.getCompleted() == null || !task.getCompleted())) {
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

                schedulers.add(createDocumentScheduler);
            }

        }

        if (schedulers.size() == 0) {
            return null;
        }

        MultiSchedulerWrapper multiScheduler = new MultiSchedulerWrapper();
        multiScheduler.add(schedulers);
        multiScheduler.setAsyn(true);
        multiScheduler.setWaitTerminated(true);
        multiScheduler.setName("create");

        multiScheduler.setCallback(new TerminatedCallback() {

            @Override
            public void call(Scheduler scheduler) throws Exception {
                indexConfigService.createSolrServer(index).commit();
            }
        });
        return multiScheduler;
    }

    protected Scheduler createMaintainScheduler(final Index index) {
        SolrInputDocumentMapper mapper = new QueuedSolrInputDocumentMapper();
        mapper.addField(new ArrayList<DocumentField>(index.getFields()));

        List<Scheduler> schedulers = new ArrayList<Scheduler>();
        for (MaintainTask task : index.getMaintainTasks()) {
            MaintainDocumentScheduler maintainDocumentScheduler = new MaintainDocumentScheduler();
            maintainDocumentScheduler.setName(task.getName());
            maintainDocumentScheduler.setDatabaseDialect(getDatabaseDialectByIndex(index));
            maintainDocumentScheduler.setQueryString(task.getContentSql());
            maintainDocumentScheduler.setBatchSize(index.getDbBatchSize());
            maintainDocumentScheduler.setUniqueField(index.getUniqueField());
            maintainDocumentScheduler.setIntervalOfCheck(task.getRepeatInterval());
            maintainDocumentScheduler.setQueueTableName(task.getQueueTableName());
            maintainDocumentScheduler.setRowMapper(mapper);
            //黑词过滤队列读取时不用创建solr server
            if(!Constants.QUEUE_SP_COMPANY_MYSQL_TABLE_NAME.equals(task.getQueueTableName().toLowerCase()) && !Constants.QUEUE_SP_COMPANY_MYSQL_TABLE_NAME_TEST.equals(task.getQueueTableName().toLowerCase())){
            	maintainDocumentScheduler.setSolrServer(indexConfigService.createSolrServer(index));
            }
            maintainDocumentScheduler.setMaintainThread(true);

            maintainDocumentScheduler.setCallback(new TerminatedCallback() {

                @Override
                public void call(Scheduler scheduler) throws Exception {
                    //保存当前的task状态
                    saveResult(index, scheduler);
                }
            });

            schedulers.add(maintainDocumentScheduler);
        }

        if (schedulers.size() == 0) {
            return null;
        }

        MultiSchedulerWrapper multiScheduler = new MultiSchedulerWrapper();
        multiScheduler.add(schedulers);
        multiScheduler.setAsyn(true);
        multiScheduler.setWaitTerminated(false);
        multiScheduler.setName("maintain");

        return multiScheduler;
    }

    protected Scheduler createOptimizeScheduler(Index index) {


        OptimizeScheduler optimizeScheduler = new OptimizeScheduler();
        optimizeScheduler.setName("optimize");
        optimizeScheduler.setSolrServer(indexConfigService.createSolrServer(index));

        MultiSchedulerWrapper multiScheduler = new MultiSchedulerWrapper();
        multiScheduler.add(optimizeScheduler);
        multiScheduler.setAsyn(true);
        multiScheduler.setWaitTerminated(false);
        multiScheduler.setName("optimize");

        return multiScheduler;
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
        //
        if (results.size() > 0) {
            indexConfigService.saveTaskStatus(results.toArray(new Task[results.size()]));
        }
    }

    protected Scheduler createCheckCreateDocumentIdScheduler(Index index){
    	CheckCreateDocumentIdScheduler scheduler = new CheckCreateDocumentIdScheduler();
    	scheduler.setSolrServer(indexConfigService.createSolrServer(index));
    	scheduler.setDatabaseDialect(getDatabaseDialectByIndex(index));
    	scheduler.setName(index.getName());
    	scheduler.setIndexName(index.getName());
    	return scheduler;
    }
    
    public DatabaseDialect getDatabaseDialectByIndex(Index index){
    	DatabaseDialect dialect = index.getDatabaseDialect();
        dialect.setDriver(index.getDbDriverClass());
        dialect.setUrl(index.getDbUrl());
        dialect.setUser(index.getDbUsername());
        dialect.setPassword(index.getDbPassword());
        return dialect;
    }

}
