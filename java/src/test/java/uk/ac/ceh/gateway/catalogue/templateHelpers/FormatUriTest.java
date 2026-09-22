package uk.ac.ceh.gateway.catalogue.templateHelpers;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.ac.ceh.gateway.catalogue.gemini.DistributionInfo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesRegex;
import static org.hamcrest.Matchers.not;

@DisplayName("Identifying the RDF node for a distribution format")
class FormatUriTest {

    private FormatUri service;

    @BeforeEach
    void setUp() {
        service = new FormatUri();
    }

    private static DistributionInfo format(String name, String type) {
        return DistributionInfo.builder().name(name).type(type).version("unknown").build();
    }

    @Nested
    @DisplayName("Preferring the IANA media type registration")
    class MediaTypes {

        @Test
        @DisplayName("a media type identifies the format")
        void mediaTypeWins() {
            assertThat(
                service.identify(format("Comma-separated values (CSV)", "text/csv")),
                is("<https://www.iana.org/assignments/media-types/text/csv>")
            );
        }

        @Test
        @DisplayName("the media type takes precedence over the name, so two spellings converge")
        void twoNamesOneMediaType() {
            assertThat(
                service.identify(format("CSV", "text/csv")),
                is(service.identify(format("Comma-separated values (CSV)", "TEXT/CSV")))
            );
        }

        @ParameterizedTest
        @DisplayName("a vendor or structured media type is still a media type")
        @ValueSource(strings = {
            "application/vnd.apache.parquet",
            "application/netcdf",
            "image/tiff",
            "application/geo+json",
            "application/x-hdf5"
        })
        void structuredMediaTypes(String type) {
            assertThat(
                service.mediaTypeUri(format("Whatever", type)),
                is("https://www.iana.org/assignments/media-types/" + type)
            );
        }

        @ParameterizedTest
        @DisplayName("free text that is not a media type is not turned into a URI")
        @ValueSource(strings = {
            "not a media type",
            "text / csv",
            "csv",
            "text/csv/extra",
            "text/",
            "/csv",
            "text/csv?charset=utf-8",
            "<script>",
            " "
        })
        void rejectsAnythingThatIsNotAMediaType(String type) {
            assertThat(
                "a media type reaches us as depositor-typed free text",
                service.mediaTypeUri(format("Shapefile", type)), is("")
            );
        }

        @Test
        @DisplayName("surrounding whitespace and case do not stop a media type being recognised")
        void mediaTypeIsTrimmedAndFolded() {
            assertThat(
                service.mediaTypeUri(format("CSV", "  Text/CSV  ")),
                is("https://www.iana.org/assignments/media-types/text/csv")
            );
        }
    }

    @Nested
    @DisplayName("Minting a node from the format's name")
    class MintedNodes {

        @Test
        @DisplayName("a format with no media type is identified by its name")
        void nameMints() {
            assertThat(service.identify(format("Shapefile", "")), matchesRegex(":format_[0-9a-f]{16}"));
        }

        @ParameterizedTest
        @DisplayName("case and internal whitespace do not fork the format")
        @CsvSource({
            "GeoJSON, geojson",
            "png, PNG",
            "Rds, rds",
            "'Tab delimited text', 'tab  delimited   text'",
            "'  Shapefile  ', Shapefile"
        })
        void nameKeyFolds(String one, String other) {
            assertThat(service.identify(format(one, "")), is(service.identify(format(other, ""))));
        }

        @ParameterizedTest
        @DisplayName("spelling variants are deliberately left as separate nodes")
        @CsvSource({
            "png, 'Portable Network Graphics (png)'",
            "rds, .rds",
            "gpkg, GeoPackage",
            "jpg, jpeg",
            "ASCII, 'ASCII grid'"
        })
        void spellingVariantsStillFork(String one, String other) {
            assertThat(
                "reconciling these needs a decision per pair, and is data cleanup rather than minting",
                service.identify(format(one, "")), not(equalTo(service.identify(format(other, ""))))
            );
        }

        @Test
        @DisplayName("the same name always mints the same node")
        void mintingIsStable() {
            assertThat(service.identify(format("Shapefile", "")), is(service.identify(format("Shapefile", ""))));
        }

        @Test
        @DisplayName("a licence and a format sharing wording do not collide")
        void prefixesKeepNodeTypesApart() {
            val licence = new LicenceUri().mintLicence("Shapefile");
            assertThat(service.identify(format("Shapefile", "")), not(equalTo(licence)));
        }
    }

    @Nested
    @DisplayName("Suppressing a format there is nothing to say about")
    class Empty {

        @Test
        @DisplayName("neither a name nor a media type means no node at all")
        void nothingToIdentify() {
            assertThat(service.identify(format("", "")), is(""));
            assertThat(service.hasContent(format("", "")), is(false));
        }

        @Test
        @DisplayName("a name of nothing but whitespace is no name")
        void blankName() {
            assertThat(service.hasContent(format("   ", "")), is(false));
        }

        @Test
        @DisplayName("a media type alone is enough, even with no name")
        void mediaTypeAloneIsEnough() {
            assertThat(service.hasContent(format("", "text/csv")), is(true));
        }

        @Test
        @DisplayName("a name alone is enough, even with no media type")
        void nameAloneIsEnough() {
            assertThat(service.hasContent(format("Shapefile", "")), is(true));
        }
    }
}
