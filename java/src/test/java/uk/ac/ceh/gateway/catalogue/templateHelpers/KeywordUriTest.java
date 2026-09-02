package uk.ac.ceh.gateway.catalogue.templateHelpers;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.vocabularies.KeywordVocabularySolrQueryService;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("Deciding which RDF node a keyword is")
class KeywordUriTest {

    private static final String GEMET_SOIL_MOISTURE = "http://www.eionet.europa.eu/gemet/concept/7842";

    @Mock private KeywordVocabularySolrQueryService vocabulary;

    private KeywordUri keywordUri;

    @BeforeEach
    void setup() {
        keywordUri = new KeywordUri(new UriNormaliser(), vocabulary);
    }

    private static uk.ac.ceh.gateway.catalogue.vocabularies.Keyword concept(String label, String url) {
        return new uk.ac.ceh.gateway.catalogue.vocabularies.Keyword(label, "GEMET", url);
    }

    @Test
    @DisplayName("the record's own concept URI is used without consulting the vocabularies")
    void recordsOwnUriWins() {
        val identified = keywordUri.identify(
            Keyword.builder().value("Soil moisture").URI(GEMET_SOIL_MOISTURE).build()
        );

        assertThat(identified, equalTo(GEMET_SOIL_MOISTURE));
        verifyNoInteractions(vocabulary);
    }

    @Test
    @SneakyThrows
    @DisplayName("a literal that resolves to exactly one known concept becomes that concept")
    void unambiguousLiteralIsPromoted() {
        given(vocabulary.resolveExactLabel("Soil moisture"))
            .willReturn(Optional.of(concept("Soil moisture", GEMET_SOIL_MOISTURE)));

        val identified = keywordUri.identify(Keyword.builder().value("Soil moisture").build());

        assertThat(identified, equalTo(GEMET_SOIL_MOISTURE));
    }

    @Test
    @SneakyThrows
    @DisplayName("the resolved concept URI is canonicalised like any other externally-supplied URI")
    void resolvedUriIsCanonicalised() {
        given(vocabulary.resolveExactLabel("Scotland"))
            .willReturn(Optional.of(concept("Scotland", "http://sws.geonames.org/2638360/")));

        val identified = keywordUri.identify(Keyword.builder().value("Scotland").build());

        assertThat(identified, equalTo("https://sws.geonames.org/2638360"));
    }

    @Test
    @SneakyThrows
    @DisplayName("surrounding whitespace does not stop a literal resolving")
    void literalIsTrimmedBeforeLookup() {
        given(vocabulary.resolveExactLabel("Soil moisture"))
            .willReturn(Optional.of(concept("Soil moisture", GEMET_SOIL_MOISTURE)));

        val identified = keywordUri.identify(Keyword.builder().value("  Soil moisture  ").build());

        assertThat(identified, equalTo(GEMET_SOIL_MOISTURE));
    }

    @Test
    @SneakyThrows
    @DisplayName("a literal that resolves to nothing keeps no URI, so the template falls back to it")
    void unresolvedLiteralKeepsNoUri() {
        given(vocabulary.resolveExactLabel("Freeform keyword")).willReturn(Optional.empty());

        val identified = keywordUri.identify(Keyword.builder().value("Freeform keyword").build());

        assertThat(identified, is(""));
    }

    @Test
    @SneakyThrows
    @DisplayName("a resolved concept whose URI cannot be emitted as Turtle keeps no URI")
    void unemittableResolvedUriKeepsNoUri() {
        given(vocabulary.resolveExactLabel("Rainfall rate"))
            .willReturn(Optional.of(concept("Rainfall rate", "hhttp://vocab.nerc.ac.uk/collection/N07/current/RAUT/")));

        val identified = keywordUri.identify(Keyword.builder().value("Rainfall rate").build());

        assertThat(identified, is(""));
    }

    @Test
    @SneakyThrows
    @DisplayName("a vocabulary lookup failure costs the keyword its promotion, never the whole record")
    void vocabularyFailureFallsBack() {
        given(vocabulary.resolveExactLabel("Soil moisture")).willThrow(new SolrServerException("Solr is down"));

        val identified = keywordUri.identify(Keyword.builder().value("Soil moisture").build());

        assertThat(identified, is(""));
    }

    @Test
    @DisplayName("a keyword with neither URI nor text is not looked up")
    void emptyKeywordIsNotLookedUp() {
        val identified = keywordUri.identify(Keyword.builder().build());

        assertThat(identified, is(""));
        verifyNoInteractions(vocabulary);
    }
}
