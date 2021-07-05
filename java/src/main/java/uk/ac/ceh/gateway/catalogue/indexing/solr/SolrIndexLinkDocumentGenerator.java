package uk.ac.ceh.gateway.catalogue.indexing.solr;

import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.IndexGenerator;
import uk.ac.ceh.gateway.catalogue.model.LinkDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;
import uk.ac.ceh.gateway.catalogue.services.UnknownContentTypeException;

@Slf4j
@ToString
public class SolrIndexLinkDocumentGenerator implements IndexGenerator<LinkDocument, SolrIndex> {
    @Setter private DocumentRepository repository;
    @Setter private IndexGeneratorRegistry<MetadataDocument, SolrIndex> indexGeneratorRegistry;
    // Cannot be final as involved in complex construction with SolrIndexMetadataDocumentGenerator in WebConfig

    public SolrIndexLinkDocumentGenerator() {
        log.info("Creating {}", this);
    }

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
