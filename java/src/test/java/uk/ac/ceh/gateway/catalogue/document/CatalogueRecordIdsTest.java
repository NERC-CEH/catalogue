package uk.ac.ceh.gateway.catalogue.document;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class CatalogueRecordIdsTest {
    private static final String BASE = "https://example.com";

    private static Optional<String> id(String identifier) {
        return CatalogueRecordIds.recordId(identifier, BASE);
    }

    @Test
    void productionHostOverHttpOrHttpsMatches() {
        assertThat(id("http://catalogue.ceh.ac.uk/id/abc"), equalTo(Optional.of("abc")));
        assertThat(id("https://catalogue.ceh.ac.uk/id/abc"), equalTo(Optional.of("abc")));
    }

    @Test
    void baseUriHostMatches() {
        assertThat(id("http://example.com/id/abc"), equalTo(Optional.of("abc")));
        assertThat(id("https://example.com/id/abc"), equalTo(Optional.of("abc")));
    }

    @Test
    void bareIdMatches() {
        assertThat(id("abc"), equalTo(Optional.of("abc")));
    }

    @Test
    void externalUriDoesNotMatch() {
        assertThat(id("https://data.example.org/id/abc"), equalTo(Optional.empty()));
        assertThat(id("https://data.example.org/thing/1"), equalTo(Optional.empty()));
    }

    @Test
    void extraPathQueryOrFragmentDoesNotMatch() {
        assertThat(id("https://example.com/id/abc/extra"), equalTo(Optional.empty()));
        assertThat(id("https://example.com/id/abc?x=1"), equalTo(Optional.empty()));
        assertThat(id("https://example.com/id/abc#frag"), equalTo(Optional.empty()));
    }

    @Test
    void nullAndBlankDoNotMatch() {
        assertThat(id(null), equalTo(Optional.empty()));
        assertThat(id("  "), equalTo(Optional.empty()));
    }
}
