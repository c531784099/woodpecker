package cn.bidlink.framework.search.service;
import cn.bidlink.framework.search.model.*;
import cn.bidlink.framework.search.model.Task;
import org.apache.solr.client.solrj.impl.CloudSolrServer;

public interface IndexConfigService {

    public void generateCreateTask(Long indexId);

    public void saveTaskStatus(Task[] tasks);

    public void clearQueue(Long indexId);

    public CloudSolrServer createSolrServer(Index index);

}
