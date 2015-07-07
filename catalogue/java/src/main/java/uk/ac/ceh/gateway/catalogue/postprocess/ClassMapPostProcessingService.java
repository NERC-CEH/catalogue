package uk.ac.ceh.gateway.catalogue.postprocess;

import lombok.Data;
import uk.ac.ceh.gateway.catalogue.util.ClassMap;

/**
 * A post processing service which delegates to an other post processing service
 * which is located from the given ClassMap Lookup
 * @see ClassMap
 * @author cjohn
 */
@Data
public class ClassMapPostProcessingService implements PostProcessingService<Object> {
    private final ClassMap<PostProcessingService> lookup;
    
    @Override
    public void postProcess(Object value) {
        PostProcessingService bestService = lookup.get(value.getClass());
        if(bestService != null) {
            bestService.postProcess(value);
        }
    }    
}
