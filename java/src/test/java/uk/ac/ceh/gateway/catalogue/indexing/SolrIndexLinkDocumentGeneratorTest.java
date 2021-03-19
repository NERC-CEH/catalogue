package uk.ac.ceh.gateway.catalogue.indexing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.model.LinkDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import java.util.Arrays;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class SolrIndexLinkDocumentGeneratorTest {
    private @Mock DocumentRepository documentRepository;
    private @Mock IndexGeneratorRegistry<MetadataDocument, SolrIndex> indexGeneratorRegistry;

    @Test
    public void testGenerateIndex() throws Exception {
        //Given
        String id = "c3a62369-4556-4820-8e6b-7b6c962175ea";
        String linkedDocumentId = "b3fdb5cf-7570-4a86-bf4e-5c608eb19af7";
        
        MetadataInfo metadataInfo = MetadataInfo.builder().state("draft").build();
        metadataInfo.addPermission(Permission.VIEW, "test0");
        metadataInfo.addPermission(Permission.VIEW, "test1");
        
        LinkDocument linkDocument = LinkDocument.builder().linkedDocumentId(linkedDocumentId).build();
        linkDocument.setId(id);
        linkDocument.setMetadata(metadataInfo);
        
        GeminiDocument original = new GeminiDocument();
        original.setId(linkedDocumentId);
        
        SolrIndexLinkDocumentGenerator generator = new SolrIndexLinkDocumentGenerator();
        generator.setRepository(documentRepository);
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
        
        MetadataInfo metadataInfo = MetadataInfo.builder().state("draft").build();
        metadataInfo.addPermission(Permission.VIEW, "test0");
        metadataInfo.addPermission(Permission.VIEW, "test1");
        
        LinkDocument linkDocument = LinkDocument.builder()
            .linkedDocumentId(linkedDocumentId)
            .additionalKeywords(Arrays.asList(
                Keyword.builder().value("test09").build(),
                Keyword.builder().value("test23").build()
            ))
            .build();
        linkDocument.setId(id);
        linkDocument.setMetadata(metadataInfo);       
        
        MetadataDocument original = new GeminiDocument().setId(linkedDocumentId);
                
        SolrIndexLinkDocumentGenerator generator = new SolrIndexLinkDocumentGenerator();
        generator.setRepository(documentRepository);
        generator.setIndexGeneratorRegistry(indexGeneratorRegistry);
        
        given(documentRepository.read(linkedDocumentId)).willReturn(original);
        given(indexGeneratorRegistry.generateIndex(original)).willReturn(new SolrIndex());
        
        //When
        SolrIndex actual = generator.generateIndex(linkDocument);
        
        //Then
        verify(indexGeneratorRegistry).generateIndex(original);
    }
    
}
