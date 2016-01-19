package cn.bidlink.framework.search.service;

import java.sql.SQLException;

import cn.bidlink.framework.common.Paging;

public interface IndexTaskService {
	/**
	 * 获得分页的任务列表
	 * */
	public Paging findCreateTaskPaging(Paging paging, Long indexId);
	/**
     * 开始执行CreateTask
     * */
	public void startCreateTask(Long createTaksId);
	/**
     * 获得维护任务	数据个数
     * */
	public Long getMaintainTaskCount(Long indexId) throws SQLException;
	/**
     * 获得创建任务	任务个数
     * */
	public Long getCreateTaskCount(String indexName);
	/**
     * CreateTask进行分任务
     * */
    public void divideCreatTask(Long indexId);
    /**
     * 删除MaintainTask队列数据
     * */
    public void deleteMaintainTaskQueue(Long indexId, String createtime, String type);
    /**
     * 修改CreateTask任务状态为未完成
     * */
    public void updateCreateTasksUnCompleted(Long indexId);
    /**
     * 修改CreateTask
     * */
    public void updateCreateTask(Long createTaskId, String sql);
    /**
     * 删除CreateTask
     * */
    public void deleteCreateTask(Long createTaskId);
    /**
     * 添加CreateTask
     * */
    public String addCreateTask(Long indexId);
    
}
