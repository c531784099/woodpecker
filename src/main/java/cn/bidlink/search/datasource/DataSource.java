package cn.bidlink.search.datasource;

/**
 * DataSource你懂的
 * 
 * @author one of search team
 * */
public interface DataSource<C> {
	
	/**
	 * 打开一个连接
	 * */
	public C openConnection();
	
	public void recieveAndSendTo(Class<?> clazz, DataSource<?> dataSource);
	
	public void send(Object data);
}
