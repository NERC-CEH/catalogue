package uk.ac.ceh.gateway.catalogue.indexing;

import java.util.List;

public interface DocumentIndexingService {
    boolean isIndexEmpty() throws DocumentIndexingException;
    void rebuildIndex() throws DocumentIndexingException;
    void indexDocuments(List<String> toIndex, String revision) throws DocumentIndexingException;
    void unindexDocuments(List<String> unIndex) throws DocumentIndexingException;
}
