package cn.bidlink.framework.search.util;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * <p>validate index object</p>
 *
 * @author laifei@ebnew.com
 */
public class ValidatorIndexUtils {

    private ValidatorIndexUtils() {
    }

    ;

    public static final String UNIQUEKEY_DEFAULT_NAME = "id";

    /**
     * <p>validate unique key value</p>
     *
     * @param documents
     * @return
     */
    public static boolean validateUniqueKey(SolrInputDocument documents) {
        return validateUniqueKey(UNIQUEKEY_DEFAULT_NAME, documents);
    }

    /**
     * <p>validate unique key value</p>
     *
     * @param uniqueKeyName
     * @param documents
     * @return
     */
    public static boolean validateUniqueKey(String uniqueKeyName, SolrInputDocument documents) {
        Object uniqueKeyObject = documents.get(uniqueKeyName);
        if (uniqueKeyObject == null) {
            return false;
        } else if (uniqueKeyObject instanceof String) {
            String strValue = (String) uniqueKeyObject;
            if (StringUtils.isBlank(strValue)) {
                return false;
            }
        } else if (uniqueKeyObject instanceof SolrInputField) {
            SolrInputField field = (SolrInputField) uniqueKeyObject;
            if (field.getValue() == null) {
                return false;
            }
        }
        return true;
    }

    public static Collection<SolrInputDocument> validateDocument(Collection<SolrInputDocument> collection) {
        return validateDocument(UNIQUEKEY_DEFAULT_NAME, collection);
    }

    public static Collection<SolrInputDocument> validateDocument(String uniqueKeyName, Collection<SolrInputDocument> collection) {
        if (CollectionUtils.isEmpty(collection)) {
            return collection;
        }
        List<SolrInputDocument> illegalDocList = new ArrayList<SolrInputDocument>();
        Iterator<SolrInputDocument> iterator = collection.iterator();
        while (iterator.hasNext()) {
            SolrInputDocument document = iterator.next();
            if (document == null) {
                illegalDocList.add(document);
                continue;
            } else if (!validateUniqueKey(uniqueKeyName, document)) {
                illegalDocList.add(document);
            }
        }
        Iterator<SolrInputDocument> illegalDocIterator = illegalDocList.iterator();
        while (illegalDocIterator.hasNext()) {
            SolrInputDocument illegalDocument = illegalDocIterator.next();
            collection.remove(illegalDocument);
        }
        return collection;
    }

    public static void main(String... strings) {
        List<String> list = new ArrayList<String>();
        list.add("er");
        list.add("1");
        list.add("2");
        list.add("er");
        list.add(null);
        list.add("er");
        list.add("77");
        list.add("er");
        Iterator<String> iterator = list.iterator();
        List<String> relist = new ArrayList<String>();
        while (iterator.hasNext()) {
            String val = iterator.next();
            if ("er".equals(val) || val == null) {
                relist.add(val);
            }
        }
        for (int i = 0; i < relist.size(); i++) {
            list.remove(relist.get(i));
        }
        list.remove("111");
    }
}
