package cn.bidlink.search.datasource.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.jms.Message;

import cn.bidlink.search.datasource.DataConverter;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface JmsObject {
	
	public String name();
	public Class<? extends DataConverter<Message, ?>> converter();

}
