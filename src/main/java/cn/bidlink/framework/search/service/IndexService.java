package cn.bidlink.framework.search.service;

import cn.bidlink.framework.search.engine.Scheduler;
import cn.bidlink.framework.search.model.Index;

import java.util.List;
//Simon
//Date:20151210
public interface IndexService {

    public List<IndexSchedulser> findAllIndexSchedulsers();

    public Scheduler startScheduler(Long indexId);

    public Scheduler findScheduler(String name);

    public List<Scheduler> findAllSchedulers();

    public void shutdownScheduler(String schedulerName);
    /**
     * 新建索引
     *
     * @param indexId
     * @return
     */
    public Scheduler createIndex(Long indexId);
    /**
     * 重建索引
     *
     * @param indexId
     * @return
     */
    public Scheduler recreateIndex(Long indexId);
    /**
     * 不进行分区创建索引
     * */
    public Scheduler createIndexWithoutDivide(Long indexId);
    /**
     * 复制Index
     * */
    public Index copyIndex(Long indexId, String newIndexName);

    /**
     * Shallow Copy Index
     */

    public Index shallowCopyIndex(Long indexId, String newIndexName);
    /**
     * 判断是否有index名字
     * */
    public boolean existsIndexName(String indexName);
}
