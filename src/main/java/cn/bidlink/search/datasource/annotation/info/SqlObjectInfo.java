package cn.bidlink.search.datasource.annotation.info;

import java.util.HashMap;
import java.util.Map;

/**
 * SQL信息映射
 * 
 * @author one of search team
 * */
public class SqlObjectInfo {
	
	static final Map<Class<?>, SqlObjectInfo> sqlObjectInfos = new HashMap<Class<?>, SqlObjectInfo>();
	private String sqlInsert;
	private String sqlUpdate;
	
	private String name;
	private String pk;
	private String sequence;
	private Map<String, String> columnInfo;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPk() {
		return pk;
	}
	public void setPk(String pk) {
		this.pk = pk;
	}
	public Map<String, String> getColumnInfo() {
		return columnInfo;
	}
	public void setColumnInfo(Map<String, String> columnInfo) {
		this.columnInfo = columnInfo;
	}
	public static Map<Class<?>, SqlObjectInfo> getSqlobjectinfos() {
		return sqlObjectInfos;
	}
	public String getSqlInsert() {
		return sqlInsert;
	}
	public void setSqlInsert(String sqlInsert) {
		this.sqlInsert = sqlInsert;
	}
	public String getSqlUpdate() {
		return sqlUpdate;
	}
	public void setSqlUpdate(String sqlUpdate) {
		this.sqlUpdate = sqlUpdate;
	}
	public String getSequence() {
		return sequence;
	}
	public void setSequence(String sequence) {
		this.sequence = sequence;
	}
}
