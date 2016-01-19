import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.apache.solr.client.solrj.response.Group;
import org.apache.solr.client.solrj.response.GroupCommand;
import org.apache.solr.client.solrj.response.GroupResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.cloud.ZooKeeperException;
import org.apache.solr.common.params.GroupParams;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;


public class SolrTest1 {

    public static CloudSolrServer cloudSolrServer = null;

    public static CloudSolrServer getCloudServer(){
        if (cloudSolrServer==null){
            String zkHostUrl = "10.1.1.19:2181,10.1.1.20:2181,10.1.1.21:2181";

            try {

                cloudSolrServer = new CloudSolrServer(zkHostUrl);

                cloudSolrServer.setDefaultCollection("searchbu");
                cloudSolrServer.setZkClientTimeout(20000);
                cloudSolrServer.setZkConnectTimeout(1000);

            } catch (MalformedURLException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (Exception e1){
                e1.printStackTrace();
            }


        }
return cloudSolrServer;
    }

    public static void searchNormal() throws SolrServerException {
        SolrQuery sq = new SolrQuery();
        sq.setParam("q","infoClassCodes:(2901 0105)");
                   sq.setStart(0);
          sq.setRows(10);
        QueryResponse qrp = getCloudServer().query(sq);
        SolrDocumentList list = qrp.getResults();
        for (int i = 0; i < list.size(); i++) {
            SolrDocument doc = list.get(i);
            System.out.println("companyId:" + doc.getFieldValue("companyId"));
        }

    }

    public static void searchGroup() throws SolrServerException {
        SolrQuery params = new SolrQuery();
        params.setParam("q","infoClassCodes:(2901 0105)");
        params.setStart(0);
        params.setRows(10);
        params.setParam(GroupParams.GROUP,"true");
        params.setParam(GroupParams.GROUP_FIELD,"companyId");
        params.setParam(GroupParams.GROUP_LIMIT,"2");
        params.setParam(GroupParams.GROUP_TOTAL_COUNT,"true");
        QueryResponse rsp = getCloudServer().query(params);
        GroupResponse gresp = rsp.getGroupResponse();// 注意：此处不能再用resp.getResults()接收结果
        List<GroupCommand> commands = gresp.getValues();
        if (commands != null)
        {
            for (GroupCommand com : commands)
            {

                System.out.println("总的分组个数：" + com.getNGroups().longValue());
int i =0;
                for (Group group : com.getValues())
                {
++i;
                    SolrDocumentList hits = group.getResult();
                    System.out.println("*******"+i +":"+ group.getGroupValue());
                    for (SolrDocument doc : hits)
                    {
                        System.out.println("companyId:" + doc.getFieldValue("companyId"));
//                            Question question = SolrUtils
                        //                                  .solrDocumentToQuestion(doc);
//                            listquestion.add(question);
                    }

                }

            }
        }
    }


    /**
     * @param args
     * @throws IOException
     * @throws SolrServerException
     */
    public static void main(String[] args) throws SolrServerException, IOException {
   //     searchNormal();
        searchGroup();


    }

}
