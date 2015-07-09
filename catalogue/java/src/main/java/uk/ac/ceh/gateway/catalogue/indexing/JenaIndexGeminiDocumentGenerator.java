package uk.ac.ceh.gateway.catalogue.indexing;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import java.util.List;
import lombok.Data;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import static uk.ac.ceh.gateway.catalogue.indexing.Ontology.*;

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
        if(document.getParentIdentifier() != null) {
            toReturn.add(ResourceFactory.createStatement(me, IS_PART_OF, generator.resource(document.getParentIdentifier())));
        }

        document.getResourceIdentifiers().forEach( r -> {
            toReturn.add(ResourceFactory.createStatement(me, IDENTIFIER, ResourceFactory.createPlainLiteral(r.getCoupleResource())));
        });
        
        return toReturn;
    }
}
