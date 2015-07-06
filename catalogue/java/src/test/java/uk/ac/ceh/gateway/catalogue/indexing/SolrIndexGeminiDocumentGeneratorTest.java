package uk.ac.ceh.gateway.catalogue.indexing;

import java.util.Arrays;
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
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.ResponsibleParty;
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
        generator = new SolrIndexGeminiDocumentGenerator(documentIndexer, geometryService, codeLookupService);
    }
    
    @Test
    public void checkThatIsOglTrueIsTransferredToIndex(){
        //Given
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getUseLimitations()).thenReturn(Arrays.asList(
            Keyword.builder()
                .URI("http://eidchub.ceh.ac.uk/administration-folder/tools/ceh-standard-licence-texts/ceh-open-government-licence/plain")
                .build(),
            Keyword.builder()
                .URI("http://eidchub.ceh.ac.uk/administration-folder/tools/ceh-standard-licence-texts/open-government-licence-non-ceh-data")
                .build(),
            Keyword.builder()
                .value("More use limitations")
                .build()
        ));
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
        when(document.getUseLimitations()).thenReturn(Arrays.asList(
            Keyword.builder()
                .URI("http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/")
                .build(),
            Keyword.builder()
                .URI("http://eidchub.ceh.ac.uk")
                .build(),
            Keyword.builder()
                .value("More use limitations")
                .build()
        ));
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
        when(document.getUseLimitations()).thenReturn(Arrays.asList(
            Keyword.builder()
                .URI("http://eidchub.ceh.ac.uk/metadata/eb7599f4-35f8-4365-bd4a-4056ee6c6083")
                .build(),
            Keyword.builder()
                .value("More use limitations")
                .build()
        ));
        when(codeLookupService.lookup("licence.isOgl", false)).thenReturn("ISNT OGL");
        
        //When
        SolrIndex index = generator.generateIndex(document);
        
        //Then
        assertEquals("Expected isOgl to be false", "ISNT OGL", index.getLicence());
    }
    
    @Test
    public void checkThatEIDCCustodianIsIndexed(){
        //Given
        ResponsibleParty custodian = ResponsibleParty.builder().role("custodian").organisationName("EIDC Hub").build();
        GeminiDocument document = new GeminiDocument();
        document.setResponsibleParties(Arrays.asList(custodian));
        
        //When
        SolrIndex actual = generator.generateIndex(document);
        
        //Then
        assertThat("Expected dataCentre to be EIDCHub", actual.getDataCentre(), equalTo("EIDCHub"));
    }
    
    @Test
    public void checkThatEIDCCustodianIsNotIndexed(){
        //Given
        ResponsibleParty custodian = ResponsibleParty.builder().role("author").organisationName("Test").build();
        GeminiDocument document = new GeminiDocument();
        document.setResponsibleParties(Arrays.asList(custodian));
        
        //When
        SolrIndex index = generator.generateIndex(document);
        
        //Then
        assertThat("Expected dataCentre to be empty", index.getDataCentre(), equalTo(""));
    }
    
}
