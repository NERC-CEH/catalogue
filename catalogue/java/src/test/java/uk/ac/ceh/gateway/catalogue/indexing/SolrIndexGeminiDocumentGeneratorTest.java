package uk.ac.ceh.gateway.catalogue.indexing;

import com.hp.hpl.jena.rdf.model.Model;
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
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.services.CodeLookupService;
import uk.ac.ceh.gateway.catalogue.services.SolrGeometryService;
import static com.hp.hpl.jena.rdf.model.ResourceFactory.*;
import java.net.URI;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.contains;
import uk.ac.ceh.gateway.catalogue.gemini.DescriptiveKeywords;

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
        when(document.getUseLimitations()).thenReturn(Arrays.asList(
            Keyword.builder()
                .URI("http://eidc.ceh.ac.uk/administration-folder/tools/ceh-standard-licence-texts/ceh-open-government-licence/plain")
                .build(),
            Keyword.builder()
                .URI("http://eidc.ceh.ac.uk/administration-folder/tools/ceh-standard-licence-texts/open-government-licence-non-ceh-data")
                .build(),
            Keyword.builder()
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
        when(document.getUseLimitations()).thenReturn(Arrays.asList(
            Keyword.builder()
                .URI("http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/")
                .build(),
            Keyword.builder()
                .URI("http://eidc.ceh.ac.uk")
                .build(),
            Keyword.builder()
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
        when(document.getUseLimitations()).thenReturn(Arrays.asList(
            Keyword.builder()
                .URI("http://eidc.ceh.ac.uk/metadata/eb7599f4-35f8-4365-bd4a-4056ee6c6083")
                .build(),
            Keyword.builder()
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
    
    @Test
    public void checkThatImpScaleIsIndexed() {
        //Given
        DescriptiveKeywords imp = DescriptiveKeywords.builder()
            .keywords(Arrays.asList(
                Keyword.builder()
                    .value("Catchment")
                    .URI("http://vocabs.ceh.ac.uk/imp/scale/catchment")
                    .build(),
                Keyword.builder()
                    .value("National")
                    .URI("http://vocabs.ceh.ac.uk/imp/scale/national")
                    .build()
            )
        ).build();
        
        DescriptiveKeywords other = DescriptiveKeywords.builder()
            .keywords(Arrays.asList(
                Keyword.builder()
                    .value("Green")
                    .build(),
                Keyword.builder()
                    .value("Blue")
                    .URI("http://example.com/blue")
                    .build()
            )
        ).build();
        
        GeminiDocument document = new GeminiDocument();
        document.setDescriptiveKeywords(Arrays.asList(imp, other));
            
        
        //When
        SolrIndex index = generator.generateIndex(document);
        
        //Then
        assertThat("Expected catchment and national indexed", index.getImpScale(), contains("Catchment", "National"));
        assertThat("Expected to not index Blue", index.getImpScale(), not(contains("Blue")));
        
    }
    
    @Test
    public void checkThatImpBroaderCatchmentIssuesIsIndexed() {
        //Given
        DescriptiveKeywords imp = DescriptiveKeywords.builder()
            .keywords(Arrays.asList(
                Keyword.builder()
                    .value("Agri-environment")
                    .URI("http://vocabs.ceh.ac.uk/imp/bci/agri-environment")
                    .build(),
                Keyword.builder()
                    .value("Ecosystem Response")
                    .URI("http://vocabs.ceh.ac.uk/imp/bci/ecosystem-response")
                    .build()
            )
        ).build();
        
        DescriptiveKeywords other = DescriptiveKeywords.builder()
            .keywords(Arrays.asList(
                Keyword.builder()
                    .value("Green")
                    .build(),
                Keyword.builder()
                    .value("Blue")
                    .URI("http://example.com/blue")
                    .build()
            )
        ).build();
        
        GeminiDocument document = new GeminiDocument();
        document.setDescriptiveKeywords(Arrays.asList(imp, other));
            
        
        //When
        SolrIndex index = generator.generateIndex(document);
        
        //Then
        assertThat("Expected agri-environment and ecosytem response indexed", index.getImpBroaderCatchmentIssues(), contains("Agri-environment", "Ecosystem Response"));
        assertThat("Expected to not index Blue", index.getImpScale(), not(contains("Blue")));
        
    }
    
    @Test
    public void checkThatImpWaterQualityIsIndexed() {
        //Given
        DescriptiveKeywords imp = DescriptiveKeywords.builder()
            .keywords(Arrays.asList(
                Keyword.builder()
                    .value("Nitrogen")
                    .URI("http://vocabs.ceh.ac.uk/imp/wq/nitrogen")
                    .build(),
                Keyword.builder()
                    .value("Phosphorous")
                    .URI("http://vocabs.ceh.ac.uk/imp/wq/phosphorous")
                    .build()
            )
        ).build();
        
        DescriptiveKeywords other = DescriptiveKeywords.builder()
            .keywords(Arrays.asList(
                Keyword.builder()
                    .value("Green")
                    .build(),
                Keyword.builder()
                    .value("Blue")
                    .URI("http://example.com/blue")
                    .build()
            )
        ).build();
        
        GeminiDocument document = new GeminiDocument();
        document.setDescriptiveKeywords(Arrays.asList(imp, other));
            
        
        //When
        SolrIndex index = generator.generateIndex(document);
        
        //Then
        assertThat("Expected nitrogen and phosphorous to be indexed", index.getImpWaterQuality(), contains("Nitrogen", "Phosphorous"));
        assertThat("Expected to not index Blue", index.getImpWaterQuality(), not(contains("Blue")));
        
    }
    
}
