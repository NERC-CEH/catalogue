package uk.ac.ceh.gateway.catalogue.indexing;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import static uk.ac.ceh.gateway.catalogue.indexing.Ontology.*;
import static com.hp.hpl.jena.rdf.model.ResourceFactory.*;

/**
 * The following class extracts semantic details from a GeminiDocument and 
 * returns these as Jena Statements (triples)
 * @author cjohn
 */
@Data
public class JenaIndexGeminiDocumentGenerator implements IndexGenerator<GeminiDocument, List<Statement>> {
    private final JenaIndexMetadataDocumentGenerator generator;

    @Override
    public List<Statement> generateIndex(GeminiDocument document) {
        List<Statement> toReturn = generator.generateIndex(document);
        
        Resource me = generator.resource(document.getId());
        toReturn.add(createStatement(me, IDENTIFIER, createPlainLiteral(me.getURI()))); //Add as an identifier of itself
        
        Optional.ofNullable(document.getParentIdentifier()).ifPresent( p -> {
            toReturn.add(createStatement(me, IS_PART_OF, createResource(p)));
        });

        Optional.ofNullable(document.getRevisionOfIdentifier()).ifPresent( r -> {
            toReturn.add(createStatement(me, REPLACES, createPlainLiteral(r)));
        });
        
        document.getResourceIdentifiers().forEach( r -> {
            toReturn.add(createStatement(me, IDENTIFIER, createPlainLiteral(r.getCoupleResource())));
        });
        
        document.getCoupledResources().forEach( r -> {
            toReturn.add(createStatement(me, RELATION, createPlainLiteral(r)));
        });
        
        return toReturn;
    }
}
