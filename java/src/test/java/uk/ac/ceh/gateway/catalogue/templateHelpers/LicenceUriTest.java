package uk.ac.ceh.gateway.catalogue.templateHelpers;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@DisplayName("Identifying the RDF node for a licence or rights statement")
class LicenceUriTest {

    private static final String OGL = "https://spdx.org/licenses/OGL-UK-3.0.ttl";

    private LicenceUri service;

    @BeforeEach
    void setUp() {
        service = new LicenceUri();
    }

    @Nested
    @DisplayName("Canonicalising a URI that is already known (dri-one #327)")
    class Canonicalising {

        @ParameterizedTest
        @DisplayName("every spelling of the Open Government Licence converges on the SPDX URI")
        @ValueSource(strings = {
            "https://eidc.ac.uk/licences/ogl/plain",
            "https://eidc.ac.uk/licences/OGL/plain",
            "https://eidc.ceh.ac.uk/licences/ogl",
            "https://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/",
            "https://nationalarchives.gov.uk/doc/open-government-licence/"
        })
        void openGovernmentLicence(String supplied) {
            assertThat(service.canonicalise(supplied), is(OGL));
        }

        @ParameterizedTest
        @DisplayName("every spelling of a Creative Commons 4.0 licence converges on its SPDX URI")
        @CsvSource({
            "https://creativecommons.org/licenses/by/4.0/,          https://spdx.org/licenses/CC-BY-4.0.ttl",
            "https://creativecommons.org/licenses/by/4.0/deed.en,   https://spdx.org/licenses/CC-BY-4.0.ttl",
            "http://creativecommons.org/licenses/by/4.0,            https://spdx.org/licenses/CC-BY-4.0.ttl",
            "https://creativecommons.org/licenses/by-nc/4.0,        https://spdx.org/licenses/CC-BY-NC-4.0.ttl",
            "https://creativecommons.org/licenses/by-nd/4.0/,       https://spdx.org/licenses/CC-BY-ND-4.0.ttl"
        })
        void creativeCommons(String supplied, String expected) {
            assertThat(service.canonicalise(supplied.trim()), is(expected.trim()));
        }

        @ParameterizedTest
        @DisplayName("a bespoke catalogue licence converges on the canonical eidc host, path intact")
        @ValueSource(strings = {
            "/licences/ecn/plain", "/licences/chessmet/plain",
            "/licences/standard-click-through/plain", "/licences/ogl-ukbms/plain"
        })
        void eidcHostSplit(String path) {
            assertThat(
                "these four slugs are exactly the set seen in production under both hosts",
                service.canonicalise("https://eidc.ceh.ac.uk" + path),
                is(service.canonicalise("https://eidc.ac.uk" + path))
            );
            assertThat(service.canonicalise("https://eidc.ceh.ac.uk" + path), is("https://eidc.ac.uk" + path));
        }

        @Test
        @DisplayName("path case is used to match but not to rewrite, so the emitted path is untouched")
        void pathCaseIsPreservedInTheOutput() {
            assertThat(
                service.canonicalise("https://eidc.ceh.ac.uk/licences/ECN/Plain"),
                is("https://eidc.ac.uk/licences/ECN/Plain")
            );
        }

        @Test
        @DisplayName("a trailing slash is stripped, so it cannot fork the licence")
        void trailingSlash() {
            assertThat(
                service.canonicalise("https://eidc.ac.uk/licences/ecn/plain/"),
                is("https://eidc.ac.uk/licences/ecn/plain")
            );
        }

        @ParameterizedTest
        @DisplayName("an unrecognised URI passes straight through, as it did before #327")
        @ValueSource(strings = {
            "https://example.com/licences/something-bespoke",
            "https://eidc.ac.uk/licences/not-in-the-list/plain",
            "https://creativecommons.org/licenses/by/3.0/",
            "https://creativecommons.org/publicdomain/zero/1.0/",
            "not a uri at all",
            "mailto:enquiries@ceh.ac.uk"
        })
        void unrecognisedPassesThrough(String supplied) {
            assertThat(service.canonicalise(supplied), is(supplied));
        }

        @Test
        @DisplayName("an empty URI is returned unchanged rather than becoming a stem")
        void empty() {
            assertThat(service.canonicalise(""), is(""));
        }

        @Test
        @DisplayName("the open-government path is matched on any host, the archives path only on its own")
        void hostScoping() {
            assertThat(
                "the /licences/ogl path is a catalogue path, matched wherever it is hosted",
                service.canonicalise("https://example.com/licences/ogl"), is(OGL)
            );
            assertThat(
                "an open-government-licence path elsewhere is not the National Archives' licence",
                service.canonicalise("https://example.com/doc/open-government-licence"),
                is("https://example.com/doc/open-government-licence")
            );
        }
    }

    @Nested
    @DisplayName("Minting a node for free text with no URI")
    class Minting {

        @Test
        @DisplayName("the same wording is one node however it is spaced or cased")
        void textKeyFolds() {
            val expected = service.mintLicence("Open Government Licence");
            assertThat(service.mintLicence("open government licence"), is(expected));
            assertThat(service.mintLicence("  Open   Government\nLicence  "), is(expected));
        }

        @Test
        @DisplayName("a licence, an access-rights statement and a copyright notice never collide")
        void prefixesKeepNodeTypesApart() {
            val text = "Open Government Licence";
            assertThat(service.mintLicence(text), not(equalTo(service.mintAccessRights(text))));
            assertThat(service.mintLicence(text), not(equalTo(service.mintCopyright(text))));
            assertThat(service.mintAccessRights(text), not(equalTo(service.mintCopyright(text))));
        }

        @Test
        @DisplayName("each node type is recognisable by its prefix")
        void prefixes() {
            assertThat(service.mintLicence("x").startsWith(":licence_"), is(true));
            assertThat(service.mintAccessRights("x").startsWith(":accessRights_"), is(true));
            assertThat(service.mintCopyright("x").startsWith(":copyright_"), is(true));
        }

        /**
         * Pins the emitted node, not just its stability. These nodes are already
         * published in the graph, so a change to the key or the hash would silently
         * re-identify every licence in it — the one thing consolidating the hashing
         * into {@link MintedNode} had to avoid.
         */
        @Test
        @DisplayName("the minted node is byte-for-byte what it has always been")
        void mintedNodesAreFrozen() {
            assertThat(service.mintLicence("OGL"), is(":licence_741e4c777d1f94b0"));
            assertThat(service.mintAccessRights("Open"), is(":accessRights_2348f99874421257"));
            assertThat(service.mintCopyright("(c) UKCEH"), is(":copyright_d376e886f6146c97"));
        }
    }
}
