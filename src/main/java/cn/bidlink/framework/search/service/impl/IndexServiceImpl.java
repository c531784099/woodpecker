package cn.bidlink.framework.search.service.impl;

import cn.bidlink.framework.exception.GeneralException;
import cn.bidlink.framework.search.dao.impl.FieldDaoImpl;
import cn.bidlink.framework.search.dao.impl.IndexDaoImpl;
import cn.bidlink.framework.search.dao.impl.TaskDaoImpl;
import cn.bidlink.framework.search.engine.AbstractScheduler;
import cn.bidlink.framework.search.engine.Scheduler;
import cn.bidlink.framework.search.engine.SchedulerChain;
import cn.bidlink.framework.search.engine.SchedulerChain.TerminatedCallback;
import cn.bidlink.framework.search.model.Field;
import cn.bidlink.framework.search.model.Index;
import cn.bidlink.framework.search.model.Task;
import cn.bidlink.framework.search.service.IndexConfigService;
import cn.bidlink.framework.search.service.IndexSchedulser;
import cn.bidlink.framework.search.service.IndexService;
import cn.bidlink.framework.search.service.SchedulerService;
import cn.bidlink.framework.search.util.SpringPoolUtils;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;

import java.util.*;

public class IndexServiceImpl implements IndexService, DisposableBean, InitializingBean, ApplicationContextAware {

    /**
     * 日志对象
     */
    protected final Logger logger = Logger.getLogger(getClass());

    private ApplicationContext applicationContext;

    private IndexDaoImpl indexDao;
    private FieldDaoImpl fieldDao;
    private TaskDaoImpl taskDao;

    private SchedulerService schedulerService;

    private IndexConfigService indexConfigService;

    private Map<String, Scheduler> schedulers = new HashMap<String, Scheduler>() {

        private static final long serialVersionUID = 1L;

        public synchronized Scheduler put(String key, Scheduler value) {
            return super.put(key, value);
        }

    };

    public void setIndexDao(IndexDaoImpl indexDao) {
        this.indexDao = indexDao;
    }
    public void setFieldDao(FieldDaoImpl fieldDao) {
		this.fieldDao = fieldDao;
	}
	public void setTaskDao(TaskDaoImpl taskDao) {
		this.taskDao = taskDao;
	}
	public void setIndexConfigService(IndexConfigService indexConfigService) {
        this.indexConfigService = indexConfigService;
    }
    public void setSchedulerService(SchedulerService schedulerService) {
		this.schedulerService = schedulerService;
	}
    
	@Override
    public List<IndexSchedulser> findAllIndexSchedulsers() {
        ArrayList<IndexSchedulser> indexSchedulers = new ArrayList<IndexSchedulser>();
        List<Index> indexs = indexDao.getAll();
        if(indexs!=null){
        Collections.sort(indexs,new Comparator<Index>() {
            @Override
            public int compare(Index o1, Index o2) {
                long m1 = o1.getOrd();
                long m2 = o2.getOrd();
                if(o1.getOrd()==null)
                    m1 = o1.getId();
                if(o2.getOrd()==null){
                    m2 = o2.getId();
                }
                if(m1>m2){
                    return 1;
                }else if(m1<m2){
                    return -1;
                }else{
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
                }
            }
        });
	        for (Index index : indexs) {
	        	if(index.getState() != null && index.getState() == 1L){
	        		indexSchedulers.add(new IndexSchedulser(index, schedulers.get(index.getName())));
	        	}
	        }
        }
        return indexSchedulers;
    }

    public Scheduler startScheduler(Long indexId) {
        Index index = indexDao.get(indexId);
        final SchedulerChain schedulerChain = schedulerService.createScheduler(index);
        schedulerChain.setTerminatedCallback(new TerminatedCallback() {

            private SchedulerChain node;

            @Override
            public void call(SchedulerChain node) {
                this.node = node;
                logger.info("schedulerChain [" + schedulerChain.getName() + "] terminated" + new Date());
            }
        });
        schedulerChain.setName(index.getName());
        schedulerChain.start();
        schedulers.put(index.getName(), schedulerChain);
        return schedulerChain;
    }

    public Scheduler findScheduler(String name) {
        return schedulers.get(name);
    }

    public List<Scheduler> findAllSchedulers() {
        return new ArrayList<Scheduler>(schedulers.values());
    }

