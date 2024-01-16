package uk.ac.ceh.gateway.catalogue.indexing;

/**
 * The following interface will enable an indexing representation of a given D
 * which can then be processed by some indexing implementation
 *
 * @param <D> Document type
 * @param <I> Indexable representation
 */
public interface IndexGenerator<D, I> {
    I generateIndex(D toIndex) throws DocumentIndexingException;
}
