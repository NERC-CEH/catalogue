package uk.ac.ceh.gateway.catalogue.indexing;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Defines a DocumentIndexingService which delegates to another
 * DocumentIndexingService using a specified ExecutorService. This enables long
 * indexing tasks to be performed on a different thread and the bulk of the 
 * methods to return without blocking.
 */
@Slf4j
@AllArgsConstructor
public class AsyncDocumentIndexingService implements DocumentIndexingService {
    private final DocumentIndexingService proxy;
    private final ExecutorService executor;

    /**
     * Create an async document indexing service which uses a single thread
     * executor and delegates to the specified proxy
     * @param proxy service to delegate to
     */
    public AsyncDocumentIndexingService(DocumentIndexingService proxy) {
        this(proxy, Executors.newSingleThreadExecutor());
    }

    /**
     * Calls the underlying DocumentIndexingServices #isIndexEmpty()
     * @return the value of isIndexEmpty() from the underlying proxy
     * @throws DocumentIndexingException if there was a problem determining if
     *   the index is empty or not
     */
    @Override
    public boolean isIndexEmpty() throws DocumentIndexingException {
        return proxy.isIndexEmpty();
    }

    /**
     * Submits a request to rebuild the index. DocumentIndexingExceptions get
     * logged
     */
    @Override
    public void rebuildIndex() {
        executor.submit(() -> {
            try {
                proxy.rebuildIndex();
            }
            catch(DocumentIndexingException ex) {
                log.error("Failed to asynchronously rebuild index", ex);
            }
        });
    }

    /**
     * Submits a request to index the specified files at the specified revision.
     * DocumentIndexingExceptions get logged.
     * @param toIndex document names to index
     * @param revision the revision to index at
     */
    @Override
    public void indexDocuments(final List<String> toIndex, final String revision) {
        executor.submit(() -> {
            try {
                proxy.indexDocuments(toIndex, revision);
            }
            catch(DocumentIndexingException ex) {
                log.error("Failed to asynchronously index documents", ex);
            }
        });
    }

    /**
     * Submits a request to remove the specified files from the index.
     * DocumentIndexingExceptions get logged.
     * @param unIndex document names to remove from the index
     */
    @Override
    public void unindexDocuments(List<String> unIndex) {
	executor.submit(() -> {
            try {
                proxy.unindexDocuments(unIndex);
            }
            catch(DocumentIndexingException ex) {
                log.error("Failed to asynchronously unindex", ex);
            }
        });
    }
}
