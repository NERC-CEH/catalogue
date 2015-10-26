package uk.ac.ceh.gateway.catalogue.postprocess;

/**
 * A post processing service which doesn't actually do anything
 * @author cjohn
 */
public class NullPostProcessingService implements PostProcessingService {

    @Override
    public void postProcess(Object value) throws PostProcessingException {}
}
