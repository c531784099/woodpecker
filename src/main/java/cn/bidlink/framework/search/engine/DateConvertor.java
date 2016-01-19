package cn.bidlink.framework.search.engine;

import cn.bidlink.framework.search.util.JdbcUtils;
import cn.bidlink.framework.util.DateUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class DateConvertor implements FieldConvertor {

    @Override
    public Object convert(DocumentField field, ResultSet resultSet) throws SQLException {
        Date date = (Date) JdbcUtils.safeValue(resultSet, field.getColumnNames(), field.getType());
        if (date == null) {
            return null;
        }
        return DateUtils.getFormatedTime(date);
    }

}
