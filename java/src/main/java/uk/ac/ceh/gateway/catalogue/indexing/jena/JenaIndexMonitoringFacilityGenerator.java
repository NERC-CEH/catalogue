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

    public JenaIndexMonitoringFacilityGenerator(JenaIndexMetadataDocumentGenerator generator, String baseUri) {
        this.generator = generator;
        log.info("Creating");
    }

    @Override
    public List<Statement> generateIndex(MonitoringFacility document) {
        List<Statement> toReturn = generator.generateIndex(document);
        Resource me = generator.resource(document.getId());

        Optional.ofNullable(document.getGeometry())
            .ifPresent(b -> toReturn.add(createStatement(me, SF_GEOMETRY, createTypedLiteral(b.getGeometryString(), GEO_GEOJSONLITERAL)))
            );

        Optional.ofNullable(document.getGeometry())
            .ifPresent(g -> {
                Resource geometryNode = createResource(me + "#geom");

                toReturn.add(createStatement(me, RDF_TYPE, GEO_FEATURE));
                toReturn.add(createStatement(me, GEO_HASGEOMETRY, geometryNode));
                toReturn.add(createStatement(geometryNode, RDF_TYPE, GEO_GEOMETRY));
                Optional<String> wktOptional = g.getWkt();
                if (wktOptional.isPresent()) {
                    String wktString = wktOptional.get();
                    toReturn.add(createStatement(geometryNode, GEO_ASWKT, createTypedLiteral(wktString, GEO_WKTLITERAL)));
                } else {
                    log.info("Could not generate WKT from geometry, getWkt() returned empty");
                }
            });

        Optional.ofNullable(document.getOperationalStatus())
            .ifPresent(s -> toReturn.add(createStatement(me, DOO_OPERATIONALSTATUS, createPlainLiteral(s)))
            );

        return toReturn;
    }
}
//
