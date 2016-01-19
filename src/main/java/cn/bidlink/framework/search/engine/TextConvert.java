package cn.bidlink.framework.search.engine;

import cn.bidlink.framework.search.util.JdbcUtils;
import cn.bidlink.framework.search.util.StringConvertUtils;
import org.apache.commons.lang.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TextConvert implements FieldConvertor {

    @Override
    public Object convert(DocumentField field, ResultSet resultSet)
            throws SQLException {
        String text = (String) JdbcUtils.safeValue(resultSet, field.getColumnNames(), field.getType());
        if (StringUtils.isBlank(text)) {
            return null;
        }
        text = StringUtils.trim(text);
        text = StringConvertUtils.leachHTML(text);
        text = StringConvertUtils.textAbstract(text);
        
        return text;
    }

}
