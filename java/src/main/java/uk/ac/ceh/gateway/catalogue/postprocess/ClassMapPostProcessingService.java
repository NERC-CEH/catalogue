package uk.ac.ceh.gateway.catalogue.postprocess;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ceh.gateway.catalogue.indexing.ClassMap;

/**
 * A post-processing service which delegates to another post-processing service
 * which is located from the given ClassMap Lookup
 * @see ClassMap
 */
@Slf4j
@ToString(exclude = "lookup")
@SuppressWarnings({"unchecked", "rawtypes"})
public class ClassMapPostProcessingService implements PostProcessingService<Object> {
    private final ClassMap<PostProcessingService> lookup;

    public ClassMapPostProcessingService(ClassMap<PostProcessingService> lookup) {
        this.lookup = lookup;
        log.info("Creating {}", this);
    }

    @Override
    public void postProcess(Object value) throws PostProcessingException {
        PostProcessingService bestService = lookup.get(value.getClass());
        if(bestService != null) {
            bestService.postProcess(value);
        }
    }
}
