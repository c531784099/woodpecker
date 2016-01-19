package cn.bidlink.framework.search.engine;

import java.util.Date;
import java.util.List;

/**
 * 定义调度器
 * */
public interface Scheduler extends Runnable {

    public String getName();

    public Date getStartTime();

    public boolean isTerminated();

    public long getTerminatedTime();

    public boolean isCompleted();

    public long getCompletedTime();

    public long getCompletedCount();

    public void shutdown();

    public Exception getException();

    public List<Scheduler> getSchedulers();

    /**
     * 终结回调
     * */
    public void setCallback(TerminatedCallback callback);

    /**
     * 定义终结回调函数
     * */
    public interface TerminatedCallback {
        public void call(Scheduler scheduler) throws Exception;
    }
}
