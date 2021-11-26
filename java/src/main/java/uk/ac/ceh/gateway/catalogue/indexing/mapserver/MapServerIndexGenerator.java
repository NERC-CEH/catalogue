package uk.ac.ceh.gateway.catalogue.indexing.mapserver;

import freemarker.template.Configuration;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import uk.ac.ceh.gateway.catalogue.indexing.IndexGenerator;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.wms.MapServerDetailsService;

/**
 * The following IndexGenerator is responsible for creating MapFile definitions
 * for given MetadataDocuments.
 */
@Slf4j
@ToString
public class MapServerIndexGenerator implements IndexGenerator<MetadataDocument, MapFile> {
    private final Configuration templateConfiguration;
    private final MapServerDetailsService mapServerDetailsService;
    private final String mapfileTemplate = "mapfile/service.map.ftl";

    public MapServerIndexGenerator(Configuration templateConfiguration, MapServerDetailsService mapServerDetailsService) {
        this.templateConfiguration = templateConfiguration;
        this.mapServerDetailsService = mapServerDetailsService;
        log.info("Creating {}", this);
    }

    @Override
    public MapFile generateIndex(MetadataDocument document) {
        val definition = mapServerDetailsService.getMapDataDefinition(document);
        val projSystems = mapServerDetailsService.getProjectionSystems(definition);
        return new MapFile(templateConfiguration, mapfileTemplate, projSystems, document);
    }
}
