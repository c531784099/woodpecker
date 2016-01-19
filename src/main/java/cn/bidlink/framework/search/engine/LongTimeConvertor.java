package cn.bidlink.framework.search.engine;

import cn.bidlink.framework.search.util.JdbcUtils;
import cn.bidlink.framework.util.DateUtils;
import org.apache.commons.lang.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class LongTimeConvertor implements FieldConvertor {

    @Override
    public Object convert(DocumentField field, ResultSet resultSet) throws SQLException {
        String dateString = (String) JdbcUtils.safeValue(resultSet, field.getColumnNames(), field.getType());
        if (StringUtils.isBlank(dateString)) {
            return null;
        }
        Date date = DateUtils.parse(dateString);
        long longTime = DateUtils.getFormatedTime(date);
        return longTime;
    }

}
