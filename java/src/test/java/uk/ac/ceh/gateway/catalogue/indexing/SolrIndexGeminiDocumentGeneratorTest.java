package uk.ac.ceh.gateway.catalogue.indexing;

import java.util.Arrays;
import java.util.List;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceConstraint;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.services.CodeLookupService;
import uk.ac.ceh.gateway.catalogue.services.SolrGeometryService;

/**
 *
 * @author cjohn
 */
public class SolrIndexGeminiDocumentGeneratorTest {
    @Mock SolrIndexMetadataDocumentGenerator documentIndexer;
    @Mock SolrGeometryService geometryService;
    @Mock CodeLookupService codeLookupService;
    private SolrIndexGeminiDocumentGenerator generator;
    
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        when(documentIndexer.generateIndex(any(MetadataDocument.class))).thenReturn(new SolrIndex());
        generator = new SolrIndexGeminiDocumentGenerator(new ExtractTopicFromDocument(), documentIndexer, geometryService, codeLookupService);
    }
    
    @Test
    public void checkThatTopicIsTransferedToIndex() {
        //Given
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getTopics()).thenReturn(Arrays.asList("http://onto.nerc.ac.uk/CEHMD/topic/2","http://onto.nerc.ac.uk/CEHMD/topic/3"));
        when(document.getId()).thenReturn("123");
        List<String> expected = Arrays.asList("0/Biodiversity/", "0/Phenology/");
        
        //When
        SolrIndex index = generator.generateIndex(document);
        List<String> actual = index.getTopic();
        
        //Then
        assertThat("Actual topic should have required items", actual, equalTo(expected));
    }
    
    @Test
    public void checkThatIsOglTrueIsTransferredToIndex(){
        //Given
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getUseConstraints()).thenReturn(Arrays.asList(
            ResourceConstraint.builder()
                .uri("http://eidc.ceh.ac.uk/administration-folder/tools/ceh-standard-licence-texts/ceh-open-government-licence/plain")
                .build(),
            ResourceConstraint.builder()
                .uri("http://eidc.ceh.ac.uk/administration-folder/tools/ceh-standard-licence-texts/open-government-licence-non-ceh-data")
                .build(),
            ResourceConstraint.builder()
                .value("More use limitations")
                .build()
        ));
        when(document.getId()).thenReturn("123");
        when(codeLookupService.lookup("licence.isOgl", true)).thenReturn("IS OGL");
        
        //When
        SolrIndex index = generator.generateIndex(document);
        
        //Then
        assertEquals("Expected isOgl to be true", "IS OGL", index.getLicence());
    }
    
    @Test
    public void checkThatIsOglTrueIsTransferredToIndexForOtherFormatOfUrl(){
        //Given
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getUseConstraints()).thenReturn(Arrays.asList(
            ResourceConstraint.builder()
                .uri("http://eidc.ceh.ac.uk/administration-folder/tools/ceh-standard-licence-texts/open-government-licence-non-ceh-data")
                .build(),
            ResourceConstraint.builder()
                .value("More use limitations")
                .build()
        ));
        when(document.getId()).thenReturn("123");
        when(codeLookupService.lookup("licence.isOgl", true)).thenReturn("IS OGL");
        
        //When
        SolrIndex index = generator.generateIndex(document);
        
        //Then
        assertEquals("Expected isOgl to be true", "IS OGL", index.getLicence());
    }
    
    @Test
    public void checkThatIsOglTrueIsTransferredToIndexForOtherFormatOfUrlForAnother(){
        //Given
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getUseConstraints()).thenReturn(Arrays.asList(
            ResourceConstraint.builder()
                .uri("http://eidc.ceh.ac.uk/administration-folder/tools/ceh-standard-licence-texts/OGLnonceh/plain")
                .build(),
            ResourceConstraint.builder()
                .value("More use limitations")
                .build()
        ));
        when(document.getId()).thenReturn("123");
        when(codeLookupService.lookup("licence.isOgl", true)).thenReturn("IS OGL");
        
        //When
        SolrIndex index = generator.generateIndex(document);
        
        //Then
        assertEquals("Expected isOgl to be true", "IS OGL", index.getLicence());
    }
    
    @Test
    public void checkThatIsNationalArchivesOglTrueIsTransferredToIndex(){
        //Given
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getUseConstraints()).thenReturn(Arrays.asList(
            ResourceConstraint.builder()
                .uri("http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/")
                .build(),
            ResourceConstraint.builder()
                .uri("http://eidc.ceh.ac.uk")
                .build(),
            ResourceConstraint.builder()
                .value("More use limitations")
                .build()
        ));
        when(document.getId()).thenReturn("123");
        when(codeLookupService.lookup("licence.isOgl", true)).thenReturn("IS OGL");
        
        //When
        SolrIndex index = generator.generateIndex(document);
        
        //Then
        assertEquals("Expected isOgl to be true", "IS OGL", index.getLicence());
    }
    
    @Test
    public void checkThatIsOglFalseIsTransferredToIndex(){
        //Given
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getUseConstraints()).thenReturn(Arrays.asList(
            ResourceConstraint.builder()
                .uri("http://eidc.ceh.ac.uk/metadata/eb7599f4-35f8-4365-bd4a-4056ee6c6083")
                .build(),
            ResourceConstraint.builder()
                .value("More use limitations")
                .build()
        ));
        when(document.getId()).thenReturn("123");
        when(codeLookupService.lookup("licence.isOgl", false)).thenReturn("ISNT OGL");
        
        //When
        SolrIndex index = generator.generateIndex(document);
        
        //Then
        assertEquals("Expected isOgl to be false", "ISNT OGL", index.getLicence());
    }
    
}
