package cn.bidlink.framework.search.util;

import cn.bidlink.framework.exception.GeneralException;
import cn.bidlink.framework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public abstract class JdbcUtils {

    @SuppressWarnings("unchecked")
    public static <T> T safeValue(ResultSet rs, String name, Class<T> columnType) {
        if (!StringUtils.hasLength(name)) {
            return null;
        }
        name = name.trim();

        try {
            if (columnType.equals(Byte.class)) {
                return (T) new Byte(rs.getByte(name));
            } else if (columnType.equals(Integer.class)) {
                return (T) new Integer(rs.getInt(name));
            } else if (columnType.equals(Long.class)) {
                return (T) new Long(rs.getLong(name));
            } else if (columnType.equals(Float.class)) {
                return (T) new Float(rs.getFloat(name));
            } else if (columnType.equals(Double.class)) {
                return (T) new Double(rs.getDouble(name));
            } else if (columnType.equals(Boolean.class)) {
            	//据说new Boolean对象非常消耗资源
                //return (T) new Boolean(rs.getBoolean(name));
            	return (T) Boolean.valueOf(rs.getBoolean(name));
            	//return (T) (rs.getBoolean(name) ?  Boolean.TRUE : Boolean.FALSE);
            } else if (columnType.equals(Character.class)) {
            	//多此一举
                //return (T) new String(rs.getString(name));
            	return (T) rs.getString(name);
            } else if (columnType.equals(String.class)) {
            	//多此一举
                //String namestr =  rs.getString(name);
                //return (T) new String(namestr);
                return (T) rs.getString(name);
            } else if (columnType.equals(Timestamp.class)) {
                return (T) rs.getTimestamp(name);
            } else if (Date.class.isAssignableFrom(columnType)) {
                return (T) rs.getDate(name);
            } else {
                return (T) rs.getObject(name);
            }
        } catch (SQLException e) {
            throw new GeneralException("jdbc fetch data error", e);
        } catch (NullPointerException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T safeValue(ResultSet rs, int columnIndex, Class<T> columnType) {
        try {
            if (columnType.equals(Byte.class)) {
                return (T) new Byte(rs.getByte(columnIndex));
            } else if (columnType.equals(Integer.class)) {
                return (T) new Integer(rs.getInt(columnIndex));
            } else if (columnType.equals(Long.class)) {
                return (T) new Long(rs.getLong(columnIndex));
            } else if (columnType.equals(Float.class)) {
                return (T) new Float(rs.getFloat(columnIndex));
            } else if (columnType.equals(Double.class)) {
                return (T) new Double(rs.getDouble(columnIndex));
            } else if (columnType.equals(Boolean.class)) {
                return (T) new Boolean(rs.getBoolean(columnIndex));
            } else if (columnType.equals(Character.class)) {
                return (T) new String(rs.getString(columnIndex));
            } else if (columnType.equals(String.class)) {
                return (T) new String(rs.getString(columnIndex));
            } else if (columnType.equals(Timestamp.class)) {
                return (T) rs.getTimestamp(columnIndex);
            } else if (Date.class.isAssignableFrom(columnType)) {
                return (T) rs.getDate(columnIndex);
            } else {
                return (T) rs.getObject(columnIndex);
            }
        } catch (SQLException e) {
            throw new GeneralException("jdbc fetch data error", e);
        } catch (NullPointerException e) {
            return null;
        }
    }
}
