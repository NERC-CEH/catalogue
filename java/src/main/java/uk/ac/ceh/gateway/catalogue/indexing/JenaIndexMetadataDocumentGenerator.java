package uk.ac.ceh.gateway.catalogue.indexing;

import static com.google.common.base.Strings.emptyToNull;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import static org.apache.jena.rdf.model.ResourceFactory.*;
import static uk.ac.ceh.gateway.catalogue.indexing.Ontology.*;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.services.DocumentIdentifierService;

/**
 * A triple extractor for generic metadata documents.
 * @author cjohn
 */
@Data
public class JenaIndexMetadataDocumentGenerator implements IndexGenerator<MetadataDocument, List<Statement>> {
    private final DocumentIdentifierService documentIdentifierService;

    @Override
    public List<Statement> generateIndex(MetadataDocument document) {
        List<Statement> toReturn = new ArrayList<>();
        if(emptyToNull(document.getId()) != null) {
            Resource me = resource(document.getId());
            toReturn.add(
                createStatement(
                    me,
                    IDENTIFIER,
                    createPlainLiteral(document.getId())
                )
            );
            
            Optional.ofNullable(emptyToNull(document.getTitle()))
                    .ifPresent(t -> toReturn.add(
                        createStatement(me, TITLE, createPlainLiteral(t)))
                    );
            
            Optional.ofNullable(emptyToNull(document.getType()))
                    .ifPresent(t -> toReturn.add(
                        createStatement(me, TYPE, createPlainLiteral(t)))
                    );
            
            Optional.ofNullable(document.getRelationships())
                .ifPresent(relationships -> {
                    relationships.forEach(rel -> {
                        toReturn.add(
                            createStatement(
                                me,
                                createProperty(rel.getRelation()),
                                resource(rel.getTarget())
                            )
                        );
                    });
                });
        }
        return toReturn;
    }
    
    public Resource resource(String id) {
        return createResource(documentIdentifierService.generateUri(id));
    }
}
