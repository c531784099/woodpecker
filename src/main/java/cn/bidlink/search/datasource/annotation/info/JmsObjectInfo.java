package cn.bidlink.search.datasource.annotation.info;

import java.util.HashMap;
import java.util.Map;

import javax.jms.Message;

import cn.bidlink.search.datasource.DataConverter;

/**
 * JMS信息映射
 * 
 * @author one of search team
 * */
public class JmsObjectInfo {
	
	static final Map<Class<?>, JmsObjectInfo> jmsObjectInfos = new HashMap<Class<?>, JmsObjectInfo>();
	
	private String name;
	private Class<? extends DataConverter<Message, ?>> converter;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Class<? extends DataConverter<Message, ?>> getConverter() {
		return converter;
	}
	public void setConverter(Class<? extends DataConverter<Message, ?>> converter) {
		this.converter = converter;
	}
	public static Map<Class<?>, JmsObjectInfo> getJmsobjectinfos() {
		return jmsObjectInfos;
	}
}
