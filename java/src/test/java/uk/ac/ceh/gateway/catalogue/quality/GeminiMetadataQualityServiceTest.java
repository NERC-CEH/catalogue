package uk.ac.ceh.gateway.catalogue.quality;

import com.google.common.collect.ImmutableMap;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.ResourceUtils;
import uk.ac.ceh.gateway.catalogue.config.DownloadUrlProperties;
import uk.ac.ceh.gateway.catalogue.document.reading.DocumentReader;
import uk.ac.ceh.gateway.catalogue.templateHelpers.UriNormaliser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.ac.ceh.gateway.catalogue.quality.Results.Severity.ERROR;
import static uk.ac.ceh.gateway.catalogue.quality.Results.Severity.INFO;
import static uk.ac.ceh.gateway.catalogue.quality.Results.Severity.WARNING;


@Slf4j
@ExtendWith(MockitoExtension.class)
public class GeminiMetadataQualityServiceTest {
    private GeminiMetadataQualityService service;
    private final Configuration config = Configuration.defaultConfiguration()
        .jsonProvider(new JacksonJsonProvider())
        .mappingProvider(new JacksonMappingProvider())
        .addOptions(
            Option.DEFAULT_PATH_LEAF_TO_NULL,
            Option.SUPPRESS_EXCEPTIONS
        );

    @Mock
    private DocumentReader documentReader;
    @Mock
    private DownloadUrlProperties downloadUrlProperties;

    @BeforeEach
    public void setup() {
        this.service = new GeminiMetadataQualityService(documentReader, downloadUrlProperties, new UriChecks(new UriNormaliser()));
    }

    @Test
    @SneakyThrows
    public void successfullyCheckExistingDocument() {
        //given
        given(documentReader.read("test1", "raw"))
            .willReturn(
                ResourceUtils.getFile(getClass().getResource("test1.raw"))
            );
        given(documentReader.read("test1", "meta"))
            .willReturn(
                ResourceUtils.getFile(getClass().getResource("test1.meta"))
            );

        //when
        this.service.check("test1");

        //then
        verify(documentReader).read("test1", "raw");
        verify(documentReader).read("test1", "meta");
    }

    @Test
    @SneakyThrows
    public void successfullyCheckEmptyDocument() {
        //given
        given(documentReader.read("test0", "raw"))
            .willReturn(
                ResourceUtils.getFile(getClass().getResource("test0.raw"))
            );
        given(documentReader.read("test0", "meta"))
            .willReturn(
                ResourceUtils.getFile(getClass().getResource("test0.meta"))
            );

        //when
        this.service.check("test0");

        //then
        verify(documentReader).read("test0", "raw");
        verify(documentReader).read("test0", "meta");
    }

    @Test
    public void checkAddressOrganisationName() {
        //given
        val addresses = new ArrayList<Map<String, String>>(Arrays.asList(
            ImmutableMap.of("organisationName", "Test organisation 0"),
            ImmutableMap.of("organisationName", "Test organisation 1"),
            ImmutableMap.of("organisationName", "Test organisation 2")
        ));

        //when
        val actual = this.service.checkAddress(addresses, "Test");

        //then
        assertThat(actual, empty());
    }

    @Test
    public void checkWrongAddressOrganisationName() {
        //given
        val addresses = new ArrayList<Map<String, String>>(Arrays.asList(
            ImmutableMap.of("organisationName", "Test organisation 0"),
            ImmutableMap.of("organisationName", "Test organisation 1"),
            ImmutableMap.of("familylName", "individual 0")
        ));

        //when
        val actual = this.service.checkAddress(addresses, "Test");

        //then
        assertThat(actual, not(empty()));
    }

