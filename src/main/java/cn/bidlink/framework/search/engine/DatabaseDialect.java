package cn.bidlink.framework.search.engine;

import java.sql.Connection;
import java.util.List;

/**
 * 定义数据库方言操作
 * */
public interface DatabaseDialect {
	
	public String getTimeFormat(String time);
	public String getCurrentTime();
	/**
	 * 计算count值
	 * */
	public String getCountStr(String queryStr);
	
	/**
	 * 驱动类赋值
	 * @param 驱动类完整路径
	 * */
	public void setDriver(String dirver);
	
	/**
	 * 数据库URL赋值
	 * */
	public void setUrl(String url);
	
	/**
	 * 数据库用户名赋值
	 * */
	public void setUser(String user);
	
	/**
	 * 数据库用户密码赋值
	 * */
	public void setPassword(String password);
	
	public String getDirver();
	
	public String setUrl();
	
	public String setUser();
	
	public String getPassword();
	
	/**
	 * 根据以上信息获得数据库连接
	 * */
	public Connection getConnection();

	/**
	 * 获得分页查询语句
	 * @param 查询语句
	 * @param 第一个元素索引
	 * @param 查询的个数
	 * */
	public String getLimitString(String sortBy, String sortType, String queryString, int firstIndex, int size);

	/**
	 * 获得分割主键的结果语句
	 * 通过首元素和尾元素获得首个主键和最后一个主键的结果集，多个首元素和尾元素时连接查询结果
	 * @param 表名
	 * @param 主键名
	 * @param List<Long[]{首元素索引，尾元素索引}>
	 * */
	public String getDivideString(String divideQueryString, String tableName, String primaryKey,
			List<Long[]> bounds);

}
