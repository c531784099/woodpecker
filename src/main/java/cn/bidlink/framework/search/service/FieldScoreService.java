package cn.bidlink.framework.search.service;

import java.util.List;

import cn.bidlink.framework.search.model.FieldScore;

public interface FieldScoreService {
	
	public List<FieldScore> findFieldScoreByIndexId(Long indexId);

}
