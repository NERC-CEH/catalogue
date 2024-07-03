package uk.ac.ceh.gateway.catalogue.indexing.jena;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import uk.ac.ceh.gateway.catalogue.indexing.IndexGenerator;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Strings.emptyToNull;
import static org.apache.jena.rdf.model.ResourceFactory.*;
import static uk.ac.ceh.gateway.catalogue.indexing.jena.Ontology.*;

/**
 * A triple extractor for generic metadata documents.
 */
@Slf4j
@ToString
public class JenaIndexMetadataDocumentGenerator implements IndexGenerator<MetadataDocument, List<Statement>> {
    private final DocumentIdentifierService documentIdentifierService;

    public JenaIndexMetadataDocumentGenerator(DocumentIdentifierService documentIdentifierService) {
        this.documentIdentifierService = documentIdentifierService;
        log.info("Creating");
    }

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
            //TODO add publication status here
            toReturn.add(
                createStatement(
                    me,
                    IDENTIFIER,
                    createPlainLiteral(document.getState())
                )
            );
        }
        return toReturn;
    }

    public Resource resource(String id) {
        if (id.startsWith("http")) {
            return createResource(id);
        } else {
            return createResource(documentIdentifierService.generateUri(id));
        }
    }
}
