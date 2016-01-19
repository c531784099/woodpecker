package cn.bidlink.search.datasource.apps.spring.jms;

import javax.jms.Message;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.util.Assert;

import cn.bidlink.search.datasource.DataConverter;
import cn.bidlink.search.datasource.DataSource;
import cn.bidlink.search.datasource.annotation.JmsObject;
import cn.bidlink.search.datasource.util.AnnotationUilts;

public abstract class SpringJmsDataSource implements DataSource<JmsTemplate>{
	
	protected String brokerUrl;
	protected String username;
	protected String password;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void recieveAndSendTo(Class<?> clazz, DataSource<?> dataSource){
		Assert.isTrue(AnnotationUilts.existsAnnotation(clazz, JmsObject.class), "要获取JMS的信息封装，必须声明类["+clazz.getName()+"]为@JmsObject");
		JmsObject jmsObject = clazz.getAnnotation(JmsObject.class);
		JmsTemplate jmsTemplate = openConnection();
		Assert.hasLength(jmsObject.name(), "要获取JMS的信息封装，必须声明类["+clazz.getName()+"]的队列名称(destinationName):@JmsObject(name)");
		DataConverter dataConverter = null;
		if(jmsObject.converter() != null){
			try {
				dataConverter = jmsObject.converter().newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		while(true) {
			Message message = jmsTemplate.receive(jmsObject.name());
			dataSource.send(dataConverter.convert(message, clazz));
		}
	}
	
	@Override
	public void send(Object data) {
		
	}

	public String getBrokerUrl() {
		return brokerUrl;
	}
	public void setBrokerUrl(String brokerUrl) {
		this.brokerUrl = brokerUrl;
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
