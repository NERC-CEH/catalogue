package uk.ac.ceh.gateway.catalogue.indexing;

import lombok.AllArgsConstructor;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Strings.emptyToNull;
import static org.apache.jena.rdf.model.ResourceFactory.*;
import static uk.ac.ceh.gateway.catalogue.indexing.Ontology.*;

/**
 * The following class extracts semantic details from a GeminiDocument and 
 * returns these as Jena Statements (triples)
 */
@AllArgsConstructor
public class JenaIndexGeminiDocumentGenerator implements IndexGenerator<GeminiDocument, List<Statement>> {
    private final JenaIndexMetadataDocumentGenerator generator;

    @Override
    public List<Statement> generateIndex(GeminiDocument document) {
        List<Statement> toReturn = generator.generateIndex(document);
        
        Resource me = generator.resource(document.getId());
        toReturn.add(createStatement(me, IDENTIFIER, createPlainLiteral(me.getURI()))); //Add as an identifier of itself
        
        Optional.ofNullable(emptyToNull(document.getParentIdentifier())).ifPresent( p -> {
            toReturn.add(createStatement(me, IS_PART_OF, generator.resource(p)));
        });

        Optional.ofNullable(emptyToNull(document.getRevisionOfIdentifier())).ifPresent( r -> {
            toReturn.add(createStatement(me, REPLACES, generator.resource(r)));
        });
        
        Optional.ofNullable(document.getBoundingBoxes()).orElse(Collections.emptyList()).forEach(b -> {
            toReturn.add(createStatement(me, HAS_GEOMETRY, createTypedLiteral(b.getWkt(), WKT_LITERAL)));
        });
        
        Optional.ofNullable(document.getResourceIdentifiers()).orElse(Collections.emptyList())
                .stream()
                .filter(r -> !r.getCoupledResource().isEmpty()).forEach( r -> {
                    toReturn.add(createStatement(me, IDENTIFIER, createPlainLiteral(r.getCoupledResource())));
        });
        
        document.getCoupledResources().stream().filter(r -> !r.isEmpty()).forEach( r -> {
            toReturn.add(createStatement(me, RELATION, createResource(r)));
        });
        
        return toReturn;
    }
}
