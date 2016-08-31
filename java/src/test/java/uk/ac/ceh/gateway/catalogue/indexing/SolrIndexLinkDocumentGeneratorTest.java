package uk.ac.ceh.gateway.catalogue.indexing;

import java.util.Arrays;
import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.BDDMockito.given;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.LinkDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;

public class SolrIndexLinkDocumentGeneratorTest {
    private @Mock DocumentRepository documentRepository;
    private @Mock IndexGeneratorRegistry<MetadataDocument, SolrIndex> indexGeneratorRegistry;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGenerateIndex() throws Exception {
        //Given
        String id = "c3a62369-4556-4820-8e6b-7b6c962175ea";
        String linkedDocumentId = "b3fdb5cf-7570-4a86-bf4e-5c608eb19af7";
        
        MetadataInfo metadataInfo = new MetadataInfo()
            .setState("draft");
        metadataInfo.addPermission(Permission.VIEW, "test0");
        metadataInfo.addPermission(Permission.VIEW, "test1");
        
        LinkDocument linkDocument = new LinkDocument();
        linkDocument.setId(id);
        linkDocument.setLinkedDocumentId(linkedDocumentId);
        linkDocument.setMetadata(metadataInfo);
        
        GeminiDocument original = new GeminiDocument();
        original.setId(linkedDocumentId);
        
        SolrIndexLinkDocumentGenerator generator = new SolrIndexLinkDocumentGenerator(documentRepository);
        generator.setIndexGeneratorRegistry(indexGeneratorRegistry);
        
        given(documentRepository.read(linkedDocumentId)).willReturn(original);
        given(indexGeneratorRegistry.generateIndex(original)).willReturn(new SolrIndex());
        
        //When
        SolrIndex actual = generator.generateIndex(linkDocument);
        
        
        //Then
        verify(documentRepository).read(linkedDocumentId);
        verify(indexGeneratorRegistry).generateIndex(original);
    }
    
    @Test
    public void additionalKeywordsAddedToSolrIndex() throws Exception {
        //Given
        String id = "c3a62369-4556-4820-8e6b-7b6c962175ea";
        String linkedDocumentId = "b3fdb5cf-7570-4a86-bf4e-5c608eb19af7";
        
        MetadataInfo metadataInfo = new MetadataInfo();
        metadataInfo.setState("draft");
        metadataInfo.addPermission(Permission.VIEW, "test0");
        metadataInfo.addPermission(Permission.VIEW, "test1");
        
        LinkDocument linkDocument = new LinkDocument();
        linkDocument.setId(id);
        linkDocument.setLinkedDocumentId(linkedDocumentId);
        linkDocument.setMetadata(metadataInfo);
        linkDocument.setAdditionalKeywords(Arrays.asList(
            Keyword.builder().value("test09").build(),
            Keyword.builder().value("test23").build()
        ));        
        
        MetadataDocument original = new GeminiDocument().setId(linkedDocumentId);
                
        SolrIndexLinkDocumentGenerator generator = new SolrIndexLinkDocumentGenerator(documentRepository);
        generator.setIndexGeneratorRegistry(indexGeneratorRegistry);
        
        given(documentRepository.read(linkedDocumentId)).willReturn(original);
        given(indexGeneratorRegistry.generateIndex(original)).willReturn(new SolrIndex());
        
        //When
        SolrIndex actual = generator.generateIndex(linkDocument);
        
        //Then
        verify(indexGeneratorRegistry).generateIndex(original);
    }
    
}
