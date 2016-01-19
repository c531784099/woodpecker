package cn.bidlink.framework.search.engine;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface FieldConvertor {

    public Object convert(DocumentField field, ResultSet resultSet) throws SQLException;

}
