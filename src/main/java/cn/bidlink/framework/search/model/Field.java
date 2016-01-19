package cn.bidlink.framework.search.model;

import cn.bidlink.framework.exception.GeneralException;
import cn.bidlink.framework.search.engine.DocumentField;
import cn.bidlink.framework.search.engine.FieldConvertor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@org.hibernate.annotations.Entity(dynamicInsert = true, dynamicUpdate = true)
@Table(name = "indexer_field")
public class Field extends DocumentField {

    private Long id;

    private String name;

    private String columnNames;

    private Class<?> type;

    private Boolean unique;

    private Class<?> convertorClass;

    private FieldConvertor convertor;

    private Long indexId;

    private String sourceType;

    @Id
    @GenericGenerator(name = "fieldGenerator", strategy = "increment")
    @GeneratedValue(generator = "fieldGenerator")
    @Column(name = "id")
    public Long getId() {
        return id;
    }

    @Column(name = "name")
    public String getName() {
        return name;
    }

    @Column(name = "column_names")
    public String getColumnNames() {
        return columnNames;
    }

    @Column(name = "type")
    public Class<?> getType() {
        return type;
    }

    @Column(name = "unique_key")
    public Boolean getUnique() {
        return unique;
    }

    @Column(name = "index_id")
    public Long getIndexId() {
        return indexId;
    }

    @Column(name = "convertor")
    public Class<?> getConvertorClass() {
        return convertorClass;
    }

    @Column(name = "source_type")
    public String getSourceType() {
        return sourceType;
    }

    @Transient
    public FieldConvertor getConvertor() {
        if (convertor == null && convertorClass != null) {
            try {
                convertor = (FieldConvertor) convertorClass.newInstance();
            } catch (Exception e) {
                throw new GeneralException(e);
            }
        }
        return convertor;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setColumnNames(String columnNames) {
        this.columnNames = columnNames;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public void setUnique(Boolean unique) {
        this.unique = unique;
    }

    public void setIndexId(Long indexId) {
        this.indexId = indexId;
    }

    public void setConvertorClass(Class<?> convertorClass) {
        this.convertorClass = convertorClass;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }
}
