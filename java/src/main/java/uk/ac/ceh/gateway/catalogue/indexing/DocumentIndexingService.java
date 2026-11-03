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

    /**
     * Whether a rebuild of this index is under way. While it is, the index holds only some of the
     * documents, so anything read from it - a record's relationships, say - may be incomplete even
     * though the index is not empty.
     */
    default boolean isRebuilding() {
        return false;
    }

    /**
     * Whether the index is fit to be read from: it is neither empty nor part-way through a rebuild.
     * An index that cannot be reached is not ready. A rebuild in which some documents failed to
     * index still leaves the rest, and that index counts as ready - it is as complete as a rebuild
     * will make it, since one that holds documents isn't rebuilt again automatically.
     */
    default boolean isReady() {
        try {
            return !isRebuilding() && !isIndexEmpty();
        } catch (DocumentIndexingException ex) {
            return false;
        }
    }
}
