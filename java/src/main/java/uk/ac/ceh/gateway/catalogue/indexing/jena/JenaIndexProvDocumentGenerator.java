package uk.ac.ceh.gateway.catalogue.indexing.jena;

import com.google.common.base.Strings;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import uk.ac.ceh.gateway.catalogue.model.ProvDocument;
import uk.ac.ceh.gateway.catalogue.indexing.IndexGenerator;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.apache.jena.rdf.model.ResourceFactory.*;
import static uk.ac.ceh.gateway.catalogue.indexing.jena.Ontology.*;

/**
 * The following class extracts semantic details from a ProvDocument and
 * returns these as Jena Statements (triples)
 */
@Slf4j
@ToString
public class JenaIndexProvDocumentGenerator implements IndexGenerator<ProvDocument, List<Statement>> {
    private final JenaIndexMetadataDocumentGenerator generator;
    private final String baseUri;

    public JenaIndexProvDocumentGenerator(JenaIndexMetadataDocumentGenerator generator, String baseUri) {
        this.generator = generator;
        this.baseUri = baseUri;
        log.info("Creating {}", this);
    }

    @Override
    public List<Statement> generateIndex(ProvDocument document) {
        List<Statement> toReturn = generator.generateIndex(document);

        Resource me = generator.resource(document.getId());
        toReturn.add(createStatement(me, IDENTIFIER, createPlainLiteral(me.getURI()))); //Add as an identifier of itself

        Optional.ofNullable(document.getRelatedRecords())
            .orElse(Collections.emptyList())
            .stream()
            .filter(rr -> !Strings.isNullOrEmpty(rr.getRel()))
            .filter(rr -> !Strings.isNullOrEmpty(rr.getIdentifier()))
            .forEach(rr -> {
                log.debug(rr.toString());
                toReturn.add(createStatement(
                    me,
                    createProperty(rr.getRel()),
                    createProperty(baseUri + "/id/" + rr.getIdentifier())
                ));
            });

        return toReturn;
    }
}
