package uk.ac.ceh.gateway.catalogue.indexing.jena;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import uk.ac.ceh.gateway.catalogue.indexing.IndexGenerator;
import uk.ac.ceh.gateway.catalogue.infrastructure.InfrastructureRecord;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.apache.jena.rdf.model.ResourceFactory.createPlainLiteral;
import static org.apache.jena.rdf.model.ResourceFactory.createStatement;
import static uk.ac.ceh.gateway.catalogue.indexing.jena.Ontology.IDENTIFIER;

/**
 * The following class extracts semantic details from a InfrastructureRecord and
 * returns these as Jena Statements (triples)
 */
@Slf4j
@ToString
public class JenaIndexInfrastructureRecordGenerator implements IndexGenerator<InfrastructureRecord, List<Statement>> {
    private final JenaIndexMetadataDocumentGenerator generator;
    private final String baseUri;

    public JenaIndexInfrastructureRecordGenerator(JenaIndexMetadataDocumentGenerator generator, String baseUri) {
        this.generator = generator;
        this.baseUri = baseUri;
        log.info("Creating {}", this);
    }

    @Override
    public List<Statement> generateIndex(InfrastructureRecord document) {
        List<Statement> toReturn = generator.generateIndex(document);

        Resource me = generator.resource(document.getId());
        toReturn.add(createStatement(me, IDENTIFIER, createPlainLiteral(me.getURI()))); //Add as an identifier of itself

        Optional.ofNullable(document.getResourceIdentifiers())
            .orElse(Collections.emptyList())
            .stream()
            .filter(r -> !r.getCoupledResource().isEmpty())
            .forEach(r ->
                toReturn.add(createStatement(me, IDENTIFIER, createPlainLiteral(r.getCoupledResource())))
            );

        return toReturn;
    }
}
