package uk.ac.ceh.gateway.catalogue.sparql;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class SparqlVocabularyServiceTest {

    @Test
    public void keywordIsMemberOfTopConcept() {
        //given
        Multimap<String, String> vocabulary = ArrayListMultimap.create();
        vocabulary.put("http://vocabs.ceh.ac.uk/ncterms/drivers", "http://vocabs.ceh.ac.uk/ncterms/climate_change");
        VocabularyService vocabularyService = new SparqlVocabularyService(vocabulary);

        //when
        boolean actual = vocabularyService.isMember(
            "http://vocabs.ceh.ac.uk/ncterms/drivers",
            "http://vocabs.ceh.ac.uk/ncterms/climate_change"
        );

        //then
        assertThat("Should be true", actual, is(true));
    }

    @Test
    public void keywordIsNotMemberOfTopConcept() {
        //given
        Multimap<String, String> vocabulary = ArrayListMultimap.create();
        vocabulary.put("http://vocabs.ceh.ac.uk/ncterms/drivers", "http://vocabs.ceh.ac.uk/ncterms/climate_change");
        VocabularyService vocabularyService = new SparqlVocabularyService(vocabulary);

        //when
        boolean actual = vocabularyService.isMember(
            "http://vocabs.ceh.ac.uk/ncterms/drivers",
            "http://example.com/test"
        );

        //then
        assertThat("Should be false", actual, is(false));
    }

    @Test
    public void unknownTopConcept() {
        //given
        Multimap<String, String> vocabulary = ArrayListMultimap.create();
        vocabulary.put("http://vocabs.ceh.ac.uk/ncterms/drivers", "http://vocabs.ceh.ac.uk/ncterms/climate_change");
        VocabularyService vocabularyService = new SparqlVocabularyService(vocabulary);

        //when
        boolean actual = vocabularyService.isMember(
            "http://example.com/test",
            "http://example.com/ncterms/climate_change"
        );

        //then
        assertThat("Should be false", actual, is(false));
    }
}
