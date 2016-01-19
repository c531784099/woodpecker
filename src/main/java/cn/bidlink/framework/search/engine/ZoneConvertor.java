package cn.bidlink.framework.search.engine;

import cn.bidlink.framework.search.util.JdbcUtils;
import cn.bidlink.framework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ZoneConvertor implements FieldConvertor {

    private String chinaCode = "1001";

    public void setChinaCode(String chinaCode) {
        this.chinaCode = chinaCode;
    }

    @Override
    public Object convert(DocumentField field, ResultSet resultSet) throws SQLException {
        String[] names = field.getColumnNames().split(",");

        String country = JdbcUtils.safeValue(resultSet, names[0], String.class);
        String area = JdbcUtils.safeValue(resultSet, names[1], String.class);
        String city = JdbcUtils.safeValue(resultSet, names[2], String.class);
        String county = JdbcUtils.safeValue(resultSet, names[3], String.class);
        if (StringUtils.hasLength(country)) {
            if (country.equals(chinaCode)) {
                if (StringUtils.hasLength(county)) {
                    return county;
                } else if (StringUtils.hasLength(city)) {
                    return city;
                } else if (StringUtils.hasLength(area)) {
                    return area;
                }
            }
            return country;
        }
        return null;
    }

}
