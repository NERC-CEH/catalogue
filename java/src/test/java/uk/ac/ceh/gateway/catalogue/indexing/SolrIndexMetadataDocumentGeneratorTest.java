package uk.ac.ceh.gateway.catalogue.indexing;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.BDDMockito.given;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.model.Catalogue;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.services.CatalogueService;
import uk.ac.ceh.gateway.catalogue.services.CodeLookupService;
import uk.ac.ceh.gateway.catalogue.services.DocumentIdentifierService;

/**
 *
 * @author cjohn
 */
public class SolrIndexMetadataDocumentGeneratorTest {
    @Mock CodeLookupService codeLookupService;
    @Mock DocumentIdentifierService documentIdentifierService;
    @Mock CatalogueService catalogueService;
    private SolrIndexMetadataDocumentGenerator generator;
    
    @Before
    public void createGeminiDocumentSolrIndexGenerator() {
        MockitoAnnotations.initMocks(this);
        generator = new SolrIndexMetadataDocumentGenerator(
            codeLookupService,
            documentIdentifierService,
            catalogueService
        );
    }
    
    @Test
    public void checkThatTitleIsTransferedToIndex() {
        //Given
        GeminiDocument document = new GeminiDocument();
        document.setTitle("my gemini document");
        
        //When
        SolrIndex index = generator.generateIndex(document);
        
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
        SolrIndex index = generator.generateIndex(document);
        
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
        SolrIndex index = generator.generateIndex(document);
        
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
        SolrIndex index = generator.generateIndex(document);
        
        //Then
        assertEquals("Expected to get my resourceType", "Dataset", index.getResourceType());
    }
    
    @Test
    public void checkThatLongDescriptionWithSpacesIsShortened(){
        //Given
        int maxDescriptionLength = SolrIndex.MAX_DESCRIPTION_CHARACTER_LENGTH;
        String description = "Once_upon_a_time,_there_was_a_metadata_description_that_had_to_be_more_than_" + maxDescriptionLength + "_characters_in_length.__It_started_its_life_at_only_30_characters_long,_but_it_ate_its_porridge_every_morning_and_soon_started_to_grow.__After_a_month_it_was_241_characters_in_length.__At_this_stage_Description_Growth_Hormone_(DGH)_really_kicked_in_and_in_now_time_it_was_all_grown_up_happily_exceeded_the_required_number_of_characters_and_ready_to_be_used_for_junit_testing._And_here_is_more_guff._And_here_is_more_guff_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more.";
        SolrIndex document = new SolrIndex();
        document.setDescription(description);
        
        //Then
        assertThat("Expected description to be longer than the threshold length of " + maxDescriptionLength, maxDescriptionLength, lessThan(description.length()));
        assertThat("Shortened description is shorter than original description", description.length(), greaterThan(document.getShortenedDescription().length()));
    }
    
    @Test
    public void checkThatLongDescriptionWithoutSpacesIsShortened(){
        //Given
        int maxDescriptionLength = SolrIndex.MAX_DESCRIPTION_CHARACTER_LENGTH;
        String description = "Once_upon_a_time,_there_was_a_metadata_description_that_had_to_be_more_than_" + maxDescriptionLength + "_characters_in_length.__It_started_its_life_at_only_30_characters_long,_but_it_ate_its_porridge_every_morning_and_soon_started_to_grow.__After_a_month_it_was_241_characters_in_length.__At_this_stage_Description_Growth_Hormone_(DGH)_really_kicked_in_and_in_now_time_it_was_all_grown_up_happily_exceeded_the_required_number_of_characters_and_ready_to_be_used_for_junit_testing._And_here_is_more_guff._And_here_is_more_guff_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more.";
        SolrIndex document = new SolrIndex();
        document.setDescription(description);

        //Then
        assertThat("Expected description to be longer than the threshold length of " + maxDescriptionLength, maxDescriptionLength, lessThan(description.length()));
        assertThat("Shortened description is shorter than original description", description.length(), greaterThan(document.getShortenedDescription().length()));
    }
    
    @Test
    public void checkThatShortDescriptionIsNotShortened(){
        //Given
        int maxDescriptionLength = SolrIndex.MAX_DESCRIPTION_CHARACTER_LENGTH;
        String description = "I am short";
        SolrIndex document = new SolrIndex();
        document.setDescription(description);
        
        //Then
        assertThat("Expected description to be shorter than the threshold length of " + maxDescriptionLength, maxDescriptionLength, greaterThan(description.length()));
        assertEquals("Shortened description is the same length as the original description", description.length(), document.getShortenedDescription().length());
    }
    
    @Test
    public void checkNullDescriptionGeneratesEmptyStringForShortenedDescription(){
        //Given
        SolrIndex document = new SolrIndex();
        document.setDescription(null);
        
        //When
        String expected = document.getShortenedDescription();
        
        //Then
        assertThat("Expected shortenedDescription to be empty string" , expected, equalTo(""));
    }
    
    @Test
    public void checkThatCatalogueTitleIsTransferedToIndex() {
        //Given
        Catalogue catalogue = Catalogue
            .builder()
            .id("eidc")
            .title("Environmental Information Data Centre")
            .url("https://eidc-catalogue.ceh.ac.uk")
            .build();
        MetadataInfo info = new MetadataInfo().setCatalogue("eidc");
        MetadataDocument document = new GeminiDocument().setMetadata(info);
        given(catalogueService.retrieve("eidc")).willReturn(catalogue);
        
        //When
        SolrIndex index = generator.generateIndex(document);

        //Then
        assertThat(
                "Expected to get \"Environmental Information Data Centre\"",
                index.getCatalogue(),
                equalTo("Environmental Information Data Centre")
        );
    }

}
