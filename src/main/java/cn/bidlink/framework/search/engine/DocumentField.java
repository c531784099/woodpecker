package cn.bidlink.framework.search.engine;

public class DocumentField {

    private String name;

    private String columnNames;

    private Class<?> type;

    private Boolean unique;

    private FieldConvertor convertor;

    private String sourceType;

    public String getName() {
        return name;
    }

    public String getColumnNames() {
        return columnNames;
    }

    public Class<?> getType() {
        return type;
    }

    public Boolean getUnique() {
        return unique;
    }

    public FieldConvertor getConvertor() {
        return convertor;
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

    public void setConvertor(FieldConvertor convertor) {
        this.convertor = convertor;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

}
