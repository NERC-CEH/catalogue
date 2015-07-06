package uk.ac.ceh.gateway.catalogue.indexing;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.services.DocumentIdentifierService;

/**
 *
 * @author cjohn
 * @param <D>
 */
@Data
public class JenaIndexMetadataDocumentGenerator implements IndexGenerator<MetadataDocument, List<Statement>> {
    public static final Property IDENTIFIER = ResourceFactory.createProperty("http://purl.org/dc/terms/identifier");
    public static final Property TITLE = ResourceFactory.createProperty("http://purl.org/dc/terms/title");
    public static final Property TYPE = ResourceFactory.createProperty("http://purl.org/dc/terms/type");
    public static final Property PARENT = ResourceFactory.createProperty("http://purl.org/dc/terms/isPartOf");
    
    private final DocumentIdentifierService documentIdentifierService;

    @Override
    public List<Statement> generateIndex(MetadataDocument document) {
        List<Statement> toReturn = new ArrayList<>();
        
        Resource me = resource(document.getId());
        toReturn.add(ResourceFactory.createStatement(me, IDENTIFIER, ResourceFactory.createPlainLiteral(document.getId())));
        toReturn.add(ResourceFactory.createStatement(me, TITLE, ResourceFactory.createPlainLiteral(document.getTitle())));
        toReturn.add(ResourceFactory.createStatement(me, TYPE, ResourceFactory.createPlainLiteral(document.getType())));
        return toReturn;
    }
    
    public Resource resource(String fileIdentifier) {
        String uri = "https://catalogue.ceh.ac.uk/id/" + documentIdentifierService.generateFileId(fileIdentifier);
        return ResourceFactory.createResource(uri);
    }
}
