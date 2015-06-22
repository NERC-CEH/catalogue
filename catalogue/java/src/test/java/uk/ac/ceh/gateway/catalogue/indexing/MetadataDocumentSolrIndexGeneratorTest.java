package uk.ac.ceh.gateway.catalogue.indexing;

import java.util.Arrays;
import java.util.List;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.indexing.MetadataDocumentSolrIndexGenerator.DocumentSolrIndex;
import uk.ac.ceh.gateway.catalogue.services.CodeLookupService;
import uk.ac.ceh.gateway.catalogue.services.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.services.SolrGeometryService;

/**
 *
 * @author cjohn
 */
public class MetadataDocumentSolrIndexGeneratorTest {
    @Mock CodeLookupService codeLookupService;
    @Mock DocumentIdentifierService documentIdentifierService;
    @Mock SolrGeometryService geometryService;
    private MetadataDocumentSolrIndexGenerator generator;
    
    @Before
    public void createGeminiDocumentSolrIndexGenerator() {
        MockitoAnnotations.initMocks(this);
        generator = new MetadataDocumentSolrIndexGenerator(new ExtractTopicFromDocument(), codeLookupService, documentIdentifierService, geometryService);
    }
    
    @Test
    public void checkThatTopicIsTransferedToIndex() {
        //Given
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getTopics()).thenReturn(Arrays.asList("http://onto.nerc.ac.uk/CEHMD/topic/2","http://onto.nerc.ac.uk/CEHMD/topic/3"));
        List<String> expected = Arrays.asList("0/Biodiversity/", "0/Phenology/");
        
        //When
        DocumentSolrIndex index = generator.generateIndex(document);
        List<String> actual = index.getTopic();
        
