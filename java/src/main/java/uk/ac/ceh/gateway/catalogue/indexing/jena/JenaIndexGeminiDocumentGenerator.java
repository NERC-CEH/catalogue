package uk.ac.ceh.gateway.catalogue.indexing.jena;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.indexing.IndexGenerator;

import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
        log.info("Creating {}", this);
    }

    @Override
    public List<Statement> generateIndex(GeminiDocument document) {
        List<Statement> toReturn = generator.generateIndex(document);

        Resource me = generator.resource(document.getId());
        toReturn.add(createStatement(me, IDENTIFIER, createPlainLiteral(me.getURI()))); //Add as an identifier of itself

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
                toReturn.add(createStatement(me, EIDC_USES, createResource(r)))
            );

        try {
            toReturn.add(createStatement(me, PUBLICATION_DATE, createTypedLiteral(document.getPublicationDate().toInstant().atZone(ZoneId.of("UTC")).toLocalDate().toString())));
        } catch(NullPointerException e) {
            log.info("No Publication Date");
        }
        toReturn.add(createStatement(me, RESOURCE_STATUS, createTypedLiteral(document.getResourceStatus())));

        return toReturn;
    }
}
