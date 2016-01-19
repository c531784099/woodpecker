package cn.bidlink.framework.search.service;

import cn.bidlink.framework.search.engine.Scheduler;
import cn.bidlink.framework.search.model.Index;
import org.apache.log4j.Logger;

public class IndexSchedulser {

    public final Logger logger = Logger.getLogger(getClass());

    private Index index;

    private Scheduler scheduler;

    public IndexSchedulser(Index index, Scheduler scheduler) {
        this.index = index;
        this.scheduler = scheduler;
    }

    public Index getIndex() {
        return index;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }
}
