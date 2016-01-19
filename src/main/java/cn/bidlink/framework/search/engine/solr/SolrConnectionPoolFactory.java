package cn.bidlink.framework.search.engine.solr;

import cn.bidlink.framework.search.model.Index;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.log4j.Logger;
import java.net.MalformedURLException;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 13-7-30
 * Time: 上午11:52
 * To change this template use File | Settings | File Templates.
 */
public class SolrConnectionPoolFactory implements ConnectionPoolFactory<CloudSolrServer> {
    public static final Logger logger = Logger.getLogger(SolrConnectionPoolFactory.class);

    private static SolrConnectionPoolFactory poolFactory = null;
    private static Hashtable<String, CloudSolrServer> pools = null;

    public static synchronized SolrConnectionPoolFactory getInstance(){
        if(poolFactory == null){
            logger.info("init solr connection factory...");
            poolFactory = new SolrConnectionPoolFactory();
            pools = new Hashtable<String, CloudSolrServer>();
        }
        return poolFactory;
    }

    @Override
    public Map<String, CloudSolrServer> getPools(){
        return pools;
    }
    private CloudSolrServer create(Index index){
        int i =1;
        CloudSolrServer solr = null;
        try {
            solr = new CloudSolrServer(index.getSolrServerUrl());
            solr.setDefaultCollection(index.getSolrServerCollection());

            logger.info("create solr connection...");

        } catch (MalformedURLException e) {
            try {
                Thread.sleep(5000*i);
            } catch (InterruptedException e1) {
                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates
            logger.error("create solr connection exception" + e.getMessage());
            i++;
            logger.info("try to create solr connection "+i+" times");
            this.getConnectionPool(index);
        }
        return  solr;
    }

    @Override
    public CloudSolrServer getConnectionPool(Index index) {
        int i = 1;
        if(!pools.containsKey(index.getSolrServerCollection())){
            pools.put(index.getSolrServerCollection(),create(index));
        }
        return pools.get(index.getSolrServerCollection());

    }

    @Override
    public CloudSolrServer getConnectionPool(Index index, Boolean isCreate) {
        if(pools.containsKey(index.getSolrServerCollection())){
            pools.remove(index.getSolrServerCollection());

        }
        pools.put(index.getSolrServerCollection(),create(index));
        return pools.get(index.getSolrServerCollection());

    }

    @Override
    public void closeConnectionPool(String name) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
