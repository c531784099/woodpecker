package cn.bidlink.framework.search.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.bidlink.framework.search.data.creditScore.CreditScore;
import cn.bidlink.search.datasource.annotation.info.ObjectInfoFactory;
import cn.bidlink.search.datasource.apps.spring.jms.SpringJmsDataSource;
import cn.bidlink.search.datasource.apps.sql.SqlDataSource;

public class TomcatStartedExecuteListener implements ServletContextListener{

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		/**获取诚信值*/
		new CreditScoreGetThread().start();
	}
	
	class CreditScoreGetThread extends Thread{
		@Override
		public void run() {
			ApplicationContext xt = new ClassPathXmlApplicationContext("applicationContext-ds.xml");
			//这里用于加载数据，不要删除
			ObjectInfoFactory objectInfoFactory = (ObjectInfoFactory)xt.getBean("objectInfoFactory");
			SpringJmsDataSource creditScoreJmsDataSource = (SpringJmsDataSource)xt.getBean("creditScoreJmsDataSource");
			SqlDataSource creditScoreSqlDataSource = (SqlDataSource)xt.getBean("creditScoreSqlDataSource");
			creditScoreJmsDataSource.recieveAndSendTo(CreditScore.class, creditScoreSqlDataSource);
		}
	}

}
