package uk.ac.ceh.gateway.catalogue.indexing.jena;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.indexing.IndexGenerator;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.apache.jena.rdf.model.ResourceFactory.*;
import static uk.ac.ceh.gateway.catalogue.indexing.jena.Ontology.*;

/**
 * The following class extracts semantic details from a GeminiDocument and
 * returns these as Jena Statements (triples)
 */
@Slf4j
@ToString
public class JenaIndexGeminiDocumentGenerator implements IndexGenerator<GeminiDocument, List<Statement>> {
    private final JenaIndexMetadataDocumentGenerator generator;
    private final String baseUri;

    public JenaIndexGeminiDocumentGenerator(JenaIndexMetadataDocumentGenerator generator, String baseUri) {
        this.generator = generator;
        this.baseUri = baseUri;
        log.info("Creating");
    }

    @Override
    public List<Statement> generateIndex(GeminiDocument document) {
        List<Statement> toReturn = generator.generateIndex(document);

        Resource me = generator.resource(document.getId());
        toReturn.add(createStatement(me, IDENTIFIER, createPlainLiteral(me.getURI()))); //Add as an identifier of itself

        Optional.ofNullable(document.getFileset())
            .orElse(Collections.emptyList())
            .forEach(fileset -> {
                fileset.getObservedProperty().forEach(op -> {
                    Resource observedPropertyResource = createResource(
                        Stream.of(op.getUri(), op.getTitle(), op.getValue())
                            .filter(value -> value != null && !value.trim().isEmpty())
                            .map(String::trim)
                            .findFirst()
                            .orElse("")
                    );

                    if (op.getUri() !=null && !op.getUri().isEmpty()) {
                        toReturn.add(createStatement(me, HAS_OBSERVED_PROPERTY, observedPropertyResource));
                    } else {
                        toReturn.add(createStatement(me, HAS_OBSERVED_PROPERTY, createPlainLiteral(String.valueOf(observedPropertyResource))));
                        observedPropertyResource = generator.resourceObservedProperty(observedPropertyResource, document.getId());
                    }

                    toReturn.add(createStatement(observedPropertyResource, RDFS_LABEL, createPlainLiteral(op.getValue())));

                    if (op.getUnitsUri() != null && !op.getUnitsUri().isEmpty()) {
                        Resource unitResource = generator.resource(op.getUnitsUri().trim());
                        toReturn.add(createStatement(observedPropertyResource, HAS_UNIT, unitResource));

                        if (op.getUnits() != null) {
                            toReturn.add(createStatement(unitResource, RDFS_LABEL, createPlainLiteral(op.getUnits())));
                        }
                    }
                });
            });

        Optional.ofNullable(document.getType())
            .ifPresent(t -> {
                if ("dataset".equalsIgnoreCase(t)) {
                    toReturn.add(createStatement(me, RDF_TYPE, CLASS_DCATDATASET));
                }
            });

        Optional.ofNullable(document.getBoundingBoxes())
            .orElse(Collections.emptyList())
            .forEach(b ->
                toReturn.add(createStatement(me, HAS_GEOMETRY, createTypedLiteral(b.getWkt(), WKT_LITERAL)))
            );

        Optional.ofNullable(document.getResourceIdentifiers())
            .orElse(Collections.emptyList())
            .stream()
            .filter(r -> !r.getCoupledResource().isEmpty())
            .forEach(r ->
                toReturn.add(createStatement(me, IDENTIFIER, createPlainLiteral(r.getCoupledResource())))
            );

        Optional.ofNullable(document.getCoupledResources())
            .orElse(Collections.emptyList())
            .stream()
            .filter(r -> !r.isEmpty())
            .forEach(r ->
                toReturn.add(createStatement(me, REQUIRES, createResource(r)))
            );
        Optional.ofNullable(document.getPublicationDate())
            .ifPresent(publicationDate -> {
                toReturn.add(createStatement(
                    me,
                    PUBLICATION_DATE,
                    createTypedLiteral(LocalDate.ofInstant(publicationDate.toInstant(), ZoneId.of("UTC")).toString())
                ));
            });

        toReturn.add(createStatement(me, RESOURCE_STATUS, createTypedLiteral(document.getAvailability())));
        return toReturn;
    }
}
