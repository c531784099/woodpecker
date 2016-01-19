package cn.bidlink.framework.search.service;

import cn.bidlink.framework.search.engine.SchedulerChain;
import cn.bidlink.framework.search.model.Index;

public interface SchedulerService {

	/**
	 * 
	 * */
    public SchedulerChain createScheduler(Index index);

}
