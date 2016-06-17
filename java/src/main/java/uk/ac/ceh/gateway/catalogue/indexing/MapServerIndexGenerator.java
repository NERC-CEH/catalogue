package uk.ac.ceh.gateway.catalogue.indexing;

import freemarker.template.Configuration;
import lombok.AllArgsConstructor;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.services.MapServerDetailsService;

/**
 * The following IndexGenerator is responsible for creating MapFile definitions
 * for given MetadataDocuments. 
 * @author cjohn
 */
@AllArgsConstructor
public class MapServerIndexGenerator implements IndexGenerator<MetadataDocument, MapFile> {
    private final Configuration templateConfiguration;
    private final MapServerDetailsService mapServerDetailsService;
    
    /**
     * If the given MetadataDocument meets the prerequisite requirements to 
     * generate a MapFile, then return an instance of it. Otherwise return null
     * indicating that the supplied document can not be used to create a map file
     * @param toIndex document to turn in to a map file
     * @return A MapFile to generate for the given document.
     */
    @Override
    public MapFile generateIndex(MetadataDocument toIndex) {
        String mapfileTemplate = getMapFileTemplate(toIndex);
        if(mapfileTemplate != null) {
            return new MapFile(templateConfiguration, mapfileTemplate, toIndex);
        }
        else {
            return null;
        }
    }
    
    /**
     * Locates the map file template to use for the given document. This method
     * can safely return null, which means that this metadata document can not 
     * have map services created for it.
     * @param document to locate a map file template for
     * @return A map file template or null
     */
    public String getMapFileTemplate(MetadataDocument document) {
        if(mapServerDetailsService.isMapServiceHostable(document)) {
            return "mapfile/service.map.tpl";
        }
        return null;
    }
}