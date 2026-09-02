package uk.ac.ceh.gateway.catalogue.templateHelpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@DisplayName("Normalising externally-supplied URIs for RDF")
class UriNormaliserTest {

    private UriNormaliser service;

    @BeforeEach
    void setUp() {
        service = new UriNormaliser();
    }

    @Nested
    @DisplayName("Rejecting URIs that must not become RDF nodes")
    class Rejection {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        void blankInput(String given) {
            assertThat(service.normalise(given), is(emptyString()));
        }

        @Test
        @DisplayName("a doubled scheme letter is rejected rather than becoming a dead-end node")
        void malformedScheme() {
            assertThat(
                service.normalise("hhttp://vocab.nerc.ac.uk/collection/N07/current/RAUT/"),
                is(emptyString())
            );
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "ftp://example.com/thing",
            "mailto:someone@example.com",
            "urn:isbn:0451450523",
            "example.com/thing",
            "/id/12345",
            "https://",
            "https:///path-with-no-host"
        })
        void notAnHttpUri(String given) {
            assertThat(service.normalise(given), is(emptyString()));
        }

        @ParameterizedTest
        @DisplayName("characters Turtle forbids inside <> are rejected, not emitted as broken Turtle")
        @ValueSource(strings = {
            "https://example.com/a space",
            "https://example.com/back\\slash",
            "https://example.com/angle<bracket>",
            "https://example.com/curly{brace}",
            "https://example.com/pipe|char",
            "https://example.com/quote\"mark",
            "https://example.com/new\nline"
        })
        void illegalInTurtleIri(String given) {
            assertThat(service.normalise(given), is(emptyString()));
        }
    }

    @Nested
    @DisplayName("Rules applied to every host")
    class UniversalRules {

        @Test
        void trimsSurroundingWhitespace() {
            assertThat(
                service.normalise("  http://onto.nerc.ac.uk/CAST/187  "),
                is(equalTo("http://onto.nerc.ac.uk/CAST/187"))
            );
        }

        @ParameterizedTest
        @CsvSource({
            "HTTP://onto.nerc.ac.uk/CAST/187, http://onto.nerc.ac.uk/CAST/187",
            "http://ONTO.NERC.AC.UK/CAST/187, http://onto.nerc.ac.uk/CAST/187",
            "Http://Onto.Nerc.Ac.Uk:8080/CAST/187, http://onto.nerc.ac.uk:8080/CAST/187"
        })
        void lowerCasesSchemeAndHostOnly(String given, String expected) {
            assertThat(service.normalise(given), is(equalTo(expected)));
        }

        @Test
        @DisplayName("path case is significant and is left alone")
        void preservesPathCase() {
            assertThat(
                service.normalise("http://onto.nerc.ac.uk/CAST/AbC"),
                is(equalTo("http://onto.nerc.ac.uk/CAST/AbC"))
            );
        }

        @Test
        @DisplayName("escapes it cannot decode are still canonicalised to upper-case hex")
        void upperCasesPercentEncodingItCannotDecode() {
            assertThat(
                service.normalise("http://example.com/a%3db%26c"),
                is(equalTo("http://example.com/a%3Db%26c"))
            );
        }
    }

    @Nested
    @DisplayName("Percent-decoding only where the decoded form is unambiguous")
    class PercentDecoding {

        @Test
        @DisplayName("an encoded slash in a query converges on the literal form")
        void decodesEncodedSlashesInQuery() {
            assertThat(
                service.normalise("https://gtr.ukri.org/projects?ref=NE%2FS008926%2F1"),
                is(equalTo("https://gtr.ukri.org/projects?ref=NE/S008926/1"))
            );
        }

        @Test
        @DisplayName("the two gtr forms of one grant produce the same node")
        void bothGtrFormsConverge() {
            assertThat(
                service.normalise("http://gtr.ukri.org/projects?ref=NE%2fS008926%2f1%2f"),
                is(equalTo(service.normalise("https://gtr.ukri.org/projects?ref=NE/S008926/1")))
            );
        }

        @Test
        @DisplayName("an encoded slash in a path is structural and stays encoded")
        void keepsEncodedSlashesInPath() {
            assertThat(
                service.normalise("https://example.com/collection/N07%2Fcurrent"),
                is(equalTo("https://example.com/collection/N07%2Fcurrent"))
            );
        }

        @ParameterizedTest
        @DisplayName("query delimiters and anything non-ASCII stay encoded")
        @CsvSource({
            "https://example.com/x?a=1%26b=2, https://example.com/x?a=1%26b=2",
            "https://example.com/x?a%3D1, https://example.com/x?a%3D1",
            "https://example.com/x?a=1%2B2, https://example.com/x?a=1%2B2",
            "https://example.com/x?a=50%25, https://example.com/x?a=50%25",
            "https://example.com/caf%C3%A9, https://example.com/caf%C3%A9",
            "https://example.com/a%20b?q=a%20b, https://example.com/a%20b?q=a%20b"
        })
        void keepsAmbiguousEscapes(String given, String expected) {
            assertThat(service.normalise(given), is(equalTo(expected)));
        }

        @Test
        @DisplayName("dot segments are not created by decoding")
        void doesNotDecodeDots() {
            assertThat(
                service.normalise("https://example.com/a/%2E%2E/b"),
                is(equalTo("https://example.com/a/%2E%2E/b"))
            );
        }

        @Test
        void decodesUnreservedCharacters() {
            assertThat(
                service.normalise("https://example.com/a%2Db%5Fc%7Ed%41"),
                is(equalTo("https://example.com/a-b_c~dA"))
            );
        }
    }

    @Nested
    @DisplayName("Per-host scheme preference")
    class SchemePreference {

        @ParameterizedTest
        @CsvSource({
            "http://gtr.ukri.org/projects?ref=NE/J015644/1, https://gtr.ukri.org/projects?ref=NE/J015644/1",
            "http://doi.org/10.5285/abcd, https://doi.org/10.5285/abcd",
            "http://ror.org/00pggkr55, https://ror.org/00pggkr55",
            "http://orcid.org/0000-0001-2345-6789, https://orcid.org/0000-0001-2345-6789",
            "http://digital.ceh.ac.uk/vocab/ra/1, https://digital.ceh.ac.uk/vocab/ra/1"
        })
        void upgradesKnownHostsToHttps(String given, String expected) {
            assertThat(service.normalise(given), is(equalTo(expected)));
        }

        /**
         * Checked against each authority rather than assumed: the eLTER store
         * holds EnvThes only under http, GEMET's getConcept returns an http uri
         * and rejects the https form with 400, NVS returns an http subject even
         * over an https request, the CAST graph on vocabs.ceh.ac.uk is named
         * with http, and AGROVOC mints http (dri-one #350).
         */
        @ParameterizedTest
        @DisplayName("a vocabulary that mints http keeps it — upgrading invents a second concept URI")
        @ValueSource(strings = {
            "http://vocabs.lter-europe.net/EnvThes/30347",
            "http://www.eionet.europa.eu/gemet/concept/530",
            "http://vocab.nerc.ac.uk/collection/N07/current/UNRS/",
            "http://onto.nerc.ac.uk/CAST/187",
            "http://aims.fao.org/aos/agrovoc/c_8543"
        })
        void vocabularyConceptsKeepHttp(String conceptUri) {
            assertThat(service.normalise(conceptUri), is(equalTo(conceptUri)));
        }

        @ParameterizedTest
        @DisplayName("and a record supplying https for one is brought back to it, so both forms converge")
        @CsvSource({
            "https://vocabs.lter-europe.net/EnvThes/30347, http://vocabs.lter-europe.net/EnvThes/30347",
            "https://www.eionet.europa.eu/gemet/concept/530, http://www.eionet.europa.eu/gemet/concept/530",
            "https://vocab.nerc.ac.uk/collection/N07/current/UNRS/, http://vocab.nerc.ac.uk/collection/N07/current/UNRS/",
            "https://onto.nerc.ac.uk/CAST/187, http://onto.nerc.ac.uk/CAST/187",
            "https://aims.fao.org/aos/agrovoc/c_8543, http://aims.fao.org/aos/agrovoc/c_8543"
        })
        void vocabularyConceptsAreBroughtBackToHttp(String given, String expected) {
            assertThat(service.normalise(given), is(equalTo(expected)));
        }

        @Test
        @DisplayName("a stray trailing slash on an EnvThes concept is stripped, converging the two forms")
        void envThesTrailingSlashIsStripped() {
            assertThat(
                service.normalise("https://vocabs.lter-europe.net/EnvThes/30347/"),
                is(equalTo("http://vocabs.lter-europe.net/EnvThes/30347"))
            );
        }

        @Test
        @DisplayName("an NVS concept keeps its trailing slash, which is part of the identifier")
        void nvsTrailingSlashSurvives() {
            assertThat(
                service.normalise("https://vocab.nerc.ac.uk/collection/N07/current/UNRS/"),
                is(equalTo("http://vocab.nerc.ac.uk/collection/N07/current/UNRS/"))
            );
        }

        @Test
        @DisplayName("hosts we have not confirmed serve https keep their scheme")
        void leavesUnknownHostsAlone() {
            assertThat(
                service.normalise("http://onto.nerc.ac.uk/CAST/187"),
                is(equalTo("http://onto.nerc.ac.uk/CAST/187"))
            );
            assertThat(
                service.normalise("http://purl.org/coar/access_right/c_abf2"),
                is(equalTo("http://purl.org/coar/access_right/c_abf2"))
            );
        }
    }

    @Nested
    @DisplayName("Per-host trailing-slash policy")
    class TrailingSlash {

        @Test
        @DisplayName("the bare and slashed forms of a geonames place produce the same node")
        void geonamesFormsConverge() {
            assertThat(
                service.normalise("http://sws.geonames.org/2638360/"),
                is(equalTo("https://sws.geonames.org/2638360"))
            );
            assertThat(
                service.normalise("http://sws.geonames.org/2638360"),
                is(equalTo(service.normalise("http://sws.geonames.org/2638360/")))
            );
        }

        @Test
        @DisplayName("a NERC vocabulary concept URI keeps the slash that makes it resolve")
        void keepsSignificantTrailingSlash() {
            assertThat(
                service.normalise("http://vocab.nerc.ac.uk/collection/N07/current/RAUT/"),
                is(equalTo("http://vocab.nerc.ac.uk/collection/N07/current/RAUT/"))
            );
        }

        @Test
        @DisplayName("a bare authority keeps its slash, there is no path to strip")
        void keepsBareAuthoritySlash() {
            assertThat(service.normalise("https://doi.org/"), is(equalTo("https://doi.org/")));
        }

        @Test
        @DisplayName("only one slash is stripped")
        void stripsASingleSlash() {
            assertThat(
                service.normalise("https://sws.geonames.org/2638360//"),
                is(equalTo("https://sws.geonames.org/2638360/"))
            );
        }

        @Test
        @DisplayName("a fragment means the trailing slash belongs to the path, leave it")
        void leavesSlashBeforeFragmentAlone() {
            assertThat(
                service.normalise("http://www.wikidata.org/entity/Q145/#id"),
                is(equalTo("http://www.wikidata.org/entity/Q145/#id"))
            );
        }
    }

    @Nested
    @DisplayName("Normalisation is idempotent")
    class Idempotence {

        @ParameterizedTest
        @ValueSource(strings = {
            "HTTP://GTR.UKRI.ORG/projects?ref=NE%2fJ015644%2f1%2f",
            "http://sws.geonames.org/2638360/",
            "http://vocab.nerc.ac.uk/collection/N07/current/RAUT/",
            "http://onto.nerc.ac.uk/CAST/187",
            "https://doi.org/10.5285/abcd"
        })
        void normalisingTwiceChangesNothing(String given) {
            var once = service.normalise(given);
            assertThat(service.normalise(once), is(equalTo(once)));
        }
    }
}
