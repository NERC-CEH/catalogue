package uk.ac.ceh.gateway.catalogue.indexing;

import java.io.IOException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ceh.gateway.catalogue.model.LinkDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.services.UnknownContentTypeException;

@Slf4j
public class SolrIndexLinkedDocumentGenerator implements IndexGenerator<LinkDocument, SolrIndex> {
    private final DocumentRepository repository;
    private IndexGeneratorRegistry<MetadataDocument, SolrIndex> indexGeneratorRegistry;

    @Autowired
    public SolrIndexLinkedDocumentGenerator(DocumentRepository repository) {
        this.repository = repository;
    }
    
    public void setIndexGeneratorRegistry(@NonNull IndexGeneratorRegistry indexGeneratorRegistry) {
        this.indexGeneratorRegistry = indexGeneratorRegistry;
    }

    @Override
    public SolrIndex generateIndex(LinkDocument linkDocument) throws DocumentIndexingException {
        try {
            MetadataDocument linked = repository.read(linkDocument.getLinkedDocumentId());
            linked.attachMetadata(linkDocument.getMetadata());
            if (linkDocument.getAdditionalKeywords() != null) {
                linked.addAdditionalKeywords(linkDocument.getAdditionalKeywords());
            } 
            SolrIndex solrIndex = indexGeneratorRegistry.generateIndex(linked);
            solrIndex.setIdentifier(linkDocument.getId());
            return solrIndex;
        } catch (IOException | UnknownContentTypeException | PostProcessingException | NullPointerException ex) {
            throw new DocumentIndexingException(String.format("Unable to index file: %s", linkDocument.getId()), ex);
        }
    }

}
