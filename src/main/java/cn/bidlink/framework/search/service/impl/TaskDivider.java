package cn.bidlink.framework.search.service.impl;

import cn.bidlink.framework.exception.GeneralException;
import cn.bidlink.framework.search.engine.DatabaseDialect;
import cn.bidlink.framework.search.engine.DocumentField;
import cn.bidlink.framework.search.util.JdbcUtils;
import cn.bidlink.framework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TaskDivider {

    private long divideSize;

    private DocumentField uniqueField;

    private String tableName;

    private DatabaseDialect databaseDialect;

    private String contentQueryString;
    
    private String divideQueryString;

    public static final String START_ID = ":startId";

    public static final String END_ID = ":endId";

    public List<String> divide() {
        // 都不允许空
        if (uniqueField == null
                || !StringUtils.hasLength(tableName)
                || databaseDialect == null) {
            throw new GeneralException("dataSource, uniqueField,tableName or databaseDialect ");
        }
        if (divideSize < 1) {
            divideSize = 1000000;
        }
        Connection connection = null;
        Statement statement = null;
        List<String[]> boundKeys = null;
        try {
            connection = databaseDialect.getConnection();
            statement = connection.createStatement();
            boundKeys = getBoundKeys(statement);
            connection.commit();
        } catch (Exception e) {
        	try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
            throw new GeneralException("divide task error", e);
        } finally {
            try {
                if (statement != null && !statement.isClosed()) {
                    statement.close();
                }
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                throw new GeneralException("fetch index data error :[" + this + "]", e);
            }
        }

        if (CollectionUtils.isEmpty(boundKeys)) {
            return null;
        }
        List<String> dividedQueryString = new ArrayList<String>();
        if(contentQueryString.contains("[") && contentQueryString.contains("]")){
        	contentQueryString = contentQueryString.replace("[", " ");
        	contentQueryString = contentQueryString.replace("]", " ");
        	contentQueryString = contentQueryString + " and "+tableName+"."+uniqueField.getName()+">="+START_ID+" and "+tableName+"."+uniqueField.getName()+"<="+END_ID;
        }else{
        	contentQueryString = contentQueryString + " where "+tableName+"."+uniqueField.getName()+">="+START_ID+" and "+tableName+"."+uniqueField.getName()+"<="+END_ID;
        }
        for (int i = 0; i < boundKeys.size(); i++) {
            String[] boundKey = boundKeys.get(i);
            String s = contentQueryString.replaceFirst(START_ID, boundKey[0]).replaceFirst(END_ID, boundKey[1]);
            dividedQueryString.add(s);
        }
        return dividedQueryString;
    }

    private List<String[]> getBoundKeys(Statement statement) throws Exception {
        List<Long[]> bounds = getBoundIndexes(statement);
        List<String[]> ids = new ArrayList<String[]>();
        if (bounds.size() == 0) {
            ids.add(new String[]{"-1", Long.MAX_VALUE + ""});
            return ids;
        }
        String divideSql = databaseDialect.getDivideString(divideQueryString, tableName, uniqueField.getColumnNames(), bounds);
        ResultSet rs = statement.executeQuery(divideSql);
        while (rs.next()) {
            ids.add(new String[]{JdbcUtils.safeValue(rs, 1, String.class),
                    JdbcUtils.safeValue(rs, 2, String.class)});
        }
        rs.close();
        return ids;
    }

    private List<Long[]> getBoundIndexes(Statement statement) throws Exception {
        // 获取总数
        /*
        String countSql = " select count(" + uniqueField.getColumnNames() + ") " +
                " from " + tableName +
                " order by " + uniqueField.getColumnNames() + " asc ";
        */
    	//String countSql = " select count(*) " + " from (" + contentQueryString + ") as other";
    	String countSql = databaseDialect.getCountStr(divideQueryString);
        ResultSet rs = statement.executeQuery(countSql);
        long count = 0;
        if (rs.next()) {
            count = rs.getInt(1);
        }
        rs.close();
        List<Long[]> dividePoints = new ArrayList<Long[]>();
        // 如果不满足分割要求，则范围从第一个到最后一个
        if (count == 0) {
            return dividePoints;
        }
        if (count <= divideSize) {
            dividePoints.add(new Long[]{1L, count});
        } else {
            // 总数小于分割大小也无需分割
            // 获取分割点行号，不包含头尾，如：1,2,3 表示了4段，即：<=1,1<&&<=2,2<&&<=3,3>
            long divideLength = count / divideSize;
            for (long i = 1; i <= divideLength; i++) {
                if (i == 1) {
                    dividePoints.add(new Long[]{1L, divideSize});
                } else {
                    dividePoints.add(new Long[]{(i - 1) * divideSize + 1, i * divideSize});
                }
            }
            if (count % divideSize != 0) {
                dividePoints.add(new Long[]{divideLength * divideSize + 1, count});
            }
        }
        return dividePoints;
    }

    public void setDivideSize(long divideSize) {
        this.divideSize = divideSize;
    }
    public void setUniqueField(DocumentField uniqueField) {
        this.uniqueField = uniqueField;
    }
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    public void setDatabaseDialect(DatabaseDialect databaseDialect) {
        this.databaseDialect = databaseDialect;
    }
    public void setContentQueryString(String contentQueryString) {
        this.contentQueryString = contentQueryString;
    }
	public String getDivideQueryString() {
		return divideQueryString;
	}
	public void setDivideQueryString(String divideQueryString) {
		this.divideQueryString = divideQueryString;
	}
}
