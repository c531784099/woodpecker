package cn.bidlink.framework.search.engine;

import org.apache.solr.common.SolrInputDocument;


public class QueuedSolrInputDocument extends SolrInputDocument {

    private static final long serialVersionUID = 1L;

    private OperationType operationType;

    private long queueId;

    //对应的业务编号
    private String businessId;

    //新增和修改时需要Document
    private SolrInputDocument document;

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    public SolrInputDocument getDocument() {
        return document;
    }

    public void setDocument(SolrInputDocument document) {
        this.document = document;
    }

    public long getQueueId() {
        return queueId;
    }

    public void setQueueId(long queueId) {
        this.queueId = queueId;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

}
