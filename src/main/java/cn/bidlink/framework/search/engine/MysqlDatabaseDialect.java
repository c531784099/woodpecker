package cn.bidlink.framework.search.engine;

import java.util.List;

import cn.bidlink.framework.search.service.impl.TaskDivider;

public class MysqlDatabaseDialect extends AbstractDatabaseDialect {
	
	@Override
	public String getTimeFormat(String time) {
		return "'"+time+"'";
	}

	@Override
	public String getCurrentTime() {
		return "now()";
	}

	@Override
	public String getCountStr(String queryStr) {
		return "select count(*) " + " from (" + queryStr + ") as other";
	}

	@Override
	public String getLimitString(String sortBy, String sortType, String queryString, int firstIndex, int size) {
		String sql = null;
		if(sortBy != null){
			sql = queryString + " order by " + sortBy + " " + sortType + " limit " + firstIndex + "," + size;
		}else{
			sql = queryString + " limit " + firstIndex + "," + size;
		}
		return sql;
	}
	
	@Override
	public String getDivideString(String divideQueryString, String tableName, String primaryKey, List<Long[]> bounds) {
		StringBuffer s = new StringBuffer(); 
		for(int i = 0 ; i < bounds.size(); i++) {
			Long[] bound = bounds.get(i);
			/*
			s.append(" select min(" + primaryKey + ") startKey, max(" + primaryKey + ") endKey from (");
			s.append(" 		select * from ( ");
			s.append("			select " + primaryKey + " from " + tableName + " order by " + primaryKey + " asc ");
			s.append("		) t limit " + (bound[0] - 1) + "," + (bound[1] - bound[0] + 1));
			s.append(" ) t" + i);
			*/
			String divideQueryStringNew = "select min(id), max(id) from (" + divideQueryString + " limit "+TaskDivider.START_ID + "," + TaskDivider.END_ID +") as other";
			s.append(divideQueryStringNew.replaceFirst(TaskDivider.START_ID, ""+(bound[0]-1)).replaceFirst(TaskDivider.END_ID, ""+(bound[1] - bound[0] + 1)));
			if(i < bounds.size() - 1) {
				s.append(" \n union \n");
			}
		}
		return s.toString();
	}
	
	@Override
	public String setUrl() {
		return this.url;
	}

	@Override
	public String setUser() {
		return this.user;
	}
}
