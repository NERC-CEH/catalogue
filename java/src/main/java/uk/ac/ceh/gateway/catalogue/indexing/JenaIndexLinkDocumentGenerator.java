package uk.ac.ceh.gateway.catalogue.indexing;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import uk.ac.ceh.gateway.catalogue.model.LinkDocument;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Strings.emptyToNull;
import static org.apache.jena.rdf.model.ResourceFactory.createStatement;
import static uk.ac.ceh.gateway.catalogue.indexing.Ontology.SOURCE;

@Slf4j
@ToString
public class JenaIndexLinkDocumentGenerator implements IndexGenerator<LinkDocument, List<Statement>> {
    private final JenaIndexMetadataDocumentGenerator generator;

    public JenaIndexLinkDocumentGenerator(JenaIndexMetadataDocumentGenerator generator) {
        this.generator = generator;
        log.info("Creating {}", this);
    }

    @Override
    public List<Statement> generateIndex(LinkDocument document) throws DocumentIndexingException {
        List<Statement> toReturn = generator.generateIndex(document);
        
        Resource me = generator.resource(document.getId());
        Optional.ofNullable(emptyToNull(document.getLinkedDocumentId())).ifPresent( ld -> {
            toReturn.add(createStatement(me, SOURCE, generator.resource(ld)));
        });
        
        return toReturn;
    }

}
