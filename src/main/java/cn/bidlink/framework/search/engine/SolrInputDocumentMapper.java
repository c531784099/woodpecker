package cn.bidlink.framework.search.engine;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.util.StringUtils;

import cn.bidlink.framework.exception.GeneralException;
import cn.bidlink.framework.search.util.JdbcUtils;
import cn.bidlink.framework.util.CollectionUtils;

public class SolrInputDocumentMapper implements DocumentMapper {

	private final List<DocumentField> fields = new ArrayList<DocumentField>();

	public SolrInputDocumentMapper() {

	}

	public SolrInputDocumentMapper(List<DocumentField> fields) {
		addField(fields);
	}

	public void addField(DocumentField field) {
		fields.add(field);
	}

	@SuppressWarnings("unchecked")
	public void addField(DocumentField[] fields) {
		this.fields.addAll(CollectionUtils.arrayToList(fields));
	}

	public void addField(List<DocumentField> fields) {
		this.fields.addAll(fields);
	}

	@Override
	public SolrInputDocument map(SolrInputDocument document, ResultSet resultSet, String sourceType) throws SQLException {
		if (!CollectionUtils.isEmpty(fields) && document != null) {
			for (DocumentField field : fields) {
				if (!org.apache.commons.lang.StringUtils.equals(sourceType, field.getSourceType())) {
					continue;
				}
				Object value = null;
				if (field.getConvertor() != null) {
					value = field.getConvertor().convert(field, resultSet);
				} else {
					if (!StringUtils.hasLength(field.getName())
							|| !StringUtils.hasLength(field.getColumnNames())
							|| field.getType() == null) {
						throw new GeneralException(" document field is empty");
					}
					if (field.getColumnNames().indexOf(",") > -1) {
						String[] columnNames = field.getColumnNames().split(",");
						StringBuilder sv = new StringBuilder();
						for (String columnName : columnNames) {
							if (!StringUtils.hasLength(columnName)){
								continue;
							}
							String s = JdbcUtils.safeValue(resultSet, columnName.trim(), String.class);
							if (StringUtils.hasLength(s)) {
								sv.append(s);
							}
						}
						value = sv.toString();
					} else {
						//String aa = field.getType().toString();
						value = JdbcUtils.safeValue(resultSet, field.getColumnNames(), field.getType());
					}
				}
				String name = field.getName();
				if (!document.containsKey(name)) {
					document.addField(field.getName(), value);
				}
			}
			return document;
		}
		return null;
	}

	@Override
	public SolrInputDocument map(ResultSet resultSet, int rowNum) throws SQLException {
		SolrInputDocument document = resolveDocument();
		if (!CollectionUtils.isEmpty(fields)) {
			for (DocumentField field : fields) {
				//System.out.println(field.getColumnNames());
				// String fieldName=field.getName();
				// if(field.getSourceType()!=null&&!field.getName().equals("noisestate")){//
				// continue;
				// }
				if (org.apache.commons.lang.StringUtils.isNotBlank(field.getSourceType())) {
					continue;
				}
				Object value = null;

				if (field.getConvertor() != null) {
					value = field.getConvertor().convert(field, resultSet);
				} else {
					if (!StringUtils.hasLength(field.getName())
							|| !StringUtils.hasLength(field.getColumnNames())
							|| field.getType() == null) {
						throw new GeneralException(" document field is empty");
					}
					
					if(field.getColumnNames().contains(",")){
						String[] columnNames = field.getColumnNames().split(",");
						//此字符串在方法内部，不存在线程安全问题，改用StringBuilder能提高性能
						//StringBuffer sv = new StringBuffer();
						StringBuilder sv = new StringBuilder();
						for (String columnName : columnNames) {
							if (!StringUtils.hasLength(columnName)){
								continue;
							}
							String s = JdbcUtils.safeValue(resultSet, columnName.trim(), String.class);
							if (StringUtils.hasLength(s)) {
								sv.append(s + " ");
							}
						}
						value = sv.toString();
					} else {
						value = JdbcUtils.safeValue(resultSet, field.getColumnNames(), field.getType());
					}
				}
				document.addField(field.getName(), value);
			}
		}
		return document;
	}

	protected SolrInputDocument resolveDocument() {
		return new SolrInputDocument();
	}

}
