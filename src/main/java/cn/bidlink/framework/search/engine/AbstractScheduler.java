package cn.bidlink.framework.search.engine;

import cn.bidlink.framework.exception.GeneralException;
import cn.bidlink.framework.util.CollectionUtils;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;

public abstract class AbstractScheduler implements Scheduler {

    public final Logger logger = Logger.getLogger(getClass());

    protected String name;
    
    protected String indexName;

    protected boolean isManyThreads;

    public boolean isMaintainThread() {
        return isMaintainThread;
    }

    public void setMaintainThread(boolean maintainThread) {
        isMaintainThread = maintainThread;
    }

    protected  boolean isMaintainThread=false;
    
    protected Date startTime;

    /**是否已终结*/
    protected boolean terminated;

    protected long terminatedTime;

    protected boolean completed;

    protected long completedTime;

    protected Exception exception;

    private TerminatedCallback callback;

    @Override
    public void run() {
        //record status
        startTime = new Date();
        terminated = false;
        terminatedTime = 0;
        completed = false;
        completedTime = 0;
        try {
            execute();
            completed = true;
            completedTime = System.currentTimeMillis() - startTime.getTime();
        } catch (Exception e) {
            logger.error("scheduler [" + getName() + "] error", e);
            exception = e;
            throw new GeneralException("Scheduler [" + getName() + "] execute error ", e);
        } finally {
            terminated = true;
            terminatedTime = System.currentTimeMillis() - startTime.getTime();
            if (callback != null) {
                try {
                    callback.call(this);
                } catch (Exception e) {
                    throw new GeneralException(" TerminatedCallback call failed", e);
                }
            }
        }
    }

    public abstract void execute() throws Exception;

    public String getName() {
        return name;
    }

    public Date getStartTime() {
        return startTime;
    }

    public boolean isTerminated() {
        return terminated;
    }

    public long getTerminatedTime() {
        return terminatedTime;
    }

    public boolean isCompleted() {
        return completed;
    }

    public long getCompletedTime() {
        return completedTime;
    }

    @Override
    public Exception getException() {
        return exception;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCallback(TerminatedCallback callback) {
        this.callback = callback;
    }

    public List<Scheduler> getSchedulers() {
        return null;
    }
    
    public String getIndexName() {
		return indexName;
	}
	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}
	public boolean isManyThreads() {
		return isManyThreads;
	}
	public void setManyThreads(boolean isManyThreads) {
		this.isManyThreads = isManyThreads;
	}

	public static <T extends Scheduler> void shutdown(List<T> schedulers) {
        if (CollectionUtils.isEmpty(schedulers)) {
            return;
        }
        for (Scheduler scheduler : schedulers) {
            scheduler.shutdown();
        }
    }

    public static <T extends Scheduler> boolean isCompleted(List<T> schedulers) {
        if (CollectionUtils.isEmpty(schedulers)) {
            return false;
        }
        boolean completed = true;
        for (Scheduler scheduler : schedulers) {
            completed &= scheduler.isCompleted();
            if (!completed) {
                return false;
            }
        }
        return completed;
    }

    public static <T extends Scheduler> boolean isTerminated(List<T> schedulers) {
        if (CollectionUtils.isEmpty(schedulers)) {
            return false;
        }
        boolean terminated = true;
        for (Scheduler scheduler : schedulers) {
            terminated &= scheduler.isTerminated();
            if (!terminated) {
                return false;
            }
        }
        return terminated;
    }

    public static <T extends Scheduler> long getCompletedCount(List<T> schedulers) {
        if (CollectionUtils.isEmpty(schedulers)) {
            return 0;
        }

        long completedCount = 0;
        for (Scheduler scheduler : schedulers) {
            completedCount += scheduler.getCompletedCount();
        }
        return completedCount;
    }

    public static <T extends Scheduler> long getTerminatedTime(List<T> schedulers) {
        if (CollectionUtils.isEmpty(schedulers)) {
            return 0;
        }

        long terminatedTime = 0;
        for (Scheduler scheduler : schedulers) {
            terminatedTime += scheduler.getTerminatedTime();
        }
        return terminatedTime;
    }

    public static <T extends Scheduler> long getCompletedTime(List<T> schedulers) {
        if (CollectionUtils.isEmpty(schedulers)) {
            return 0;
        }

        long completedTime = 0;
        for (Scheduler scheduler : schedulers) {
            completedTime += scheduler.getCompletedTime();
        }
        return completedTime;
    }

    public static <T extends Scheduler> void execute(ExecutorService executorService, List<T> schedulers) {
        for (Scheduler scheduler : schedulers) {
            executorService.execute(scheduler);
        }
    }

    public static <T extends Scheduler> Exception getException(List<T> schedulers) {
        for (Scheduler scheduler : schedulers) {
            if (scheduler.getException() != null) {
                return scheduler.getException();
            }
        }
        return null;
    }

}
