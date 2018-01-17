package uk.ac.ceh.gateway.catalogue.indexing;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ceh.gateway.catalogue.model.LinkDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;
import uk.ac.ceh.gateway.catalogue.services.UnknownContentTypeException;

public class SolrIndexLinkDocumentGenerator implements IndexGenerator<LinkDocument, SolrIndex> {
    @Setter(onMethod = @__({@Autowired}))
    private DocumentRepository repository;
    @Setter(onMethod = @__({@Autowired}))
    private IndexGeneratorRegistry<MetadataDocument, SolrIndex> indexGeneratorRegistry;

    @Override
    public SolrIndex generateIndex(LinkDocument linkDocument) throws DocumentIndexingException {
        try {
            MetadataDocument linked = repository.read(linkDocument.getLinkedDocumentId());
            linked.setMetadata(linkDocument.getMetadata());
            if (linkDocument.getAdditionalKeywords() != null) {
                linked.addAdditionalKeywords(linkDocument.getAdditionalKeywords());
            } 
            SolrIndex solrIndex = indexGeneratorRegistry.generateIndex(linked);
            solrIndex.setIdentifier(linkDocument.getId());
            return solrIndex;
        } catch (DocumentRepositoryException | UnknownContentTypeException | NullPointerException ex) {
            throw new DocumentIndexingException(String.format("Unable to index file: %s", linkDocument.getId()), ex);
        }
    }

}
