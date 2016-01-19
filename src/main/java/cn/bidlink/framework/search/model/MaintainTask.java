package cn.bidlink.framework.search.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Entity
@org.hibernate.annotations.Entity(dynamicInsert = true, dynamicUpdate = true)
@Table(name = "indexer_maintain_task")
@PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id")
public class MaintainTask extends Task {

    private String queueTableName;
    private Integer repeatInterval;

    @Column(name = "queue_table_name")
    public String getQueueTableName() {
        return queueTableName;
    }

    public void setQueueTableName(String queueTableName) {
        this.queueTableName = queueTableName;
    }

    @Column(name = "repeat_interval")
    public Integer getRepeatInterval() {
        return repeatInterval;
    }

    public void setRepeatInterval(Integer repeatInterval) {
        this.repeatInterval = repeatInterval;
    }

}
