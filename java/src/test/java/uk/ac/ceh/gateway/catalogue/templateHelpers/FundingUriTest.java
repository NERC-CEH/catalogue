package uk.ac.ceh.gateway.catalogue.templateHelpers;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.ac.ceh.gateway.catalogue.gemini.Funding;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesRegex;
import static org.hamcrest.Matchers.not;

@DisplayName("Identifying the RDF node for a grant")
class FundingUriTest {

    private FundingUri service;

    @BeforeEach
    void setUp() {
        service = new FundingUri(new UriNormaliser());
    }

    private static Funding.FundingBuilder funding() {
        return Funding.builder();
    }

    @Nested
    @DisplayName("Preferring the funder's own award number")
    class AwardNumber {

        @Test
        @DisplayName("an award number identifies the grant, wherever it appears")
        void awardNumberMints() {
            val node = service.identify(funding().awardNumber("NE/R016429/1").build(), "recordA", 0);
            assertThat(node, matchesRegex(":grant_[0-9a-f]{16}"));
            assertThat(
                "the same award on another record is the same grant",
                service.identify(funding().awardNumber("NE/R016429/1").build(), "recordB", 7), is(node)
            );
        }

        @ParameterizedTest
        @DisplayName("incidental formatting does not fork the grant")
        @CsvSource({
            "NE/R016429/1, ne/r016429/1",
            "'  NE/R016429/1  ', NE/R016429/1"
        })
        void awardNumberKeyFolds(String one, String other) {
            assertThat(
                service.identify(funding().awardNumber(one).build(), "r", 0),
                is(service.identify(funding().awardNumber(other).build(), "r", 0))
            );
        }

        @Test
        @DisplayName("the separators inside an award number are significant")
        void separatorsAreKept() {
            assertThat(
                "an award number is a funder-provided code, not a name to be reduced",
                service.identify(funding().awardNumber("NE/R016429/1").build(), "r", 0),
                not(equalTo(service.identify(funding().awardNumber("NER0164291").build(), "r", 0)))
            );
        }

        /**
         * Pinned rather than merely stable: this node is already published in the
         * graph, so consolidating the hashing into {@link MintedNode} had to leave
         * it byte-for-byte identical.
         */
        @Test
        @DisplayName("the minted node is byte-for-byte what it has always been")
        void mintedNodeIsFrozen() {
            assertThat(
                service.identify(funding().awardNumber("NE/R016429/1").build(), "r", 0),
                is(":grant_979eb827a93d305e")
            );
        }
    }

    @Nested
    @DisplayName("Falling back when there is no award number")
    class Fallbacks {

        @Test
        @DisplayName("a resolvable award URI identifies the grant")
        void awardUri() {
            assertThat(
                service.identify(funding().awardURI("http://gtr.ukri.org/projects/AB12").build(), "r", 0),
                is("<https://gtr.ukri.org/projects/AB12>")
            );
        }

        @Test
        @DisplayName("with neither, the grant is scoped to the record and its position")
        void recordScoped() {
            assertThat(
                service.identify(funding().awardTitle("Some project").build(), "record9", 3),
                is(":record9_fund3")
            );
        }
    }

    @Nested
    @DisplayName("Suppressing a grant the record says nothing about (dri-one #322)")
    class HasContent {

        @Test
        @DisplayName("an entirely empty funding entry has nothing to assert")
        void empty() {
            assertThat(service.hasContent(funding().build()), is(false));
        }

        @ParameterizedTest
        @DisplayName("any one of the four fields is enough to keep the grant")
        @CsvSource({
            "awardNumber", "awardURI", "awardTitle", "funderIdentifier"
        })
        void anyFieldIsEnough(String field) {
            val builder = funding();
            switch (field) {
                case "awardNumber" -> builder.awardNumber("NE/1");
                case "awardURI" -> builder.awardURI("https://gtr.ukri.org/projects/AB12");
                case "awardTitle" -> builder.awardTitle("Some project");
                case "funderIdentifier" -> builder.funderIdentifier("https://ror.org/00cwqg982");
                default -> throw new IllegalArgumentException(field);
            }
            assertThat(service.hasContent(builder.build()), is(true));
        }

        @Test
        @DisplayName("an award URI that cannot be emitted does not count as content on its own")
        void unusableAwardUri() {
            assertThat(
                service.hasContent(funding().awardURI("not a uri").build()),
                is(false)
            );
        }
    }
}
