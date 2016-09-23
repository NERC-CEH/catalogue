package uk.ac.ceh.gateway.catalogue.indexing;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import java.util.List;
import java.util.Optional;
import static uk.ac.ceh.gateway.catalogue.indexing.Ontology.*;
import static org.apache.jena.rdf.model.ResourceFactory.*;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ceh.gateway.catalogue.imp.ImpDocument;

/**
 * The following class extracts semantic details from a ImpDocument and 
 * returns these as Jena Statements (triples)
 */
public class JenaIndexImpDocumentGenerator implements IndexGenerator<ImpDocument, List<Statement>> {
    private final JenaIndexMetadataDocumentGenerator generator;

    @Autowired
    public JenaIndexImpDocumentGenerator(JenaIndexMetadataDocumentGenerator generator) {
        this.generator = generator;
    }

    @Override
    public List<Statement> generateIndex(ImpDocument document) {
        List<Statement> toReturn = generator.generateIndex(document);
        
        Resource me = generator.resource(document.getId());
        toReturn.add(createStatement(me, IDENTIFIER, createPlainLiteral(me.getURI()))); //Add as an identifier of itself
        
        Optional.ofNullable(document.getIdentifiers())
            .orElse(Collections.emptyList())
            .stream()
            .forEach(id -> {
                toReturn.add(
                    createStatement(me, REFERENCES, generator.resource(id))
                );
            });
        
        return toReturn;
    }
}