    @Test
    public void checkNonGeographicWithRightElements() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("nonGeographicRight.json"), this.config);

        //when
        val actual = this.service.checkNonGeographicDatasets(parsed);

        //then
        assertThat(actual, empty());
    }

    @Test
    public void checkNonGeographicWithWrongElements() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("nonGeographicWrong.json"), this.config);

        //when
        val actual = this.service.checkNonGeographicDatasets(parsed);

        //then
        assertThat(actual, not(empty()));
    }

    @Test
    public void checkSignpostHasCorrectOnlineResource() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("nercSignpostRight.json"), this.config);

        //when
        val actual = this.service.checkNercSignpost(parsed);

        //then
        assertThat(actual, empty());
    }

    @Test
    public void checkSignpostHasIncorrectOnlineResource() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("nercSignpostWrong.json"), this.config);

        //when
        val actual = this.service.checkNercSignpost(parsed);

        //then
        assertThat(actual, not(empty()));
    }

    @Test
    public void checkSignpostHasMissingOnlineResource() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("nercSignpostMissing.json"), this.config);

        //when
        val actual = this.service.checkNercSignpost(parsed);

        //then
        assertThat(actual, not(empty()));
    }

    @Test
    public void checkSignpostNotCorrectResourceType() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("nercSignpostNotResourceType.json"), this.config);

        //when
        val actual = this.service.checkNercSignpost(parsed);

        //then
        assertThat(actual, empty());
    }

    @Test
    public void checkDatasetCorrect() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("datasetCorrect.json"), this.config);

        //when
        val actual = this.service.checkSpatialDataset(parsed);

        //then
        assertThat(actual, empty());
    }

    @Test
    public void checkDatasetWrong() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("datasetWrong.json"), this.config);

        //when
        val actual = this.service.checkSpatialDataset(parsed);

        //then
        assertThat(actual, not(empty()));
    }

    @Test
    public void checkAuthorCorrect() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("authorsRight.json"), this.config);

        //when
        val actual = this.service.checkAuthors(parsed);

        //then
        assertThat(actual, empty());
    }

    @Test
    public void checkAuthorWrong() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("authorsWrong.json"), this.config);

        //when
        val actual = this.service.checkAuthors(parsed);

        //then
        assertThat(actual, not(empty()));
    }

    @Test
    public void checkTopicCategoriesCorrect() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("topicCategoriesRight.json"), this.config);

        //when
        val actual = this.service.checkTopicCategories(parsed);

        //then
        assertThat(actual, empty());
    }

    @Test
    public void checkTopicCategoriesWrong() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("topicCategoriesWrong.json"), this.config);

        //when
        val actual = this.service.checkTopicCategories(parsed);

        //then
        assertThat(actual, not(empty()));
    }

    @Test
    public void checkCustodianCorrect() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("custodiansRight.json"), this.config);

        //when
        val actual = this.service.checkCustodian(parsed);

        //then
        assertThat(actual, empty());
    }

    @Test
    public void checkCustodianWrong() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("custodiansWrong.json"), this.config);

        //when
        val actual = this.service.checkCustodian(parsed);

        //then
        assertThat(actual, not(empty()));
    }

    @Test
    public void checkPublisherCorrect() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("publishersRight.json"), this.config);

        //when
        val actual = this.service.checkPublisher(parsed);

        //then
        assertThat(actual, empty());
    }

    @Test
    public void checkPublisherWrong() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("publishersWrong.json"), this.config);

        //when
        val actual = this.service.checkPublisher(parsed);

        //then
        assertThat(actual, not(empty()));
    }

    @Test
    public void checkAuthorCorrectFromLegacyResponsibleParties() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("legacyResponsiblePartiesRight.json"), this.config);

        //when
        val actual = this.service.checkAuthors(parsed);

        //then
        assertThat(actual, empty());
    }

    @Test
    public void checkPublisherCorrectFromLegacyResponsibleParties() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("legacyResponsiblePartiesRight.json"), this.config);

        //when
        val actual = this.service.checkPublisher(parsed);

        //then
        assertThat(actual, empty());
    }

    @Test
    public void checkCustodianCorrectFromLegacyResponsibleParties() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("legacyResponsiblePartiesRight.json"), this.config);

        //when
        val actual = this.service.checkCustodian(parsed);

        //then
        assertThat(actual, empty());
    }

    @Test
    public void checkPointOfContactCorrectFromLegacyResponsibleParties() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("legacyResponsiblePartiesRight.json"), this.config);

        //when
        val actual = this.service.checkPointOfContact(parsed);

        //then
        assertThat(actual, empty());
    }

    @Test
    public void checkPointOfContactAllowsMixedCaseUkcehEmail() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("pointOfContactMixedCaseAllowed.json"), this.config);

        //when
        val actual = this.service.checkPointOfContact(parsed);

        //then
        assertThat(actual, empty());
    }

    @Test
    public void checkPointOfContactFlagsMixedCaseDomainNotOnAllowList() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("pointOfContactMixedCaseNotAllowed.json"), this.config);

        //when
        val actual = this.service.checkPointOfContact(parsed);

        //then
        assertThat(actual, contains(
            new MetadataCheck("Point of contact's  email address is Sam.Jones@CEH.ac.uk", ERROR)
        ));
    }

    @Test
    public void checkDistributorCorrect() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("distributorsRight.json"), this.config);

        //when
        val actual = this.service.checkDistributor(parsed);

        //then
        assertThat(actual, empty());
    }

    @Test
    public void checkDistributorWrong() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("distributorsWrong.json"), this.config);

        //when
        val actual = this.service.checkDistributor(parsed);

        //then
        assertThat(actual, not(empty()));
    }

    @Test
    public void checkDownloadOrdersCorrect() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("downloadOrdersRight.json"), this.config);

        //when
        when(downloadUrlProperties.getRegexDatastore()).thenReturn("https://catalogue\\.ceh\\.ac\\.uk/datastore/eidchub/.*");
        val actual = this.service.checkDownloadAndOrderLinks(parsed);

        //then
        assertThat(actual, empty());
    }

    @Test
    public void checkDownloadOrdersNotAvailable() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("downloadOrdersNotAvailable.json"), this.config);

        //when
        val actual = this.service.checkDownloadAndOrderLinks(parsed);

        //then
        assertThat(actual, empty());
    }

    @Test
    public void checkDownloadOrdersWrong() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("downloadOrdersWrong.json"), this.config);

        //when
        when(downloadUrlProperties.getRegexDatastore()).thenReturn("https://catalogue\\.ceh\\.ac\\.uk/datastore/eidchub/.*");
        when(downloadUrlProperties.getRegexOrder()).thenReturn("https://order-eidc\\.ceh\\.ac\\.uk/resources/.{8}/order\\?*.*");
        when(downloadUrlProperties.getRegexPackage()).thenReturn("https://data-package\\.ceh\\.ac\\.uk/.*");
        when(downloadUrlProperties.getRegexCeda()).thenReturn("https://catalogue\\.ceh\\.ac\\.uk/datastore/eidchub/.*");
        val actual = this.service.checkDownloadAndOrderLinks(parsed);

        //then
        assertThat(actual, not(empty()));
    }

    @Test
    public void checkDataFormatsCorrect() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("dataFormatRight.json"), this.config);

        //when
        val actual = this.service.checkDataFormat(parsed);

        //then
        assertThat(actual, empty());
    }

    @Test
    public void checkDataFormatsWrong() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("dataFormatWrong.json"), this.config);

        //when
        val actual = this.service.checkDataFormat(parsed);

        //then
        assertThat(actual, not(empty()));
    }

    @Test
    public void checkBasicsCorrect() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("basicsCorrect.json"), this.config);

        //when
        val actual = this.service.checkBasics(parsed);

        //then
        assertThat(actual, empty());
    }

    @Test
    public void checkBasicsWrong() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("basicsWrong.json"), this.config);

        //when
        val actual = this.service.checkBasics(parsed);

        //then
        assertThat(actual, not(empty()));
    }

    @Test
    public void checkAvailabilityAvailable() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("availabilityAvailable.json"), this.config);

        //when
        val actual = this.service.availabilityIsAvailable(parsed);

        //then
        assertThat(actual, is(true));
    }

    @Test
    public void checkAvailabilityNotAvailable() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("availabilityNotAvailable.json"), this.config);

        //when
        val actual = this.service.availabilityIsAvailable(parsed);

        //then
        assertThat(actual, is(false));
    }

    @Test
    public void checkBoundingBoxesWrong() {
        //given
        val parsed = JsonPath.parse(getClass().getResourceAsStream("boundingBoxWrong.json"), this.config);

        //when
        val actual = this.service.checkBoundingBoxes(parsed);

        //then
        assertThat(actual, not(empty()));
    }


    @Test
    public void resultsErrors() {
        //given
        val checks = Arrays.asList(
            new MetadataCheck("check 1", ERROR),
            new MetadataCheck("check 2", ERROR),
            new MetadataCheck("check 3", WARNING),
            new MetadataCheck("check 4", ERROR),
            new MetadataCheck("check 5", ERROR)
        );

        //when
        val actual = new Results(checks, "test").getErrors();

        //then
        assertThat(actual, equalTo(4L));
    }

    @Test
    public void resultsWarnings() {
        //given
        val checks = Arrays.asList(
            new MetadataCheck("check 1", WARNING),
            new MetadataCheck("check 2", ERROR),
            new MetadataCheck("check 3", WARNING),
            new MetadataCheck("check 4", ERROR),
            new MetadataCheck("check 5", WARNING)
        );

        //when
        val actual = new Results(checks, "test").getWarnings();

        //then
        assertThat(actual, equalTo(3L));
    }

    @Test
    public void problemsSortedBySeverity() {
        //given
        val expected = Arrays.asList(ERROR, ERROR, ERROR, ERROR, WARNING, WARNING, WARNING, WARNING, WARNING, WARNING);
        val checks = Arrays.asList(
            new MetadataCheck("check 1", WARNING),
            new MetadataCheck("check 2", ERROR),
            new MetadataCheck("check 3", WARNING),
            new MetadataCheck("check 4", ERROR),
            new MetadataCheck("check 5", WARNING),
            new MetadataCheck("check 6", WARNING),
            new MetadataCheck("check 7", ERROR),
            new MetadataCheck("check 8", WARNING),
            new MetadataCheck("check 9", ERROR),
            new MetadataCheck("check 10", WARNING)
        );

        //when
        val actual = new Results(checks, "test")
            .getProblems()
            .stream()
            .map(MetadataCheck::getSeverity)
            .toList();

        //then
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void totalErrorsAndWarnings() {
        //given
        val expectedTotalErrors = 8L;
        val expectedTotalWarnings = 12L;
        val checks = Arrays.asList(
            new MetadataCheck("check 1", WARNING),
            new MetadataCheck("check 2", ERROR),
            new MetadataCheck("check 3", WARNING),
            new MetadataCheck("check 4", ERROR),
            new MetadataCheck("check 5", WARNING),
            new MetadataCheck("check 6", WARNING),
            new MetadataCheck("check 7", ERROR),
            new MetadataCheck("check 8", WARNING),
            new MetadataCheck("check 9", ERROR),
            new MetadataCheck("check 10", WARNING)
        );

        val results =Arrays.asList(
            new Results(checks, "test0"),
            new Results(checks, "test1")
        );

        //when
        val actual = new CatalogueResults(results);

        //then
        assertThat(actual.getTotalErrors(), equalTo(expectedTotalErrors));
        assertThat(actual.getTotalWarnings(), equalTo(expectedTotalWarnings));
    }

    @Nested
    @DisplayName("Externally-supplied URIs (dri-one #318)")
    class Uris {

        private List<MetadataCheck> check(String json) {
            return service.checkUris(JsonPath.parse(json, config));
        }

        @Test
        @DisplayName("a canonical record raises nothing")
        void canonicalUrisAreSilent() {
            val actual = check("""
                {
                  "keywordsTheme": [{"value": "Scotland", "uri": "https://sws.geonames.org/2638360"}],
                  "funding": [{"awardURI": "https://gtr.ukri.org/projects?ref=NE/S008926/1"}],
                  "authors": [{"nameIdentifier": "https://orcid.org/0000-0001-2345-6789"}],
                  "accessLimitation": {"uri": "http://purl.org/coar/access_right/c_abf2"}
                }
                """);

            assertThat(actual, is(empty()));
        }

        @Test
        @DisplayName("a stray trailing slash is reported with the canonical form")
        void reportsTrailingSlash() {
            val actual = check("""
                {"keywordsPlace": [{"value": "Scotland", "uri": "http://sws.geonames.org/2638360/"}]}
                """);

            assertThat(actual, contains(new MetadataCheck(
                "Keyword URI is not in its canonical form, http://sws.geonames.org/2638360/ "
                    + "should be https://sws.geonames.org/2638360",
                INFO
            )));
        }

        @Test
        @DisplayName("a percent-encoded grant reference is reported with the canonical form")
        void reportsPercentEncodedAwardUri() {
            val actual = check("""
                {"funding": [{"awardURI": "http://gtr.ukri.org/projects?ref=NE%2FS008926%2F1"}]}
                """);

            assertThat(actual, contains(new MetadataCheck(
                "Funding award URI is not in its canonical form, "
                    + "http://gtr.ukri.org/projects?ref=NE%2FS008926%2F1 "
                    + "should be https://gtr.ukri.org/projects?ref=NE/S008926/1",
                INFO
            )));
        }

        @Test
        @DisplayName("a URI that cannot be parsed at all is an error")
        void malformedUriIsAnError() {
            val actual = check("""
                {"keywordsOther": [
                  {"value": "Rainfall rate", "uri": "hhttp://vocab.nerc.ac.uk/collection/N07/current/RAUT/"}
                ]}
                """);

            assertThat(actual, contains(new MetadataCheck(
                "Keyword URI is not a usable URI: "
                    + "hhttp://vocab.nerc.ac.uk/collection/N07/current/RAUT/",
                ERROR
            )));
        }

        @Test
        @DisplayName("a URI whose trailing slash is significant is left alone")
        void significantTrailingSlashIsNotReported() {
            val actual = check("""
                {"keywordsOther": [
                  {"value": "Rainfall rate", "uri": "http://vocab.nerc.ac.uk/collection/N07/current/RAUT/"}
                ]}
                """);

            assertThat(actual, is(empty()));
        }

        @Test
        @DisplayName("responsible-party identifiers are checked and named by their collection")
        void reportsContactIdentifiers() {
            val actual = check("""
                {"contactPoints": [{"nameIdentifier": "http://orcid.org/0000-0001-2345-6789"}]}
                """);

            assertThat(actual, contains(new MetadataCheck(
                "ORCID on contactPoints is not in its canonical form, "
                    + "http://orcid.org/0000-0001-2345-6789 should be https://orcid.org/0000-0001-2345-6789",
                INFO
            )));
        }

        @Test
        @DisplayName("the same offending URI in two places is reported once")
        void deduplicatesRepeatedOffenders() {
            val actual = check("""
                {
                  "keywordsPlace": [{"uri": "http://sws.geonames.org/2638360/"}],
                  "keywordsTheme": [{"uri": "http://sws.geonames.org/2638360/"}]
                }
                """);

            assertThat(actual, hasSize(1));
        }

        @Test
        @DisplayName("observed properties nested inside filesets are reached")
        void reportsNestedObservedProperties() {
            val actual = check("""
                {"fileset": [
                  {"observedProperty": [{"uri": "http://vocabs.lter-europe.net/EnvThes/30347/"}]}
                ]}
                """);

            assertThat(actual, contains(new MetadataCheck(
                "Observed property URI is not in its canonical form, "
                    + "http://vocabs.lter-europe.net/EnvThes/30347/ "
                    + "should be http://vocabs.lter-europe.net/EnvThes/30347",
                INFO
            )));
        }

        @Test
        @DisplayName("a record with none of these fields is handled without error")
        void emptyRecord() {
            assertThat(check("{}"), is(empty()));
        }

        @Test
        @DisplayName("blank URI values are not reported as malformed")
        void blankUrisAreIgnored() {
            val actual = check("""
                {"keywordsOther": [{"value": "No concept", "uri": ""}, {"value": "Nor this"}]}
                """);

            assertThat(actual, is(empty()));
        }
    }
}
