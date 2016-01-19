package cn.bidlink.search.datasource.util;

import java.lang.annotation.Annotation;

public class AnnotationUilts {
	
	/**
	 * 获得类是否有该注解
	 * */
	public static final boolean existsAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClazz){
		return clazz.getAnnotation(annotationClazz) != null;
	}

}
