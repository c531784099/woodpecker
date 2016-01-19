package cn.bidlink.framework.search.engine;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.jamonapi.proxy.MonProxyFactory;

import cn.bidlink.framework.exception.GeneralException;
import cn.bidlink.framework.search.Constants;
import cn.bidlink.framework.search.engine.connection.DBConnectionFactory;
import cn.bidlink.framework.util.StringUtils;
import cn.bidlink.framework.util.holder.PropertiesHolder;

/**
 * 数据库方言抽象类
 * 封装不同数据库之间的相同属性，实例化Connection
 * */
public abstract class AbstractDatabaseDialect implements DatabaseDialect{
	
	private final Logger logger = Logger.getLogger(getClass());
	
	private String dirver;
	
	private Driver driverInstance;
	
	private Properties connectProperties;
	
	protected String url;
	
	protected String user;
	
	protected String password;
	
	@Override
	public Connection getConnection() {
		if(!StringUtils.hasLength(url)
		   || !StringUtils.hasLength(user)) {
			throw new GeneralException("url, user or password is empty");
		}
		if(driverInstance == null) {
			if(!StringUtils.hasLength(dirver)) {
				throw new GeneralException(" driver is empty");
			}
			try {
				driverInstance = (Driver) Class.forName(dirver).newInstance();
			} catch (Exception e) {
				throw new GeneralException("create jdbc driver [ " + dirver + " ] failed", e);
			}
		}
		if(connectProperties == null) {
			connectProperties = new Properties();
			connectProperties.put("user", user);
			connectProperties.put("password", password);
		}
		int i = 0;
		int j = PropertiesHolder.getInt(DBConnectionFactory.CONNECTION_REGETNUM_KEY, 5);
		while(true){
			try {
				if(PropertiesHolder.getBoolean(Constants.JAMON_GATE_ISOPEN, false)){
					return MonProxyFactory.monitor(DBConnectionFactory.getConnection(driverInstance, url, connectProperties));
				}else{
					return DBConnectionFactory.getConnection(driverInstance, url, connectProperties);
				}
			} catch (SQLException e) {
				if(i == j){
					throw new GeneralException("create jdbc connection [ " + dirver + " ] failed "+j, e);
				}
				i++;
				logger.error("create jdbc connection [ " + dirver + " ] failed", e);
				logger.error("当前线程等待5s");
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				//throw new GeneralException();
			}
		}
	}
	
	public void setDriver(String driver) {
		this.dirver = driver;
	}

	@Override
	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public void setUser(String user) {
		this.user = user;
	}

	@Override
	public void setPassword(String password) {
		this.password = password;
	}

	public String getDirver() {
		return dirver;
	}

	public Driver getDriverInstance() {
		return driverInstance;
	}

	public Properties getConnectProperties() {
		return connectProperties;
	}

	public String getUrl() {
		return url;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}
}