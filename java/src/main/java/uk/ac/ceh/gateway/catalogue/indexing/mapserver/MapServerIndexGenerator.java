package uk.ac.ceh.gateway.catalogue.indexing.mapserver;

import freemarker.template.Configuration;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import uk.ac.ceh.gateway.catalogue.indexing.IndexGenerator;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.services.MapServerDetailsService;

import java.util.Optional;

/**
 * The following IndexGenerator is responsible for creating MapFile definitions
 * for given MetadataDocuments.
 */
@Slf4j
@ToString
public class MapServerIndexGenerator implements IndexGenerator<MetadataDocument, MapFile> {
    private final Configuration templateConfiguration;
    private final MapServerDetailsService mapServerDetailsService;

    public MapServerIndexGenerator(Configuration templateConfiguration, MapServerDetailsService mapServerDetailsService) {
        this.templateConfiguration = templateConfiguration;
        this.mapServerDetailsService = mapServerDetailsService;
        log.info("Creating {}", this);
    }

    /**
     * If the given MetadataDocument meets the prerequisite requirements to
     * generate a MapFile, then return an instance of it. Otherwise return null
     * indicating that the supplied document can not be used to create a map file
     * @param toIndex document to turn in to a map file
     * @return A MapFile to generate for the given document.
     */
    @Override
    public MapFile generateIndex(MetadataDocument toIndex) {
        val mapfileTemplate = getMapFileTemplate(toIndex);
        if(mapfileTemplate.isPresent()) {
            val definition = mapServerDetailsService.getMapDataDefinition(toIndex);
            val projSystems = mapServerDetailsService.getProjectionSystems(definition);
            return new MapFile(templateConfiguration, mapfileTemplate.get(), projSystems, toIndex);
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
    private Optional<String> getMapFileTemplate(MetadataDocument document) {
        if(mapServerDetailsService.isMapServiceHostable(document)) {
            return Optional.of("mapfile/service.map.ftl");
        }
        return Optional.empty();
    }
}
