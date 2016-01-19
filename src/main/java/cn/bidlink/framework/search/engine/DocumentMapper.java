package cn.bidlink.framework.search.engine;

import org.apache.solr.common.SolrInputDocument;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface DocumentMapper {

    public abstract SolrInputDocument map(SolrInputDocument document, ResultSet rs, String sourceType) throws SQLException;

    public abstract SolrInputDocument map(ResultSet rs, int rowNum) throws SQLException;

}
