package cn.bidlink.framework.search.engine.connection;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

/**
 * BoneCP数据库连接池工厂实现
 * 
 * @author 刘佳明
 * */
public class BonecpConnectionPoolFactory implements DBConnectionPoolFactory<BoneCP>{
	
	public static final Logger logger = Logger.getLogger(BonecpConnectionPoolFactory.class);
	
	/**采用单利实现是因为如果配置的不用BoneCP则不会加载器配置等信息*/
	private static BonecpConnectionPoolFactory poolFactory = null;
	/**BoneCP动态连接池，KEY为标识唯一数据库连接池，生成规则getPoolKey()*/
	private static Hashtable<String, BoneCP> pools = null;
	private static BonecpConnectionPoolWatcher watcher;
	
	public static synchronized BonecpConnectionPoolFactory getInstance(){
		return getInstance(true);
	}
	
	public static synchronized BonecpConnectionPoolFactory getInstance(boolean isWatcher){
		if(poolFactory == null){
			logger.debug("BonecpConnectionPoolFactory init...");
			poolFactory = new BonecpConnectionPoolFactory();
			pools = new Hashtable<String, BoneCP>();
			if(isWatcher){
				if(watcher == null){
					watcher = new BonecpConnectionPoolWatcher();
				}
				watcher.execute();
			}
		}
		return poolFactory;
	}

	@Override
	public Map<String, BoneCP> getPools() {
		return pools;
	}

	@Override
	public BoneCP getConnectionPool(Driver driverInstance, String url, Properties connectProperties) throws SQLException {
		String poolId = getPoolId(url, connectProperties);
		if(!pools.containsKey(poolId)){
			logger.debug("bonecp pool["+poolId+"] is not live, create...");
			BoneCPConfig config = new BoneCPConfig();
			config.setJdbcUrl(url);
			config.setUsername(connectProperties.getProperty("user"));
			config.setPassword(connectProperties.getProperty("password"));
			BoneCP boneCp = new BoneCP(config);
			pools.put(poolId, boneCp);
			return boneCp;
		}
		return pools.get(poolId);
	}
	
	@Override
	public synchronized void closeConnectionPool(String url, String user, String password) throws SQLException{
		String poolId = getPoolId(url, user, password);
		if(pools.containsKey(poolId)){
			pools.get(poolId).shutdown();
			pools.remove(poolId);
		}
	}
	
	/**
	 * 根据连接池ID判断连接池是否没有可用连接
	 * */
	public synchronized boolean isPoolEmpty(String poolId){
		return pools.get(poolId).getTotalFree() == 0;
	}
	
	/**
	 * 动态数据库连接池唯一标识ID
	 * */
	public static String getPoolId(String url, Properties connectProperties){
		return getPoolId(url, connectProperties.getProperty("user"), connectProperties.getProperty("password"));
	}
	public static String getPoolId(String url, String user, String password){
		return url + "-/" + user + "-/" + password;
	}
	public static BoneCP getBoneCPById(String poolId){
		return pools.get(poolId);
	}
}
