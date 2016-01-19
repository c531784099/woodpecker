package cn.bidlink.framework.search.engine;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OptimizeScheduler extends AbstractScheduler {

    public final Logger logger = Logger.getLogger(getClass());

    private SolrServer solrServer;

    private ScheduledExecutorService scheduledExecutorService;

    private long optimizeCount = 0;

    private Exception exception;

    private Date firstTime;

    @Override
    public void execute() {
        firstTime = getFirstTime();
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                try {
                    solrServer.optimize();
                    optimizeCount++;
                } catch (Exception e) {
                    exception = e;
                    logger.error("optimize solr error : " + solrServer, e);
                }

            }
        }, firstTime.getTime() - System.currentTimeMillis(), 24 * 60 * 60 * 1000, TimeUnit.MILLISECONDS);
    }

    private Date getFirstTime() {
        Calendar current = Calendar.getInstance();

        Calendar zeroTime = Calendar.getInstance();
        zeroTime.set(Calendar.HOUR_OF_DAY, 0);
        zeroTime.set(Calendar.MINUTE, 0);
        zeroTime.set(Calendar.SECOND, 0);
        zeroTime.set(Calendar.MILLISECOND, 0);

        Calendar firstTime = zeroTime;
        if (current.after(zeroTime)) {
            firstTime = Calendar.getInstance();
            firstTime.add(Calendar.DAY_OF_MONTH, 1);
        }
        firstTime.set(Calendar.HOUR_OF_DAY, 0);
        firstTime.set(Calendar.MINUTE, 0);
        firstTime.set(Calendar.SECOND, 0);
        firstTime.set(Calendar.MILLISECOND, 0);
        return firstTime.getTime();
    }

    @Override
    public Date getStartTime() {
        return firstTime;
    }

    @Override
    public boolean isTerminated() {
        return scheduledExecutorService != null && scheduledExecutorService.isTerminated();
    }

    @Override
    public long getTerminatedTime() {
        return 0;
    }


    @Override
    public boolean isCompleted() {
        return isTerminated();
    }

    @Override
    public long getCompletedTime() {
        return 0;
    }

    @Override
    public void shutdown() {
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdownNow();
        }
    }

    public void setSolrServer(SolrServer solrServer) {
        this.solrServer = solrServer;
    }

    @Override
    public Exception getException() {
        return super.getException() != null ? super.getException() : exception;
    }

    @Override
    public long getCompletedCount() {
        return optimizeCount;
    }

    static int i = 0;

    public static void main(String[] args) throws InterruptedException {

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
//		scheduledExecutorService.execute(new Runnable() {
//			
//			@Override
//			public void run() {

//			}
//		});
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {


            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
//		scheduledExecutorService.shutdownNow();
//		while(!scheduledExecutorService.isTerminated());
        Thread.sleep(5000);
        scheduledExecutorService.shutdown();

    }


}
