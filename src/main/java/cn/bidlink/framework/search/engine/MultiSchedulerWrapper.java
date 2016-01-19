package cn.bidlink.framework.search.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.bidlink.framework.util.holder.PropertiesHolder;


public class MultiSchedulerWrapper extends AbstractScheduler {

    private List<Scheduler> schedulers = new ArrayList<Scheduler>();

    private boolean asyn;

    private boolean waitTerminated;

    private ExecutorService threadPool;

    public void add(Scheduler scheduler) {
        this.schedulers.add(scheduler);
    }

    public void add(List<Scheduler> schedulers) {
        this.schedulers.addAll(schedulers);
    }

    @Override
    public void execute() throws Exception {
        //threadPool = Executors.newFixedThreadPool(asyn ? schedulers.size() : 1);
    	
    	/*
    	int poolSize = asyn ? PropertiesHolder.getInt("threadPool.maxCount", 10) : 1;
    	BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
    	threadPool = new ThreadPoolExecutor(poolSize, poolSize, 0, TimeUnit.MILLISECONDS, workQueue);
    	for (Scheduler scheduler : schedulers) {
    		threadPool.execute(scheduler);
        }
        */
        threadPool = Executors.newFixedThreadPool(asyn ? PropertiesHolder.getInt("threadPool.maxCount", 10) : 1);
        execute(threadPool, schedulers);
        threadPool.shutdown();
        //hold
        while (waitTerminated && !isTerminated()) ;
    }

    @Override
    public void shutdown() {
        shutdown(schedulers);
        /*
        if (threadPool != null) {
            threadPool.shutdownNow();
        }
        */
    }

    public boolean isAsyn() {
        return asyn;
    }

    @Override
    public long getTerminatedTime() {
        return waitTerminated ? super.getTerminatedTime() : 0;
    }

    @Override
    public boolean isTerminated() {
        return threadPool != null && threadPool.isTerminated() && isTerminated(schedulers);
    }

    @Override
    public boolean isCompleted() {
        return isTerminated() && isCompleted(schedulers);
    }

    @Override
    public long getCompletedTime() {
        return waitTerminated ? super.getCompletedTime() : 0;
    }

    @Override
    public long getCompletedCount() {
        return getCompletedCount(schedulers);
    }

    @Override
    public Exception getException() {
        Exception exception = super.getException();
        if (exception == null) {
            exception = getException(schedulers);
        }
        return exception;
    }

    @Override
    public List<Scheduler> getSchedulers() {
        return Collections.unmodifiableList(schedulers);
    }

    public void setAsyn(boolean asyn) {
        this.asyn = asyn;
    }

    public void setWaitTerminated(boolean waitTerminated) {
        this.waitTerminated = waitTerminated;
    }

}
