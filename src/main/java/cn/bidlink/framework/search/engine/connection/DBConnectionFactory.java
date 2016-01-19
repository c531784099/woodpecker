package cn.bidlink.framework.search.engine.connection;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.jolbox.bonecp.BoneCP;

import cn.bidlink.framework.util.holder.PropertiesHolder;

/**
 * 数据库连接工厂类
 * 
 * @author 刘佳明
 * */
public class DBConnectionFactory {
	
	public static final Logger logger = Logger.getLogger(DBConnectionFactory.class);
	
	public static final String CONNECTION_GETMETHOD_KEY = "db.connection.get.method";
	public static final String CONNECTION_REGETNUM_KEY = "db.connection.get.regetNum";
	
	public static Connection getConnection(Driver driverInstance, String url, Properties connectProperties) throws SQLException {
		if(PropertiesHolder.getInt(DBConnectionFactory.CONNECTION_GETMETHOD_KEY, 2) == 1){
			return driverInstance.connect(url, connectProperties);
		}else{
			DBConnectionPoolFactory<BoneCP> poolFactory = BonecpConnectionPoolFactory.getInstance();
			BoneCP pool = poolFactory.getConnectionPool(driverInstance, url, connectProperties);
			//总数不等于0，可用的等于0时等待
			while(pool.getTotalFree() <= 0 && pool.getTotalCreatedConnections() >=50){
				try {
					Thread.sleep(1000L * 60 * 5);
					logger.info("bonecp pool["+pool.getConfig().getJdbcUrl()+"][totalLeased/totalCreated="+pool.getTotalLeased()+"/"+pool.getTotalCreatedConnections()+"] has no connections, thread sleep 5s");
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			}
			return pool.getConnection();
		}
	}
	
	public static void shutdownConnectionPool(String url, String user, String password) throws SQLException {
		if(PropertiesHolder.getInt(DBConnectionFactory.CONNECTION_GETMETHOD_KEY, 2) == 1){
			//do nothing
		}else{
			DBConnectionPoolFactory<BoneCP> poolFactory = BonecpConnectionPoolFactory.getInstance();
			poolFactory.closeConnectionPool(url, user, password);
		}
	}
	
}