    @Override
    public void shutdownScheduler(String name) {
        Scheduler scheduler = findScheduler(name);
        if (scheduler != null) {
            scheduler.shutdown();
            schedulers.remove(name);
        }
    }

    @Override
    public void destroy() throws Exception {

        if (!CollectionUtils.isEmpty(schedulers)) {
            List<Scheduler> schedulersList = new ArrayList<Scheduler>(schedulers.values());
            AbstractScheduler.shutdown(schedulersList);
            while (!AbstractScheduler.isTerminated(schedulersList)) ;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        SchedulerService definedDchedulerService = null;
        try {
            definedDchedulerService = applicationContext.getBean(SchedulerService.class);
        } catch (Exception e) {
            logger.info("no SchedulerService found , will use DefaultSchedulerService");
        }

        if (definedDchedulerService == null) {
            schedulerService = (SchedulerService) applicationContext.getAutowireCapableBeanFactory().createBean(DefaultSchedulerService.class, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, true);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        SpringPoolUtils.applicationContext = applicationContext;
    }

    public Scheduler createIndex(Long indexId) {
        Index index = indexDao.get(indexId);
        //重新产生task
        indexConfigService.generateCreateTask(indexId);
        //启动scheduler
        Scheduler scheduler = startScheduler(indexId);
        //保存index状态
        index.setRecreated(true);
        indexDao.update(index);
        return scheduler;
    }

    @Override
    public Scheduler recreateIndex(Long indexId) {
        Index index = indexDao.get(indexId);
        //先删除原索引
        SolrServer solrServer = indexConfigService.createSolrServer(index);
        //重新产生task
        indexConfigService.generateCreateTask(indexId);
        try {
            solrServer.deleteByQuery("*:*");
            solrServer.commit();
            solrServer.shutdown();
        } catch (Exception e) {
            throw new GeneralException(e);
        }
        //启动scheduler
        Scheduler scheduler = startScheduler(indexId);
        //保存index状态
        index.setRecreated(true);
        indexDao.update(index);
        return scheduler;
    }
    
	@Override
	public Scheduler createIndexWithoutDivide(Long indexId) {
		Index index = indexDao.get(indexId);
        //启动scheduler
        Scheduler scheduler = startScheduler(indexId);
        //保存index状态
        index.setRecreated(true);
        indexDao.update(index);
        return scheduler;
	}
	
	@Override
	public Index copyIndex(Long indexId, String newIndexName) {
		Index index = indexDao.get(indexId);
		List<Field> fields = index.getFields();
		List<Task> tasks = index.getTasks();
		indexDao.getCommonDao().evict(index);
		index.setId(null);
		index.setName(newIndexName);
		index.setSolrServerCollection(newIndexName);
		Long newIndexId = indexDao.save(index);
		for(Field field : fields){
			indexDao.getCommonDao().evict(field);
			field.setId(null);
			field.setIndexId(newIndexId);
			fieldDao.save(field);
		}
		for(Task task : tasks){
			//if("create".equals(task.getName()) || "maintain".equals(task.getName())){
				indexDao.getCommonDao().evict(task);
				task.setId(null);
				task.setIndexId(newIndexId);
				taskDao.save(task);
			//}
		}
		return indexDao.get(newIndexId);
	}


    @Override
    public Index shallowCopyIndex(Long indexId, String newIndexName) {
        Index index = indexDao.get(indexId);
        List<Field> fields = index.getFields();
        List<Task> tasks = index.getTasks();
        indexDao.getCommonDao().evict(index);
        index.setId(null);
        index.setName(newIndexName);
        index.setSolrServerCollection(newIndexName);
        Long newIndexId = indexDao.save(index);
        for(Field field : fields){
            indexDao.getCommonDao().evict(field);
            field.setId(null);
            field.setIndexId(newIndexId);
            fieldDao.save(field);
        }
        for(Task task : tasks){
            //if("create".equals(task.getName()) || "maintain".equals(task.getName())){
            indexDao.getCommonDao().evict(task);
            task.setId(null);
            task.setIndexId(newIndexId);
            task.setContentSql("");
            taskDao.save(task);
            //}
        }
        return indexDao.get(newIndexId);
    }
	
	@Override
	public boolean existsIndexName(String indexName) {
		return indexDao.getUnique("name", indexName) != null;
	}
}
