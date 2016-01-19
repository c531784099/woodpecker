package cn.bidlink.framework.search.dao.impl;

import java.util.ArrayList;
import java.util.List;

import cn.bidlink.framework.common.Paging;
import cn.bidlink.framework.dao.BaseDao;
import cn.bidlink.framework.search.model.Field;
import cn.bidlink.framework.search.model.Index;
import cn.bidlink.framework.search.model.MaintainTask;
import cn.bidlink.framework.search.model.Task;

public class IndexDaoImpl extends BaseDao<Index, Long>{

	@Override
	public Index get(Long id) {
		Index index = super.get(id);
		List<Field> fields = commonDao.find("from Field f where f.indexId = ? order by f.id asc", id);
		index.setFields(fields);
		List<Task> tasks = commonDao.find("from Task t where t.indexId = ? order by t.id asc", id);
		index.setTasks(tasks);
		return index;
	}
	
	@Override
	public List<Index> getAll(Paging... pagings) {
		List<Index> indexs = super.getAll();
		List<Index> fullIndexs = new ArrayList<Index>();
		for(Index index : indexs) {
			fullIndexs.add(get(index.getId()));
		}
		return fullIndexs;
	}
	
	@Override
	public void delete(Index index) {
		delete(index.getId());
	}
	
	@Override
	public void delete(Long id) {
		super.delete(id);
		String deleteField = " delete from Field f where f.indexId = ?";
		commonDao.delete(deleteField, new Object[]{id});
		String deleteTask = " delete from Task t where t.indexId = ?";
		commonDao.delete(deleteTask, new Object[]{id});
	}
	
	@SuppressWarnings("deprecation")
	public void clearQueue(Long indexId) {
		Index index = this.get(indexId);
		List<MaintainTask> tasks = index.getMaintainTasks();
		for(MaintainTask task : tasks) {
			String sql = "delete from " + task.getQueueTableName();
			commonDao.executeSQL(sql);
		}
	}
}
