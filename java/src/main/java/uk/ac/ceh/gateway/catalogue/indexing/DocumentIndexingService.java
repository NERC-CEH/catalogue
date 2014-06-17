package uk.ac.ceh.gateway.catalogue.indexing;

import java.util.List;

/**
 *
 * @author cjohn
 */
public interface DocumentIndexingService {
    void rebuildIndex() throws DocumentIndexingException;
    void indexDocuments(List<String> toIndex, String revision) throws DocumentIndexingException;
    void unindexDocuments(List<String> unIndex) throws DocumentIndexingException;
}
