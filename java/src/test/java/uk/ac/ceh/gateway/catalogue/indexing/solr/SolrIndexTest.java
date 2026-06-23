package uk.ac.ceh.gateway.catalogue.indexing.solr;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SolrIndexTest {

    @SuppressWarnings("ConstantConditions")
    @Test
    void checkThatLongDescriptionWithSpacesIsShortened() {
        //Given
        int maxDescriptionLength = SolrIndex.MAX_DESCRIPTION_CHARACTER_LENGTH;
        String description = "Once_upon_a_time,_there_was_a_metadata_description_that_had_to_be_more_than_" + maxDescriptionLength + "_characters_in_length.__It_started_its_life_at_only_30_characters_long,_but_it_ate_its_porridge_every_morning_and_soon_started_to_grow.__After_a_month_it_was_241_characters_in_length.__At_this_stage_Description_Growth_Hormone_(DGH)_really_kicked_in_and_in_now_time_it_was_all_grown_up_happily_exceeded_the_required_number_of_characters_and_ready_to_be_used_for_junit_testing._And_here_is_more_guff._And_here_is_more_guff_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more.";
        SolrIndex document = new SolrIndex();
        document.setDescription(description);

        //Then
        assertTrue(description.length() > maxDescriptionLength);
        assertTrue(description.length() > document.getShortenedDescription().length());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void checkThatLongDescriptionWithoutSpacesIsShortened() {
        //Given
        int maxDescriptionLength = SolrIndex.MAX_DESCRIPTION_CHARACTER_LENGTH;
        String description = "Once_upon_a_time,_there_was_a_metadata_description_that_had_to_be_more_than_" + maxDescriptionLength + "_characters_in_length.__It_started_its_life_at_only_30_characters_long,_but_it_ate_its_porridge_every_morning_and_soon_started_to_grow.__After_a_month_it_was_241_characters_in_length.__At_this_stage_Description_Growth_Hormone_(DGH)_really_kicked_in_and_in_now_time_it_was_all_grown_up_happily_exceeded_the_required_number_of_characters_and_ready_to_be_used_for_junit_testing._And_here_is_more_guff._And_here_is_more_guff_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more_and_more.";
        SolrIndex document = new SolrIndex();
        document.setDescription(description);

        //Then
        assertTrue(description.length() > maxDescriptionLength);
        assertTrue(description.length() > document.getShortenedDescription().length());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void checkThatShortDescriptionIsNotShortened() {
        //Given
        int maxDescriptionLength = SolrIndex.MAX_DESCRIPTION_CHARACTER_LENGTH;
        String description = "I am short";
        SolrIndex document = new SolrIndex();
        document.setDescription(description);

        //Then
        assertTrue(description.length() < maxDescriptionLength);
        assertThat(description.length(), equalTo(document.getShortenedDescription().length()));
    }

    @Test
    void checkNullDescriptionGeneratesEmptyStringForShortenedDescription() {
        //Given
        SolrIndex document = new SolrIndex();
        document.setDescription(null);

        //When
        String expected = document.getShortenedDescription();

        //Then
        assertThat("Expected shortenedDescription to be empty string", expected, equalTo(""));
    }
}
