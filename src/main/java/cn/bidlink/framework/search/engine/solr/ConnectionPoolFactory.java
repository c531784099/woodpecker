package cn.bidlink.framework.search.engine.solr;

import cn.bidlink.framework.search.model.Index;

import java.util.Map;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 13-7-30
 * Time: 上午11:47
 * To change this template use File | Settings | File Templates.
 */
public interface ConnectionPoolFactory<S> {
    public Map<String, S> getPools();

    public S getConnectionPool(Index index);
    public S getConnectionPool(Index index, Boolean isCreate);

    public void closeConnectionPool(String name);

}

