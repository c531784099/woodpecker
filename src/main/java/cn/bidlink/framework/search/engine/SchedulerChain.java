package cn.bidlink.framework.search.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 调度器链
 * */
public class SchedulerChain extends AbstractScheduler {

	/**原调度器集合*/
    private List<Scheduler> originalSchedulers = new ArrayList<Scheduler>();
    /**执行过的调度器集合*/
    private List<Scheduler> invokedSchedulers = new ArrayList<Scheduler>();

    private TerminatedCallback callback;

    private long terminatedTime;

    private long completedTime;

    private ExecutorService threadPool;

    public void setTerminatedCallback(TerminatedCallback callback) {
        this.callback = callback;
    }

    public void add(Scheduler scheduler) {
        if (scheduler != null) {
            originalSchedulers.add(scheduler);
        }
    }

    public void add(List<Scheduler> schedulers) {
        for (Scheduler scheduler : schedulers) {
            add(scheduler);
        }
    }

    @Override
    public void shutdown() {
        shutdown(originalSchedulers);
    }

    @Override
    public void execute() throws Exception {
    	//int i = 0;
        for (Scheduler scheduler : originalSchedulers) {
        	//if(i != 0){
        		invokedSchedulers.add(scheduler);
                scheduler.run();
        	//}
        	//i++;
        }
    }

    public void start() {
        if (originalSchedulers.size() == 0) {
            return;
        }
        threadPool = Executors.newSingleThreadExecutor();
        threadPool.execute(this);
        threadPool.shutdown();
        final SchedulerChain self = this;
        if (callback != null) {
            final ScheduledExecutorService holdThreadPool = Executors.newScheduledThreadPool(1);
            holdThreadPool.scheduleAtFixedRate(new Runnable() {

                @Override
                public void run() {
                    //hold
                    if (isTerminated()) {
                        terminatedTime = System.currentTimeMillis() - getStartTime().getTime();

                        //terminated
                        logger.info("Scheduler [ " + getName() + " ] terminated");

                        if (isCompleted()) {
                            completedTime = System.currentTimeMillis() - getStartTime().getTime();
                        }

                        if (callback != null) {
                            callback.call(self);
                        }
                        holdThreadPool.shutdownNow();
                    }

                }
            }, 0, 1000, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public boolean isTerminated() {
        return threadPool != null && threadPool.isTerminated() && isTerminated(invokedSchedulers);
    }

    @Override
    public long getTerminatedTime() {
        if (callback != null) {
            return terminatedTime;
        }
        return 0;
    }

    @Override
    public boolean isCompleted() {
        return threadPool != null && threadPool.isTerminated() && isCompleted(invokedSchedulers);
    }

    @Override
    public long getCompletedTime() {
        if (callback != null) {
            return completedTime;
        }
        return 0;
    }

    @Override
    public long getCompletedCount() {
        return getCompletedCount(invokedSchedulers);
    }

    @Override
    public Exception getException() {
        Exception exception = super.getException();
        if (exception == null) {
            exception = getException(invokedSchedulers);
        }
        return exception;
    }
    
    @Override
    public List<Scheduler> getSchedulers() {
        return Collections.unmodifiableList(originalSchedulers);
    }

    public interface TerminatedCallback {

        public void call(SchedulerChain node);

    }

}
