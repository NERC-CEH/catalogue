package uk.ac.ceh.gateway.catalogue.indexing.jena;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringFacility;
import uk.ac.ceh.gateway.catalogue.indexing.IndexGenerator;

import java.util.List;
import java.util.Optional;

import static org.apache.jena.rdf.model.ResourceFactory.*;
import static uk.ac.ceh.gateway.catalogue.indexing.jena.Ontology.*;

/**
 * Extracts semantic details as triples from a MonitoringFacility
 */
@Slf4j
@ToString
public class JenaIndexMonitoringFacilityGenerator implements IndexGenerator<MonitoringFacility, List<Statement>> {
    private final JenaIndexMetadataDocumentGenerator generator;
    private final String baseUri;

    public JenaIndexMonitoringFacilityGenerator(JenaIndexMetadataDocumentGenerator generator, String baseUri) {
        this.generator = generator;
        this.baseUri = baseUri;
        log.info("Creating {}", this);
    }

    @Override
    public List<Statement> generateIndex(MonitoringFacility document) {
        List<Statement> toReturn = generator.generateIndex(document);
        Resource me = generator.resource(document.getId());

        Optional.ofNullable(document.getGeometry())
            .ifPresent(b -> toReturn.add(createStatement(me, HAS_GEOMETRY, createTypedLiteral(b.getGeometryString(), GEOJSON_LITERAL)))
            );

        return toReturn;
    }
}
