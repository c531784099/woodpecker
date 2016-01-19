package cn.bidlink.search.datasource;

/**
 * 数据转换器
 * 
 * @author one of search team
 * */
public interface DataConverter<DTF, DTT> {

	/**
	 * 数据转换
	 * */
	public DTT convert(DTF data, Class<DTT> clazz);

}
