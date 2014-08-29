package uk.ac.ceh.gateway.catalogue.gemini;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocumentSolrIndexGenerator.DocumentSolrIndex;
import uk.ac.ceh.gateway.catalogue.gemini.elements.CodeListItem;
import uk.ac.ceh.gateway.catalogue.gemini.elements.DownloadOrder;

/**
 *
 * @author cjohn
 */
public class GeminiDocumentSolrIndexGeneratorTest {
    private GeminiDocumentSolrIndexGenerator generator;
    
    @Before
    public void createGeminiDocumentSolrIndexGenerator() {
        generator = new GeminiDocumentSolrIndexGenerator();
    }
    
    @Test
    public void checkThatTitleIsTransferedToIndex() {
        //Given
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getTitle()).thenReturn("my gemini document");
        
        //When
        DocumentSolrIndex index = generator.generateIndex(document);
        
        //Then
        assertEquals("Expected to get my title", "my gemini document", index.getTitle());
    }
    
    @Test
    public void checkThatTitleIdTransferedToIndex() {
        //Given
        String id = "some crazy long, hard to rememember, number";
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getId()).thenReturn(id);
        
        //When
        DocumentSolrIndex index = generator.generateIndex(document);
        
        //Then
        assertEquals("Expected to get my id", id, index.getIdentifier());
    }
    
    @Test
    public void checkThatDescriptionIsTransferedToIndex() {
        //Given
        String description = "Once upon a time, there was a metadata record...";
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getDescription()).thenReturn(description);
        
        //When
        DocumentSolrIndex index = generator.generateIndex(document);
        
        //Then
        assertEquals("Expected to get my description", description, index.getDescription());
    }
    
    @Test
    public void checkThatResourceTypeIsTransferedToIndex() {
        //Given
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getType()).thenReturn("dataset");
        
        //When
        DocumentSolrIndex index = generator.generateIndex(document);
        
        //Then
        assertEquals("Expected to get my resourceType", "dataset", index.getResourceType());
    }
    
    @Test
    public void checkThatIsOglTrueIsTransferredToIndex(){
        //Given
        DownloadOrder downloadOrder = DownloadOrder
                .builder()
                .licenseUrl("http://eidchub.ceh.ac.uk/administration-folder/tools/ceh-standard-licence-texts/ceh-open-government-licence/plain")
                .build();
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getDownloadOrder()).thenReturn(downloadOrder);
        
        //When
        DocumentSolrIndex index = generator.generateIndex(document);
        
        //Then
        assertEquals("Expected isOgl to be true", true, index.getIsOgl());
    }
    
    @Test
    public void checkThatIsOglFalseIsTransferredToIndex(){
        //Given
        DownloadOrder downloadOrder = DownloadOrder
                .builder()
                .licenseUrl("http://I.am.a.non.ogl.license")
                .build();
        GeminiDocument document = mock(GeminiDocument.class);
        when(document.getDownloadOrder()).thenReturn(downloadOrder);
        
        //When
        DocumentSolrIndex index = generator.generateIndex(document);
        
        //Then
        assertEquals("Expected isOgl to be false", false, index.getIsOgl());
    }
    
    @Test
    public void checkThatLongDescriptionWithSpacesIsShortened(){
        //Given
        int maxDescriptionLength = GeminiDocumentSolrIndexGenerator.DocumentSolrIndex.MAX_DESCRIPTION_CHARACTER_LENGTH;
        String description = "Once_upon_a_time,_there_was_a_metadata_description_that_had_to_be_more_than_" + maxDescriptionLength + "_characters_in_length.__It_started_its_life_at_only_30_characters_long,_but_it_ate_its_porridge_every_morning_and_soon_started_to_grow.__After_a_month_it_was_241_characters_in_length.__At_this_stage_Description_Growth_Hormone_(DGH)_really_kicked_in_and_in_now_time_it_was_all_grown_up_happily_exceeded_the_required_number_of_characters_and_ready_to_be_used_for_junit_testing._And_here_is_more_guff._And_here_is_more_guff_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more.";
        GeminiDocumentSolrIndexGenerator.DocumentSolrIndex document = new GeminiDocumentSolrIndexGenerator.DocumentSolrIndex();
        document.setDescription(description);
        
        //Then
        assertThat("Expected description to be longer than the threshold length of " + maxDescriptionLength, maxDescriptionLength, lessThan(description.length()));
        assertThat("Shortened description is shorter than original description", description.length(), greaterThan(document.getShortenedDescription().length()));
    }
    
    @Test
    public void checkThatLongDescriptionWithoutSpacesIsShortened(){
        //Given
        int maxDescriptionLength = GeminiDocumentSolrIndexGenerator.DocumentSolrIndex.MAX_DESCRIPTION_CHARACTER_LENGTH;
        String description = "Once_upon_a_time,_there_was_a_metadata_description_that_had_to_be_more_than_" + maxDescriptionLength + "_characters_in_length.__It_started_its_life_at_only_30_characters_long,_but_it_ate_its_porridge_every_morning_and_soon_started_to_grow.__After_a_month_it_was_241_characters_in_length.__At_this_stage_Description_Growth_Hormone_(DGH)_really_kicked_in_and_in_now_time_it_was_all_grown_up_happily_exceeded_the_required_number_of_characters_and_ready_to_be_used_for_junit_testing._And_here_is_more_guff._And_here_is_more_guff_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more.";
        GeminiDocumentSolrIndexGenerator.DocumentSolrIndex document = new GeminiDocumentSolrIndexGenerator.DocumentSolrIndex();
        document.setDescription(description);

        //Then
        assertThat("Expected description to be longer than the threshold length of " + maxDescriptionLength, maxDescriptionLength, lessThan(description.length()));
        assertThat("Shortened description is shorter than original description", description.length(), greaterThan(document.getShortenedDescription().length()));
    }
    
    @Test
    public void checkThatShortDescriptionIsNotShortened(){
        //Given
        int maxDescriptionLength = GeminiDocumentSolrIndexGenerator.DocumentSolrIndex.MAX_DESCRIPTION_CHARACTER_LENGTH;
        String description = "I am short";
        GeminiDocumentSolrIndexGenerator.DocumentSolrIndex document = new GeminiDocumentSolrIndexGenerator.DocumentSolrIndex();
        document.setDescription(description);
        
        //Then
        assertThat("Expected description to be shorter than the threshold length of " + maxDescriptionLength, maxDescriptionLength, greaterThan(description.length()));
        assertEquals("Shortened description is the same length as the original description", description.length(), document.getShortenedDescription().length());
    }

}
