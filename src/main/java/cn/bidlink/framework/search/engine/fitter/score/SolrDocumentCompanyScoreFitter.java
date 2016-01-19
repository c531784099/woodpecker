package cn.bidlink.framework.search.engine.fitter.score;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bidlink.framework.search.dao.impl.FieldDaoImpl;
import cn.bidlink.framework.search.dao.impl.FieldScoreDaoImpl;
import cn.bidlink.framework.search.model.Field;
import cn.bidlink.framework.search.model.FieldScore;
import cn.bidlink.framework.search.util.SpringPoolUtils;
import cn.bidlink.framework.util.holder.PropertiesHolder;
import cn.bidlink.search.solr.fitter.score.SolrDocumentScoreFitter;
import cn.bidlink.search.solr.fitter.score.computer.DataCompletenessScoreComputer;
import cn.bidlink.search.solr.fitter.score.computer.DataIntervalScoreComputer;
import cn.bidlink.search.solr.fitter.score.computer.DataParticipateScoreComputer;
import cn.bidlink.search.solr.fitter.score.computer.DataTimeValidityScoreComputer;
import cn.bidlink.search.solr.fitter.score.model.SolrDocumentScore;

/**
 * 公司得分计算装配工
 * */
public class SolrDocumentCompanyScoreFitter extends SolrDocumentScoreFitter {
	
	public static final Map<String, SolrDocumentScore> solrDocumentScores = new HashMap<String, SolrDocumentScore>();;

	static {
		new DataCompletenessScoreComputer();
		new DataIntervalScoreComputer();
		new DataParticipateScoreComputer();
		new DataTimeValidityScoreComputer();
		
		FieldScoreDaoImpl fieldScoreDao = (FieldScoreDaoImpl) SpringPoolUtils.getBean(FieldScoreDaoImpl.class);
		FieldDaoImpl fieldDao = (FieldDaoImpl) SpringPoolUtils.getBean(FieldDaoImpl.class);
		Map<String, Object> params = new HashMap<String, Object>(1);
		params.put("indexId", PropertiesHolder.getLong("index.id.company"));
		List<FieldScore> fieldScores = fieldScoreDao.getByProperties(params, null);
		for(FieldScore fieldScore : fieldScores){
			SolrDocumentScore solrDocumentScore = new SolrDocumentScore();
			solrDocumentScore.setComputingClassName(fieldScore.getFieldScoreComputer().getComputerClassName());
			solrDocumentScore.setParams(fieldScore.getFieldScoreComputer().getParams());
			solrDocumentScore.setScore(fieldScore.getScore());
			Field field = fieldDao.get(fieldScore.getFieldId());
			solrDocumentScores.put(field.getName(), solrDocumentScore);
		}
	}
	
	public SolrDocumentCompanyScoreFitter(){
		super(solrDocumentScores);
	}
	
}
