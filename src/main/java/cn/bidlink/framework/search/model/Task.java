package cn.bidlink.framework.search.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@Entity
@org.hibernate.annotations.Entity(dynamicInsert = true, dynamicUpdate = true)
@Table(name = "INDEXER_TASK")
@Inheritance(strategy = InheritanceType.JOINED)
public class Task {
    private Long id;
    private String name;
    private Date startTime;
    private Boolean completed;
    private Long completedTime;
    private Long completedCount;
    private String exception;
    private String contentSql;
    private Long indexId;

    @Id
    @GenericGenerator(name = "taskGenerator", strategy = "increment")
    @GeneratedValue(generator = "taskGenerator")
    @Column(name = "id")
    public Long getId() {
        return id;
    }

    @Column(name = "name")
    public String getName() {
        return name;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start_time")
    public Date getStartTime() {
        return startTime;
    }

    @Column(name = "completed")
    public Boolean getCompleted() {
        return completed;
    }

    @Column(name = "completed_time")
    public Long getCompletedTime() {
        return completedTime;
    }

    @Column(name = "completed_count")
    public Long getCompletedCount() {
        return completedCount;
    }

    @Column(name = "exception")
    public String getException() {
        return exception;
    }

    @Column(name = "content_sql")
    public String getContentSql() {
        return contentSql;
    }

    @Column(name = "index_id")
    public Long getIndexId() {
        return indexId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public void setCompletedTime(Long completedTime) {
        this.completedTime = completedTime;
    }

    public void setCompletedCount(Long completedCount) {
        this.completedCount = completedCount;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public void setContentSql(String contentSql) {
        this.contentSql = contentSql;
    }

    public void setIndexId(Long indexId) {
        this.indexId = indexId;
    }

}
