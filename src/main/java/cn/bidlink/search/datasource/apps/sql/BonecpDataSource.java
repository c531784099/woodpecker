package cn.bidlink.search.datasource.apps.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.jolbox.bonecp.BoneCP;

import cn.bidlink.framework.exception.GeneralException;
import cn.bidlink.framework.search.engine.connection.BonecpConnectionPoolFactory;
import cn.bidlink.framework.search.engine.connection.DBConnectionPoolFactory;

public class BonecpDataSource extends SqlDataSource{
	
	@Override
	public Connection openConnection() {
		Properties connectProperties = new Properties();
		connectProperties.setProperty("user", username);
		connectProperties.setProperty("password", password);
		int i = 0;
		while(true){
			try {
				DBConnectionPoolFactory<BoneCP> poolFactory = BonecpConnectionPoolFactory.getInstance(false);
				BoneCP pool = poolFactory.getConnectionPool(driverInstance, url, connectProperties);
				//总数不等于0，可用的等于0时等待
				while(pool.getTotalFree() == 0 && pool.getTotalCreatedConnections() != 0){
					try {
						Thread.sleep(5000L);
						logger.info("数据库连接池中没有连接，当前线程进入睡眠状态");
					} catch (InterruptedException ie) {
						ie.printStackTrace();
					}
				}
				return pool.getConnection();
			} catch (SQLException e) {
				if(i == 5){
					throw new GeneralException("create jdbc connection [ " + driver + " ] failed "+5, e);
				}
				i++;
				logger.error("create jdbc connection [ " + driver + " ] failed", e);
				logger.error("当前线程等待5s");
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

}
