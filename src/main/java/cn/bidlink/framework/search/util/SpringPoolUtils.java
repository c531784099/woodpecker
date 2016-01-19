package cn.bidlink.framework.search.util;

import org.springframework.context.ApplicationContext;

public class SpringPoolUtils {
	
	public static ApplicationContext applicationContext;
	
	public static final Object getBean(Class<?> clazz){
		return applicationContext.getBean(clazz);
	}
	
	public static final Object getBean(String name){
		return applicationContext.getBean(name);
	}

}
