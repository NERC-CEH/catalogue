package uk.ac.ceh.gateway.catalogue.indexing;

import static com.google.common.base.Strings.emptyToNull;
import java.util.List;
import java.util.Optional;
import org.apache.jena.rdf.model.Resource;
import static org.apache.jena.rdf.model.ResourceFactory.createStatement;
import org.apache.jena.rdf.model.Statement;
import static uk.ac.ceh.gateway.catalogue.indexing.Ontology.SOURCE;
import uk.ac.ceh.gateway.catalogue.model.LinkDocument;

public class JenaIndexLinkDocumentGenerator implements IndexGenerator<LinkDocument, List<Statement>> {
    private final JenaIndexMetadataDocumentGenerator generator;

    public JenaIndexLinkDocumentGenerator(JenaIndexMetadataDocumentGenerator generator) {
        this.generator = generator;
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
