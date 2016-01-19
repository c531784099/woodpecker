package cn.bidlink.framework.search.data.creditScore;

import java.util.Date;

import cn.bidlink.search.datasource.annotation.JmsObject;
import cn.bidlink.search.datasource.annotation.SqlColumn;
import cn.bidlink.search.datasource.annotation.SqlObject;
import cn.bidlink.search.datasource.annotation.SqlPk;
import cn.bidlink.search.datasource.annotation.SqlSequence;

/**
 * version=0.0.1，companyId=123456，score=，updateTime=
 * 
 * */
@JmsObject(name="bid-queue-credit-score", converter=CreditScoreMessageConverter.class)
@SqlObject(name="INDEXER_QUEUE_Q_COMPANY_CLONE")
public class CreditScore {
	
	@SqlPk
	@SqlSequence(name="unireg.SEQ_INDEXER_QUEUE_Q_COMPANY.NEXTVAL")
	@SqlColumn(name="QUEUE_ID")
	private Long queueId;
	@SqlColumn(name="BUSINESS_ID")
	private Long businessId;
	@SqlColumn(name="OPERATION_TYPE")
	private Integer operationType;
	@SqlColumn(name="CREATETIME")
	private Date createTime;
	@SqlColumn(name="UPDATETIME")
	private Date updateTime;
	@SqlColumn(name="CREDIT_SCORE_MESSAGE")
	private String creditScoreMessage;
	
	public Long getQueueId() {
		return queueId;
	}
	public void setQueueId(Long queueId) {
		this.queueId = queueId;
	}
	public Long getBusinessId() {
		return businessId;
	}
	public void setBusinessId(Long businessId) {
		this.businessId = businessId;
	}
	public Integer getOperationType() {
		return operationType;
	}
	public void setOperationType(Integer operationType) {
		this.operationType = operationType;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	public String getCreditScoreMessage() {
		return creditScoreMessage;
	}
	public void setCreditScoreMessage(String creditScoreMessage) {
		this.creditScoreMessage = creditScoreMessage;
	}
}
