package cn.bidlink.framework.search.service;

import cn.bidlink.framework.search.engine.solr.SolrConnectionPoolFactory;
import cn.bidlink.framework.search.model.Index;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.cloud.ZkStateReader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;

public class test {

    /**
     * @param args
     * @throws java.net.MalformedURLException
     * @throws org.apache.solr.client.solrj.SolrServerException
     */
    public static void main(String[] args) throws MalformedURLException, SolrServerException, InterruptedException {
        // TODO Auto-generated method stub


       // String zkHosts = "211.151.182.227:2181,211.151.182.228:2181,211.151.182.231:2181,211.151.182.232:2181,211.151.182.233:2181";
        //      String zkHosts = "192.168.0.76:2191,192.168.0.77:2191,192.168.0.78:2191";
          //String zkHosts="192.168.0.85:2191";
              String zkHosts = "192.168.0.76:2181,192.168.0.77:2181,192.168.0.78:2181";
        //Index index = new Index();
        //index.setSolrServerUrl(zkHosts);
        //index.setSolrServerCollection("searchbuhis");

        CloudSolrServer solrServer1 = null;
        //  solrServer1 = SolrConnectionPoolFactory.getInstance().getConnectionPool(index);
        solrServer1 = new CloudSolrServer(zkHosts);
        solrServer1.setDefaultCollection("searchcorp");
        solrServer1.setZkClientTimeout(10000);
        solrServer1.setZkConnectTimeout(1000);
                           solrServer1.connect();
        //
        SolrQuery par = new SolrQuery();
       /**
        try {
            //   solrServer1.deleteByQuery("fullText:(老苗汤 OR 伟哥 OR 透视眼睛 OR 万艾可 OR 水果机遥控器  OR  透视眼镜)");
           // solrServer1.deleteByQuery("title:(中国人民解放军94896)");


            solrServer1.commit();

            //  solrServer1.deleteById("383140545");
        } catch (IOException e) {
            // e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
          */

         //查询条件
        String query="必联公司";
         par.setQuery("name:"+query+" OR mainProduct:"+query);
       // par.setParam("defType", "edismax");
        //par.setParam("qt","edismax");
       par.setRequestHandler("/bidsearch");
          /**
        par.setParam("q1","name:"+query);
        par.setParam("q2","mainProduct:"+query);
        par.setParam("bf","sum(map(query($q1),0.1,5,4000),map(query($q2),0.1,5,3000),wait)");
        par.setParam("mm","0%");
        //par.setParam("qf", "name^20 id^1 mainProduct^0.2 fullText^0.5 docAbstract^0.1");
        */
        par.setParam("debugQuery","true");
        par.setParam("indent","true");

        //查询结果返回起始位置
         par.setStart(0);
         //查询返回大小
         par.setRows(10);
        /**
         //设置高亮
         par.setHighlight(true).setHighlightSnippets(1);

         //设置高亮字段
         //    par.setParam("hl.fl", "title");
         par.setParam("hl.fl","name mainProduct");
         par.setParam("f.name.hl.fragsize","2");
         par.setParam("f.mainProduct.hl.fragsize","15");
         //par.setHighlightFragsize(4);
         */
         QueryResponse qr = solrServer1.query(par);
        System.out.println(qr.getResults().getNumFound());
         System.out.println(qr);
         /**
         System.out.println("123:"+qr);
         Iterator<SolrDocument> iter = qr.getResults().iterator();
         while (iter.hasNext()) {
         SolrDocument resultDoc = iter.next();
         String title = (String) resultDoc.getFieldValue("name");
         System.out.println("title:"+title);
         String id = resultDoc.getFieldValue("id").toString();
         System.out.println("id:"+id);
         if (qr.getHighlighting().get(id) != null) {
         List<String> highightSnippets = qr.getHighlighting().get(id).get("name");
         System.out.println("------------------start------");
         for(int i=0;i<highightSnippets.size();i++){

         System.out.println(highightSnippets.get(i));
         }
         System.out.println("---------------end---------");
         }
         }


         System.out.println("result:"+qr);
         // Thread.sleep(240000);
         try{
         qr = solrServer1.query(par);
         System.out.println("sed:result:"+qr);
         }catch(Exception e){
         System.out.println("sed:result:"+e);
         Thread.sleep(180000);
         }
         // solrServer1.connect();
         //qr = solrServer1.query(par);
         //System.out.println("after shutdown:"+qr);



         qr = solrServer1.query(par);
         System.out.println("************************************ close all sed:result:"+qr);
         if(solrServer1.getZkStateReader()!=null){
         System.out.println("reader is not null");
         }

         solrServer1.shutdown();
         solrServer1.shutdown();
         if(solrServer1.getZkStateReader()==null){
         System.out.println("reader is null");
         }
         solrServer1.shutdown();


         /**

         System.out.println("before:"+qr.getResults().getNumFound());
         //  solrServer1.deleteByQuery("fullText:(药房 处方药 药品名 服用 疫苗 力月西 成药 康泰克)");
         //  solrServer1.commit(true, true);
         qr = solrServer1.query(par);
         qr.getResponse().toString()
         System.out.println("after:"+qr.getResults().getNumFound());
         //    } catch (IOException e) {
         //         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         //     }
         */

    }

}
