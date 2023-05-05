package uk.ac.ceh.gateway.catalogue.postprocess;

/**
 * An interface which can populate additional information on to instances of a
 * given type
 * @param <T> the type which this service processes
 */
public interface PostProcessingService<T> {
    /**
     * Processes the given instance add adds application specific knowledge to
     * it. Useful for doing things like populating links with the correct
     * information. (e.g. link titles)
     * @param document document to process
     */
    void postProcess(T document) throws PostProcessingException;
}
