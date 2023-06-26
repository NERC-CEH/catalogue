package uk.ac.ceh.gateway.catalogue.vocabularies;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockCatalogueUser
@ActiveProfiles({"server:elter", "test"})
@DisplayName("KeywordVocabularyController")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})
@WebMvcTest(KeywordVocabularyController.class)
class KeywordVocabularyControllerTest {

    public static final String LABEL_1 = "assist-topics";
    public static final String LABEL_2 = "label";
    public static final String VOCAB_ID_1 = "ASSIST-TOPICS";
    public static final String VOCAB_ID_2 = "vocab";
    public static final String URL_1 = "www.example.com";
    public static final String URL_2 = "www.example.com/2";
    public static final String QUERY = "queryTest";

    @MockBean
    private KeywordVocabularySolrQueryService keywordService;

    @Autowired
    private MockMvc mvc;

    @SneakyThrows
    private void givenQueryResponse() {
        val keywordSolrIndex1 = new Keyword(LABEL_1, VOCAB_ID_1, URL_1);
        val keywordSolrIndex2 = new Keyword(LABEL_2, VOCAB_ID_2, URL_2);

        List<String> vocabularyIds = new ArrayList<>();
        vocabularyIds.add(VOCAB_ID_1);
        vocabularyIds.add(VOCAB_ID_2);

        given(keywordService.query(QUERY, vocabularyIds))
                .willReturn(Arrays.asList(
                        keywordSolrIndex1,
                        keywordSolrIndex2
                ));
    }

    @SneakyThrows
    private void givenQueryResponseNoQuery() {
        val keywordSolrIndex1 = new Keyword(LABEL_1, VOCAB_ID_1, URL_1);
        val keywordSolrIndex2 = new Keyword(LABEL_2, VOCAB_ID_2, URL_2);

        List<String> vocabularyIds = new ArrayList<>();
        vocabularyIds.add(VOCAB_ID_1);
        vocabularyIds.add(VOCAB_ID_2);

        given(keywordService.query("*", vocabularyIds))
                .willReturn(Arrays.asList(
                        keywordSolrIndex1,
                        keywordSolrIndex2
                ));
    }

    @Test
    @SneakyThrows
    void getSites() {
        //Given
        givenQueryResponse();
        val expectedResponse = "[{\"label\":\"assist-topics\",\"vocabId\":\"ASSIST-TOPICS\",\"url\":\"www.example.com\"},{\"label\":\"label\",\"vocabId\":\"vocab\",\"url\":\"www.example.com/2\"}]";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("vocab", VOCAB_ID_1);
        params.add("vocab", VOCAB_ID_2);
        //When
        mvc.perform(
                get("/vocabulary/keywords")
                        .queryParam("query", QUERY).queryParams(params)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponse));
    }

    @Test
    @SneakyThrows
    void getSitesNoQuery() {
        //Given
        givenQueryResponseNoQuery();
        val expectedResponse = "[{\"label\":\"assist-topics\",\"vocabId\":\"ASSIST-TOPICS\",\"url\":\"www.example.com\"},{\"label\":\"label\",\"vocabId\":\"vocab\",\"url\":\"www.example.com/2\"}]";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("vocab", VOCAB_ID_1);
        params.add("vocab", VOCAB_ID_2);

        //When
        mvc.perform(
                get("/vocabulary/keywords")
                        .queryParams(params)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedResponse));
    }

    @Test
    @SneakyThrows
    void noVocabularies() {
        //given
        given(keywordService.query("test", Collections.emptyList()))
            .willReturn(Arrays.asList(
                new Keyword("Keyword 1", "test-0", "https://example.com/1"),
                new Keyword("Keyword 2", "test-0", "https://example.com/2"),
                new Keyword("Keyword 3", "test-1", "https://example.com/3")
            ));

        //when
        mvc.perform(
            get("/vocabulary/keywords")
                .queryParam("query", "test")
        ).andExpect(status().isOk());
    }


    @Test
    @SneakyThrows
    void ThrowSolrServerException() {
        //Given
        List<String> vocabularyIds = new ArrayList<>();
        vocabularyIds.add(VOCAB_ID_1);
        vocabularyIds.add(VOCAB_ID_2);

        given(keywordService.query(QUERY, vocabularyIds)).willThrow(new SolrServerException("Test"));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("vocab", VOCAB_ID_1);
        params.add("vocab", VOCAB_ID_2);

        //When
        mvc.perform(
                get("/vocabulary/keywords")
                        .queryParam("query", QUERY).queryParams(params)
        )
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"message\":\"Solr did not respond as expected\"}"));
    }
}
