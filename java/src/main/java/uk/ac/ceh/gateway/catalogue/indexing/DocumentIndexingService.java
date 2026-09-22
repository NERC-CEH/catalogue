package uk.ac.ceh.gateway.catalogue.indexing;

import java.util.List;

public interface DocumentIndexingService {
    boolean isIndexEmpty() throws DocumentIndexingException;
    void rebuildIndex() throws DocumentIndexingException;
    void indexDocuments(List<String> toIndex, String revision) throws DocumentIndexingException;
    void unindexDocuments(List<String> unIndex) throws DocumentIndexingException;

    /**
     * Rebuild the index if it is empty, never throwing.
     *
     * @return true if the state of the index was established and acted upon - either it already held
     *         documents, or a rebuild ran (possibly with individual document failures). false if the
     *         index could not be reached at all, in which case the caller should try again later.
     */
    boolean attemptIndexing();
}
