package uk.ac.ceh.gateway.catalogue.document;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class CatalogueRecordIdsTest {
    private static final String BASE = "https://example.com";
    private static final String CANONICAL = "https://catalogue.ceh.ac.uk";
    private final CatalogueRecordIds recordIds = new CatalogueRecordIds(BASE, CANONICAL);

    private Optional<String> id(String identifier) {
        return recordIds.recordId(identifier);
    }

    @Test
    void canonicalHostOverHttpOrHttpsMatches() {
        assertThat(id("http://catalogue.ceh.ac.uk/id/abc"), equalTo(Optional.of("abc")));
        assertThat(id("https://catalogue.ceh.ac.uk/id/abc"), equalTo(Optional.of("abc")));
    }

    @Test
    void baseUriHostMatches() {
        assertThat(id("http://example.com/id/abc"), equalTo(Optional.of("abc")));
        assertThat(id("https://example.com/id/abc"), equalTo(Optional.of("abc")));
    }

    @Test
    void canonicalHostComesFromConfigurationNotACodedDefault() {
        CatalogueRecordIds elsewhere = new CatalogueRecordIds(BASE, "https://catalogue.example.org");

        assertThat(elsewhere.recordId("https://catalogue.example.org/id/abc"), equalTo(Optional.of("abc")));
        assertThat(elsewhere.recordId("https://catalogue.ceh.ac.uk/id/abc"), equalTo(Optional.empty()));
        assertThat(elsewhere.isCatalogueHost("catalogue.ceh.ac.uk"), is(false));
    }

    @Test
    void unparseableOrMissingHostsMatchNoUri() {
        CatalogueRecordIds none = new CatalogueRecordIds(null, "not a uri");

        assertThat(none.recordId("https:///id/abc"), equalTo(Optional.empty()));
        assertThat(none.recordId("abc"), equalTo(Optional.of("abc")));
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

    @Test
    void isCatalogueHostIgnoresCase() {
        assertThat(recordIds.isCatalogueHost("CATALOGUE.ceh.ac.uk"), is(true));
        assertThat(recordIds.isCatalogueHost("example.com"), is(true));
        assertThat(recordIds.isCatalogueHost("other.org"), is(false));
        assertThat(recordIds.isCatalogueHost(null), is(false));
    }
}
