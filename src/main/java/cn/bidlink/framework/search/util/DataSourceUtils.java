package cn.bidlink.framework.search.util;

import cn.bidlink.framework.search.engine.MysqlDatabaseDialect;
import cn.bidlink.framework.search.engine.OracleDatabaseDialect;
import cn.bidlink.framework.util.holder.PropertiesHolder;

public abstract class DataSourceUtils {

	public static MysqlDatabaseDialect createMysqlDatabaseDialect() {
		MysqlDatabaseDialect dialect = new MysqlDatabaseDialect();
		dialect.setDriver(PropertiesHolder.getProperty("ebnew.mysql.driver"));
		dialect.setUrl(PropertiesHolder.getProperty("ebnew.mysql.url"));
		dialect.setUser(PropertiesHolder.getProperty("ebnew.mysql.username"));
		dialect.setPassword(PropertiesHolder.getProperty("ebnew.mysql.password"));
		return dialect;
	}
	
	public static MysqlDatabaseDialect createCreditMysqlDatabaseDialect() {
		MysqlDatabaseDialect dialect = new MysqlDatabaseDialect();
		dialect.setDriver(PropertiesHolder.getProperty("creditScore.mysql.driver"));
		dialect.setUrl(PropertiesHolder.getProperty("creditScore.mysql.url"));
		dialect.setUser(PropertiesHolder.getProperty("creditScore.mysql.username"));
		dialect.setPassword(PropertiesHolder.getProperty("creditScore.mysql.password"));
		return dialect;
	}

	public static OracleDatabaseDialect createOracleDatabaseDialect() {
		OracleDatabaseDialect dialect = new OracleDatabaseDialect();
		dialect.setDriver(PropertiesHolder.getProperty("oracle.driver"));
		dialect.setUrl(PropertiesHolder.getProperty("oracle.url"));
		dialect.setUser(PropertiesHolder.getProperty("oracle.username"));
		dialect.setPassword(PropertiesHolder.getProperty("oracle.password"));
		return dialect;
	}
}
