package cn.bidlink.framework.search.engine;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.solr.common.SolrInputDocument;

import cn.bidlink.framework.search.data.creditScore.CreditScoreUtils;
import cn.bidlink.framework.search.util.JdbcUtils;

public class QueuedSolrInputDocumentMapper extends SolrInputDocumentMapper {

	public static final String COLUMN_NAME_QUEUE_ID = "QUEUE_ID";
	public static final String COLUMN_NAME_BUSINESS_ID = "BUSINESS_ID";
	public static final String COLUMN_NAME_OPERATION_TYPE = "OPERATION_TYPE";
	public static final String COLUMN_NAME_CREDIT_SCORE_MESSAGE = "CREDIT_SCORE_MESSAGE";

	@Override
	public QueuedSolrInputDocument map(ResultSet resultSet, int rowNum) throws SQLException {
		QueuedSolrInputDocument document = null;
		OperationType operationType = OperationType.get(JdbcUtils.safeValue(resultSet, COLUMN_NAME_OPERATION_TYPE, Integer.class));
		if(operationType == null) {
			return null;
		}
		if(operationType.equals(OperationType.ADD) || operationType.equals(OperationType.UPDATE)) {
			document = (QueuedSolrInputDocument) super.map(resultSet, rowNum);
		} else {
			document = (QueuedSolrInputDocument) resolveDocument();
		}
		document.setOperationType(operationType);
		document.setQueueId(JdbcUtils.safeValue(resultSet, COLUMN_NAME_QUEUE_ID, Long.class));
		document.setBusinessId(JdbcUtils.safeValue(resultSet, COLUMN_NAME_BUSINESS_ID, String.class));
		
		try {
			String creditScoreMessage = JdbcUtils.safeValue(resultSet, COLUMN_NAME_CREDIT_SCORE_MESSAGE, String.class);
			if(creditScoreMessage != null && !creditScoreMessage.equals("")){
				document.setField("weight_score_d", CreditScoreUtils.getCreditScore(creditScoreMessage));
				document.setField("rating_i", CreditScoreUtils.getRating(creditScoreMessage));
			}
		} catch (Exception e) {
			//先这么处理吧
		}
		return document;
	}
	
	@Override
	protected SolrInputDocument resolveDocument() {
		return new QueuedSolrInputDocument();
	}
}