        //Then
        assertThat("Actual topic should have required items", actual, equalTo(expected));
    }
    
    @Test
    public void checkThatTitleIsTransferedToIndex() {
        //Given
        GeminiDocument document = new GeminiDocument();
        document.setTitle("my gemini document");
        
        //When
        DocumentSolrIndex index = generator.generateIndex(document);
        
        //Then
        assertEquals("Expected to get my title", "my gemini document", index.getTitle());
    }
    
    @Test
    public void checkThatTitleIdTransferedToIndex() {
        //Given
        String id = "some crazy long, hard to rememember, number";
        when(documentIdentifierService.generateFileId(id)).thenReturn("myid");
        GeminiDocument document = new GeminiDocument();
        document.setId(id);
        
        //When
        DocumentSolrIndex index = generator.generateIndex(document);
        
        //Then
        assertEquals("Expected to get my id", "myid", index.getIdentifier());
    }
    
    @Test
    public void checkThatDescriptionIsTransferedToIndex() {
        //Given
        String description = "Once upon a time, there was a metadata record...";
        GeminiDocument document = new GeminiDocument();
        document.setDescription(description);
        
        //When
        DocumentSolrIndex index = generator.generateIndex(document);
        
        //Then
        assertEquals("Expected to get my description", description, index.getDescription());
    }
    
    @Test
    public void checkThatResourceTypeIsTransferedToIndex() {
        //Given
        GeminiDocument document = new GeminiDocument();
        document.setResourceType(Keyword.builder().value("dataset").build());
        when(codeLookupService.lookup("metadata.resourceType", "dataset")).thenReturn("Dataset");
        
        //When
        DocumentSolrIndex index = generator.generateIndex(document);
        
        //Then
        assertEquals("Expected to get my resourceType", "Dataset", index.getResourceType());
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
        DocumentSolrIndex index = generator.generateIndex(document);
        
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
        DocumentSolrIndex index = generator.generateIndex(document);
        
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
        DocumentSolrIndex index = generator.generateIndex(document);
        
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
        DocumentSolrIndex actual = generator.generateIndex(document);
        
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
        DocumentSolrIndex index = generator.generateIndex(document);
        
        //Then
        assertThat("Expected dataCentre to be empty", index.getDataCentre(), equalTo(""));
    }
    
    @Test
    public void checkThatLongDescriptionWithSpacesIsShortened(){
        //Given
        int maxDescriptionLength = MetadataDocumentSolrIndexGenerator.DocumentSolrIndex.MAX_DESCRIPTION_CHARACTER_LENGTH;
        String description = "Once_upon_a_time,_there_was_a_metadata_description_that_had_to_be_more_than_" + maxDescriptionLength + "_characters_in_length.__It_started_its_life_at_only_30_characters_long,_but_it_ate_its_porridge_every_morning_and_soon_started_to_grow.__After_a_month_it_was_241_characters_in_length.__At_this_stage_Description_Growth_Hormone_(DGH)_really_kicked_in_and_in_now_time_it_was_all_grown_up_happily_exceeded_the_required_number_of_characters_and_ready_to_be_used_for_junit_testing._And_here_is_more_guff._And_here_is_more_guff_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more.";
        MetadataDocumentSolrIndexGenerator.DocumentSolrIndex document = new MetadataDocumentSolrIndexGenerator.DocumentSolrIndex();
        document.setDescription(description);
        
        //Then
        assertThat("Expected description to be longer than the threshold length of " + maxDescriptionLength, maxDescriptionLength, lessThan(description.length()));
        assertThat("Shortened description is shorter than original description", description.length(), greaterThan(document.getShortenedDescription().length()));
    }
    
    @Test
    public void checkThatLongDescriptionWithoutSpacesIsShortened(){
        //Given
        int maxDescriptionLength = MetadataDocumentSolrIndexGenerator.DocumentSolrIndex.MAX_DESCRIPTION_CHARACTER_LENGTH;
        String description = "Once_upon_a_time,_there_was_a_metadata_description_that_had_to_be_more_than_" + maxDescriptionLength + "_characters_in_length.__It_started_its_life_at_only_30_characters_long,_but_it_ate_its_porridge_every_morning_and_soon_started_to_grow.__After_a_month_it_was_241_characters_in_length.__At_this_stage_Description_Growth_Hormone_(DGH)_really_kicked_in_and_in_now_time_it_was_all_grown_up_happily_exceeded_the_required_number_of_characters_and_ready_to_be_used_for_junit_testing._And_here_is_more_guff._And_here_is_more_guff_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more.";
        MetadataDocumentSolrIndexGenerator.DocumentSolrIndex document = new MetadataDocumentSolrIndexGenerator.DocumentSolrIndex();
        document.setDescription(description);

        //Then
        assertThat("Expected description to be longer than the threshold length of " + maxDescriptionLength, maxDescriptionLength, lessThan(description.length()));
        assertThat("Shortened description is shorter than original description", description.length(), greaterThan(document.getShortenedDescription().length()));
    }
    
    @Test
    public void checkThatShortDescriptionIsNotShortened(){
        //Given
        int maxDescriptionLength = MetadataDocumentSolrIndexGenerator.DocumentSolrIndex.MAX_DESCRIPTION_CHARACTER_LENGTH;
        String description = "I am short";
        MetadataDocumentSolrIndexGenerator.DocumentSolrIndex document = new MetadataDocumentSolrIndexGenerator.DocumentSolrIndex();
        document.setDescription(description);
        
        //Then
        assertThat("Expected description to be shorter than the threshold length of " + maxDescriptionLength, maxDescriptionLength, greaterThan(description.length()));
        assertEquals("Shortened description is the same length as the original description", description.length(), document.getShortenedDescription().length());
    }
    
    @Test
    public void checkNullDescriptionGeneratesEmptyStringForShortenedDescription(){
        //Given
        MetadataDocumentSolrIndexGenerator.DocumentSolrIndex document = new MetadataDocumentSolrIndexGenerator.DocumentSolrIndex();
        document.setDescription(null);
        
        //When
        String expected = document.getShortenedDescription();
        
        //Then
        assertThat("Expected shortenedDescription to be empty string" , expected, equalTo(""));
    }

}
