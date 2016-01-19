package cn.bidlink.framework.search.engine.connection;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import cn.bidlink.framework.util.holder.PropertiesHolder;

import com.jolbox.bonecp.BoneCP;

public class BonecpConnectionPoolWatcher implements Runnable{

	public final Logger logger = Logger.getLogger(BonecpConnectionPoolWatcher.class);
	
	public static final String DBCONNECTIONWATCHER_PROPERTIES_ISEXECUTE_KEY = "watcher.dbConnectionWatcher.isExecute";
	public static final String DBCONNECTIONWATCHER_PROPERTIES_PREIOD_KEY = "watcher.dbConnectionWatcher.period";
	
	private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

	public boolean isExecute() {
		return PropertiesHolder.getBoolean(DBCONNECTIONWATCHER_PROPERTIES_ISEXECUTE_KEY, false);
	}
	
	public void execute() {
		if(isExecute() && scheduledExecutorService != null){
		    scheduledExecutorService.scheduleAtFixedRate(this, 0, 30, TimeUnit.SECONDS);
		}
	}
	
	public void shutdown(){
		if(scheduledExecutorService != null){
			scheduledExecutorService.shutdown();
		}
	}

	@Override
	public void run() {
		Map<String, BoneCP> pools = BonecpConnectionPoolFactory.getInstance().getPools();
		if(pools != null){
			for(String key : pools.keySet()){
				BoneCP boneCP = pools.get(key);
				System.out.println("bonecp pool["+key+"]'s totalCreated="+boneCP.getTotalCreatedConnections()+",totalLeased="+boneCP.getTotalLeased()+",totalFree="+boneCP.getTotalFree());
			}
		}
	}
}
