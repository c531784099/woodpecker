package cn.bidlink.framework.search.engine.connection;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/**
 * 数据库连接池工厂类
 * 
 * @author 刘佳明
 * */
public interface DBConnectionPoolFactory<P> {
	
	/**
	 * 获得动态连接池
	 * */
	public Map<String, P> getPools();
	
	/**
	 * 由连接池获得连接
	 * */
	public P getConnectionPool(Driver driverInstance, String url, Properties connectProperties) throws SQLException;

	/**
	 * 关闭连接池并销毁
	 * */
	public void closeConnectionPool(String url, String user, String password) throws SQLException;
	
}
