package cn.bidlink.search.datasource.apps.sql;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

import org.apache.log4j.Logger;

import cn.bidlink.search.datasource.DataSource;
import cn.bidlink.search.datasource.annotation.info.SqlObjectInfo;

public abstract class SqlDataSource implements DataSource<Connection>{

	protected final Logger logger = Logger.getLogger(getClass());
	
	protected String driver;
	protected String url;
	protected String username;
	protected String password;
	protected Driver driverInstance;

	@Override
	public void recieveAndSendTo(Class<?> clazz, DataSource<?> dataSource) {
		// TODO Auto-generated method stub
	}

	@Override
	public void send(Object data) {
		if(data != null){
			SqlObjectInfo sqlObjectInfo = SqlObjectInfo.getSqlobjectinfos().get(data.getClass());
			Connection connection = openConnection();
			PreparedStatement pstatement = null;
			try {
				Field pkField = data.getClass().getDeclaredField(sqlObjectInfo.getPk());
				pkField.setAccessible(true);
				Object pkValue = pkField.get(data);
				if(pkValue != null){
					pstatement = connection.prepareStatement(sqlObjectInfo.getSqlUpdate());
				}else{
					pstatement = connection.prepareStatement(sqlObjectInfo.getSqlInsert());
				}
				System.out.println(sqlObjectInfo.getSqlInsert());
				int i=1;
				for(String fieldName : sqlObjectInfo.getColumnInfo().keySet()){
					if(!(fieldName.equals(sqlObjectInfo.getPk()) && sqlObjectInfo.getSequence() != null)){
						Field field = data.getClass().getDeclaredField(fieldName);
						field.setAccessible(true);
						setParams(pstatement, i, field.getType(), field.get(data));
						i ++;
					}
				}
				if(pkValue != null){
					pstatement.setObject(i, pkValue);
				}
				pstatement.execute();
				connection.commit();
			} catch (Exception e) {
				e.printStackTrace();
				try {
					connection.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			} finally{
				try {
					if(pstatement != null){
						pstatement.close();
					}
					if(connection != null){
						connection.close();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	public static final void setParams(PreparedStatement pstatement, int index, Class<?> type, Object value) throws SQLException{
		if(value == null){
			pstatement.setObject(index, null);
		}else if(type.equals(String.class)){
			pstatement.setString(index, (String)value);
		}else if(type.equals(Integer.class)){
			pstatement.setInt(index, (Integer)value);
		}else if(type.equals(Long.class)){
			pstatement.setLong(index, (Long)value);
		}else if(type.equals(Date.class)){
			pstatement.setDate(index, new java.sql.Date(((Date)value).getTime()));
		}
	}

	public String getDriver() {
		return driver;
	}
	public void setDriver(String driver) {
		this.driver = driver;
		try {
			this.driverInstance = (Driver) Class.forName(driver).newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
}
