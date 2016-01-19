package cn.bidlink.framework.search.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;


@Entity
@org.hibernate.annotations.Entity(dynamicInsert = true, dynamicUpdate = true)
@Table(name = "INDEXER_CREATE_TASK")
@PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id")
public class CreateTask extends Task {

    private Boolean dividable;
    private Long divideSize;

    private String divideSql;
    private String tableName;
    private Boolean original;

    @Column(name = "dividable")
    public Boolean getDividable() {
        return dividable;
    }
    @Column(name = "divide_size")
    public Long getDivideSize() {
        return divideSize;
    }
    @Column(name = "table_name")
    public String getTableName() {
        return tableName;
    }
    @Column(name = "original")
    public Boolean getOriginal() {
        return original;
    }
    @Column(name = "divide_sql")
    public String getDivideSql() {
		return divideSql;
	}
    
    public void setDivideSql(String divideSql) {
		this.divideSql = divideSql;
	}
    public void setDividable(Boolean dividable) {
        this.dividable = dividable;
    }
	public void setDivideSize(Long divideSize) {
        this.divideSize = divideSize;
    }
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    public void setOriginal(Boolean original) {
        this.original = original;
    }
}
