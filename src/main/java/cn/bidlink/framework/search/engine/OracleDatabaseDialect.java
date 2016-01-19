package cn.bidlink.framework.search.engine;

import java.util.List;

import cn.bidlink.framework.search.service.impl.TaskDivider;

public class OracleDatabaseDialect extends AbstractDatabaseDialect {
	
	@Override
	public String getTimeFormat(String time) {
		return "to_date('"+time+"','yyyy-mm-dd hh24:mi:ss')";
	}

	@Override
	public String getCurrentTime() {
		return "sysdate";
	}

	@Override
	public String getCountStr(String queryStr) {
		return "select count(*) " + " from (" + queryStr + ")";
	}

	@Override
	public String getLimitString(String sortBy, String sortType, String queryString, int firstIndex, int size) {
		String s = null;
		if(sortBy != null){
			if(firstIndex <= 0) {
				s = " select temp.* from (" + queryString + " order by "+sortBy+ " " + sortType + ") temp where rownum <= " + size; 
			} else {
				s = " select * from ( " +
						" select temp.*,rownum primitive_rownum from (" + queryString + " order by "+sortBy+" " + sortType +") temp where rownum <= " + (firstIndex + size) +
					" ) where primitive_rownum > " + firstIndex;
			}
		}else{
			if(firstIndex <= 0) {
				s = " select temp.* from (" + queryString + ") temp where rownum <= " + size; 
			} else {
				s = " select * from ( " +
						" select temp.*,rownum primitive_rownum from (" + queryString + ") temp where rownum <= " + (firstIndex + size) +
					" ) where primitive_rownum > " + firstIndex;
			}
		}
		return s;
	}
	
	@Override
	public String getDivideString(String divideQueryString, String tableName, String primaryKey, List<Long[]> bounds) {
		
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < bounds.size(); i++) {
			Long[] bound = bounds.get(i);
			/*
			s.append(" select min(" + primaryKey + "), max(" + primaryKey + ") from ( ");
			s.append(" 	select " + primaryKey + ", rownum primitive_rownum from ( ");
			s.append(" 		select " + primaryKey + " from " + tableName +" order by " + primaryKey + " asc) where rownum <= " + bound[1]);
			s.append(" ) where primitive_rownum >= " + bound[0]);
			*/
			String tempStr = "select min(id), max(id) from (select id, rownum primitive_rownum from (" + divideQueryString +") where rownum<="+TaskDivider.END_ID+") where primitive_rownum>="+TaskDivider.START_ID;
			s.append(tempStr.replaceFirst(TaskDivider.START_ID, ""+bound[0]).replaceFirst(TaskDivider.END_ID, ""+bound[1]));
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
