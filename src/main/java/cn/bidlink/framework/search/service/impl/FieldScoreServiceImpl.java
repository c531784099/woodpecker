package cn.bidlink.framework.search.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bidlink.framework.search.dao.impl.FieldScoreDaoImpl;
import cn.bidlink.framework.search.model.FieldScore;
import cn.bidlink.framework.search.service.FieldScoreService;

public class FieldScoreServiceImpl implements FieldScoreService {
	
	FieldScoreDaoImpl fieldScoreDao;

	public void setFieldScoreDao(FieldScoreDaoImpl fieldScoreDao) {
		this.fieldScoreDao = fieldScoreDao;
	}

	@Override
	public List<FieldScore> findFieldScoreByIndexId(Long indexId) {
		Map<String, Object> params = new HashMap<String, Object>(1);
		params.put("indexId", indexId);
		return fieldScoreDao.getByProperties(params, null);
	}
}
