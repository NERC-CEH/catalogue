package uk.ac.ceh.gateway.catalogue.indexing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.gateway.catalogue.deims.DeimsSolrIndex;
import uk.ac.ceh.gateway.catalogue.elter.ElterDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.services.CodeLookupService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("Indexing Elter documents into Solr")
@ExtendWith(MockitoExtension.class)
class SolrIndexElterDocumentGeneratorTest {
    @Mock SolrIndexMetadataDocumentGenerator documentIndexer;
    private SolrIndexElterDocumentGenerator generator;

    @BeforeEach
    void init() {
        when(documentIndexer.generateIndex(any(MetadataDocument.class))).thenReturn(new SolrIndex());
        generator = new SolrIndexElterDocumentGenerator(documentIndexer);
    }

    @Test
    public void checkThatElterDeimsSitesTransferedToIndex() {
        //Given
        ElterDocument document = new ElterDocument();
        List<DeimsSolrIndex> deimsSites = new ArrayList<>();
        DeimsSolrIndex deimsSolrIndex = new DeimsSolrIndex();
        deimsSolrIndex.setUrl("https://deims.org/266eedce-b67c-4935-a7b7-4dc3c169c902");
        deimsSolrIndex.setTitle("Aamotsdalen - Norway");
        deimsSites.add(deimsSolrIndex);
        document.setDeimsSites(deimsSites);

        //When
        SolrIndex index = generator.generateIndex(document);

        //Then
        assertEquals("https://deims.org/266eedce-b67c-4935-a7b7-4dc3c169c902", index.getElterDeimsUri().get(0));
        assertEquals("Aamotsdalen - Norway", index.getElterDeimsSite().get(0));
    }
}
