package uk.ac.ceh.gateway.catalogue.search;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.gateway.catalogue.vocabularies.Keyword;
import uk.ac.ceh.gateway.catalogue.vocabularies.KeywordVocabulary;

import java.util.List;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class SparqlBroaderNarrowerRetrieverTest {

    private MockRestServiceServer mockServer;

    private BroaderNarrowerRetriever retriever;

    private final String sparqlEndpoint = "https://example.com/sparql";
    private static List<KeywordVocabulary> keywordVocabularies;

    @BeforeAll
    static void init() {
        val vocab1 = mock(KeywordVocabulary.class);
        val vocab2 = mock(KeywordVocabulary.class);
        val vocab3 = mock(KeywordVocabulary.class);
        keywordVocabularies = List.of(
            vocab1,
            vocab2,
            vocab3
        );
        given(vocab1.getId()).willReturn("cast");
        given(vocab2.getId()).willReturn("ef");
        given(vocab3.getId()).willReturn("encore");

        given(vocab1.getGraph()).willReturn("urn:x-evn-master:CAST");
        given(vocab2.getGraph()).willReturn("urn:x-evn-master:EF");
        given(vocab3.getGraph()).willReturn("urn:x-evn-master:ENCORE");
    }

    @BeforeEach
    void setup() {
        val restTemplate = new RestTemplate();
        retriever = new SparqlBroaderNarrowerRetriever(
            restTemplate,
            sparqlEndpoint,
            keywordVocabularies
        );
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @SneakyThrows
    @Test
    void retrieve() {
        //given
        val response = IOUtils.toString(
            Objects.requireNonNull(
                getClass().getResource("sparql.json")
            ),
            UTF_8
        );
        mockServer.expect(requestTo(sparqlEndpoint + "?format=json-simple"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        val keyword = new Keyword(
            "acid recoverable cerium",
            "cast",
            "http://onto.nerc.ac.uk/CAST/40"
        );

        val determinands = new Keyword("determinands", "cast", "http://onto.nerc.ac.uk/CAST/1");
        val dissolvedCerium = new Keyword("dissolved cerium", "cast", "http://onto.nerc.ac.uk/CAST/41");;

        //when
        val links = retriever.retrieve(keyword);

        //then
        assertThat(links, containsInAnyOrder(determinands, dissolvedCerium));
    }
}
