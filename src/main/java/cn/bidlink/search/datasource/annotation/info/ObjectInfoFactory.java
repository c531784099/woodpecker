package cn.bidlink.search.datasource.annotation.info;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.beanguo.commons.reflect.ClassUtils;

import cn.bidlink.search.datasource.annotation.JmsObject;
import cn.bidlink.search.datasource.annotation.SqlColumn;
import cn.bidlink.search.datasource.annotation.SqlObject;
import cn.bidlink.search.datasource.annotation.SqlPk;
import cn.bidlink.search.datasource.annotation.SqlSequence;

/**
 * 映射信息工厂
 * 
 * @author one of search team
 * */
public class ObjectInfoFactory {
	
	private String packageToScan;
	
	public void init(){
		@SuppressWarnings("unchecked")
		final Class<? extends Annotation>[] annotationClasses = new Class[]{JmsObject.class, SqlObject.class};
		for(Class<?> clazz : ClassUtils.findAnnotationClassesInPackage(packageToScan, annotationClasses)){
			/**JMS*/
			JmsObject jmsObject = clazz.getAnnotation(JmsObject.class);
			if(jmsObject != null){
				JmsObjectInfo jmsObjectInfo = new JmsObjectInfo();
				jmsObjectInfo.setName(jmsObject.name());
				jmsObjectInfo.setConverter(jmsObject.converter());
				JmsObjectInfo.jmsObjectInfos.put(clazz, jmsObjectInfo);
			}
			/**SQL*/
			SqlObject sqlObject = clazz.getAnnotation(SqlObject.class);
			if(sqlObject != null){
				SqlObjectInfo sqlObjectInfo = new SqlObjectInfo();
				sqlObjectInfo.setName(sqlObject.name());
				Map<String, String> columnInfo = new HashMap<String, String>();
				for(Field field : clazz.getDeclaredFields()){
					field.setAccessible(true);
					SqlPk sqlPk = field.getAnnotation(SqlPk.class);
					if(sqlPk != null){
						sqlObjectInfo.setPk(field.getName());
						SqlSequence sqlSequence = field.getAnnotation(SqlSequence.class);
						if(sqlSequence != null){
							sqlObjectInfo.setSequence(sqlSequence.name());
						}
					}
					SqlColumn sqlColumn = field.getAnnotation(SqlColumn.class);
					if(sqlColumn != null){
						columnInfo.put(field.getName(), sqlColumn.name());
					}
				}
				sqlObjectInfo.setColumnInfo(columnInfo);
				//加载预编译SQL
				sqlObjectInfo.setSqlInsert(getInsertSqlBySqlObjectInfo(sqlObjectInfo));
				sqlObjectInfo.setSqlUpdate(getUpdateSqlBySqlObjectInfo(sqlObjectInfo));
				SqlObjectInfo.sqlObjectInfos.put(clazz, sqlObjectInfo);
			}
		}
	}
	
	public static final String getInsertSqlBySqlObjectInfo(SqlObjectInfo sqlObjectInfo){
		StringBuilder sqlInsert = new StringBuilder("INSERT INTO "+sqlObjectInfo.getName()+"(");
		StringBuilder sqlTemp = new StringBuilder();
		int i=1;
		for(String fileName : sqlObjectInfo.getColumnInfo().keySet()){
			sqlInsert.append(sqlObjectInfo.getColumnInfo().get(fileName));
			if(fileName.equals(sqlObjectInfo.getPk()) && sqlObjectInfo.getSequence() != null){
				sqlTemp.append(sqlObjectInfo.getSequence());
			}else{
				sqlTemp.append("?");
			}
			if(i != sqlObjectInfo.getColumnInfo().size()){
				sqlInsert.append(",");
				sqlTemp.append(",");
			}
			i ++;
		}
		sqlInsert.append(")VALUES(").append(sqlTemp).append(")");
		return sqlInsert.toString();
	}
	public static final String getUpdateSqlBySqlObjectInfo(SqlObjectInfo sqlObjectInfo){
		StringBuilder sqlUpdate = new StringBuilder("UPDATE "+sqlObjectInfo.getName()+" SET ");
		int i=1;
		for(String fileName : sqlObjectInfo.getColumnInfo().keySet()){
			sqlUpdate.append(sqlObjectInfo.getColumnInfo().get(fileName)).append("=").append("?");
			if(i != sqlObjectInfo.getColumnInfo().size()){
				sqlUpdate.append(",");
			}
			i ++;
		}
		sqlUpdate.append(" WHERE ").append(sqlObjectInfo.getPk()).append("=").append("?");
		return sqlUpdate.toString();
	}
	
	public String getPackageToScan() {
		return packageToScan;
	}
	public void setPackageToScan(String packageToScan) {
		this.packageToScan = packageToScan;
	}
}
