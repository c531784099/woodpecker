package cn.bidlink.framework.search.model;

import cn.bidlink.framework.search.engine.DatabaseDialect;
import cn.bidlink.framework.util.ClassUtils;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@org.hibernate.annotations.Entity(dynamicInsert = true, dynamicUpdate = true)
@Table(name = "indexer_index_cloud")
public class Index implements Cloneable {
    private Long id;
    private Long ord;
    private String name;
    private String databaseDialectClass;
    private DatabaseDialect databaseDialect;
    private String dbDriverClass;
    private String dbUrl;
    private String dbUsername;
    private String dbPassword;
    private int dbBatchSize;
    private String solrServerUrl;
    private String solrServerCollection;
    private boolean recreated;
    private Long state;

    private List<Field> fields = new ArrayList<Field>(0);

    private Field uniqueField;

    private List<Task> tasks = new ArrayList<Task>(0);

    private List<CreateTask> createTasks = new ArrayList<CreateTask>(0);

    private List<MaintainTask> maintainTasks = new ArrayList<MaintainTask>(0);

    @Id
    @GenericGenerator(name = "indexGenerator", strategy = "increment")
    @GeneratedValue(generator = "indexGenerator")
    @Column(name = "id")
    public Long getId() {
        return id;
    }

    @Column(name="ord")
    public Long getOrd(){
        return ord;
    }


    @Column(name = "name")
    public String getName() {
        return name;
    }

    @Column(name = "database_dialect_class")
    public String getDatabaseDialectClass() {
        return databaseDialectClass;
    }

    @Transient
    public DatabaseDialect getDatabaseDialect() {
        if (databaseDialect == null) {
            databaseDialect = (DatabaseDialect) ClassUtils.newInstance(databaseDialectClass);
        }
        return databaseDialect;
    }

    @Column(name = "db_url")
    public String getDbUrl() {
        return dbUrl;
    }

    @Column(name = "db_username")
    public String getDbUsername() {
        return dbUsername;
    }

    @Column(name = "db_password")
    public String getDbPassword() {
        return dbPassword;
    }

    @Column(name = "db_driver_class")
    public String getDbDriverClass() {
        return dbDriverClass;
    }

    @Column(name = "db_batch_size")
    public int getDbBatchSize() {
        return dbBatchSize;
    }

    @Column(name = "solr_server_url")
    public String getSolrServerUrl() {
        return solrServerUrl;
    }

    @Column(name = "solr_server_collection")
    public String getSolrServerCollection() {
        return solrServerCollection;
    }

    @Column(name = "recreated", nullable = true)
    public boolean isRecreated() {
        return recreated;
    }

    @Transient
    public List<Field> getFields() {
        return fields;
    }

    @Transient
    public Field getUniqueField() {
        return uniqueField;
    }

    @Transient
    public List<Task> getTasks() {
        return tasks;
    }

    @Transient
    public List<CreateTask> getCreateTasks() {
        return createTasks;
    }


    @Transient
    public List<MaintainTask> getMaintainTasks() {
        return maintainTasks;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setOrd(Long ord){
        this.ord = ord;
        if(this.ord == null){
            this.ord = this.id;
            if(this.ord ==null){
                this.ord=3L;
            }
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDatabaseDialectClass(String databaseDialectClass) {
        this.databaseDialectClass = databaseDialectClass;
    }

    public void setDbUsername(String dbUsername) {
        this.dbUsername = dbUsername;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public void setDbDriverClass(String dbDriverClass) {
        this.dbDriverClass = dbDriverClass;
    }

    public void setDbBatchSize(int dbBatchSize) {
        this.dbBatchSize = dbBatchSize;
    }

    public void setSolrServerUrl(String solrServerUrl) {
        this.solrServerUrl = solrServerUrl;
    }

    public void setSolrServerCollection(String solrServerCollection) {
        this.solrServerCollection = solrServerCollection;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
        for (Field field : fields) {
            if (field.getUnique() != null && field.getUnique()) {
                uniqueField = field;
                break;
            }
        }
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public void setUniqueField(Field uniqueField) {
        this.uniqueField = uniqueField;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        createTasks.clear();
        maintainTasks.clear();
        for (Task task : tasks) {
            if (task instanceof CreateTask) {
                createTasks.add((CreateTask) task);
            } else if (task instanceof MaintainTask) {
                maintainTasks.add((MaintainTask) task);
            }
        }
    }

    public void setRecreated(boolean recreated) {
        this.recreated = recreated;
    }
    
    @Column(name = "state")
	public Long getState() {
		return state;
	}
	public void setState(Long state) {
		this.state = state;
	}
}