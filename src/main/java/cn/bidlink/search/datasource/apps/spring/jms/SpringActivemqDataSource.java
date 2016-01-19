package cn.bidlink.search.datasource.apps.spring.jms;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

public class SpringActivemqDataSource extends SpringJmsDataSource {
	
	private JmsTemplate jmsTemplate;
	
	public void init(){
		jmsTemplate = new JmsTemplate(new ActiveMQConnectionFactory(username, password, brokerUrl));
	}

	@Override
	public JmsTemplate openConnection() {
		return jmsTemplate;
	}
}
