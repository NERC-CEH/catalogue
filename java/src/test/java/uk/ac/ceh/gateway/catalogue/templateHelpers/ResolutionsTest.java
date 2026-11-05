package uk.ac.ceh.gateway.catalogue.templateHelpers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import uk.ac.ceh.gateway.catalogue.gemini.SpatialReferenceSystem;
import uk.ac.ceh.gateway.catalogue.gemini.SpatialResolution;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;

@DisplayName("Spatial and temporal resolutions as typed RDF values")
class ResolutionsTest {

    private final Resolutions resolutions = new Resolutions(new CrsUri());

    private static SpatialResolution resolution(String distance, String uom) {
        return SpatialResolution.builder().distance(distance).uom(uom).build();
    }

    private static List<SpatialReferenceSystem> crs(String code) {
        return List.of(SpatialReferenceSystem.builder().code(code).build());
    }

    @ParameterizedTest(name = "[{0} {1}] -> [{2}]")
    @CsvSource(nullValues = "NULL", value = {
        "1000, urn:ogc:def:uom:EPSG::9001, 1000",
        "1000, m, 1000",
        "3, kilometre, 3000",
        "3, km, 3000",
        "3, urn:ogc:def:uom:EPSG::9036, 3000",
        "1000, metre, 1000",
        "5, metres, 5",
        "3, meters, 3",
        "2, Meter, 2",
        "1000, Meter, 1000",
        "'.25', m, 0.25",
        "'.01', m, 0.01",
        "0.50, m, 0.5",
        "' 7 ', ' M ', 7",
        "'1,000', m, ''",
        "approx 30, m, ''",
        "-5, m, ''",
        "0, m, ''",
        "30, urn:ogc:def:uom:EPSG::9103, ''",
        "1, urn:ogc:def:uom:EPSG::9102, ''",
        "1, urn:ogc:def:uom:EPSG::9108, ''",
        "1, deg, ''",
        "1, degrees, ''",
        "1, '\"', ''",
        "NULL, NULL, ''",
        "'', m, ''",
        "100, NULL, ''"
    })
    void metresWithUnit(String distance, String uom, String expected) {
        assertThat(resolutions.metres(resolution(distance, uom), List.of()), equalTo(expected));
    }

    @ParameterizedTest(name = "CRS [{0}] with no unit -> [{1}]")
    @CsvSource(nullValues = "NULL", value = {
        "27700, 100",
        "29901, 100",
        "3857, 100",
        "3035, 100",
        "32630, 100",
        "32730, 100",
        "4326, ''",
        "NULL, ''"
    })
    void metresInferredFromCrs(String code, String expected) {
        assertThat(resolutions.metres(resolution("100", null), crs(code)), equalTo(expected));
    }

    @Test
    void noResolutionGivesNothing() {
        assertThat(resolutions.metres(null, List.of()), equalTo(""));
        assertThat(resolutions.metres(
            SpatialResolution.builder().equivalentScale("50000").build(), crs("27700")), equalTo(""));
    }

    @Test
    void nullCrsListIsTolerated() {
        assertThat(resolutions.metres(resolution("100", null), null), equalTo(""));
    }

    @ParameterizedTest(name = "[{0}] -> [{1}]")
    @CsvSource(nullValues = "NULL", value = {
        "1 day, P1D",
        "daily, P1D",
        "30 min, PT30M",
        "30 minutes, PT30M",
        "1 month, P1M",
        "monthly, P1M",
        "Monthly, P1M",
        "1 year, P1Y",
        "yearly, P1Y",
        "2 years, P2Y",
        "28 days, P28D",
        "Weekly, P7D",
        "fortnightly, P14D",
        "15 minute, PT15M",
        "1 minute, PT1M",
        "1 hour, PT1H",
        "24 hours, PT24H",
        "72 hours, PT72H",
        "10 second, PT10S",
        "1 seconds, PT1S",
        "1 s, PT1S",
        "annual, P1Y",
        "Annual, P1Y",
        "annually, P1Y",
        "hourly, PT1H",
        "Daily, P1D",
        "decadal, P10Y",
        "two weeks, P14D",
        "Two weeks, P14D",
        "Four weeks, P28D",
        "2 weeks, P14D",
        "7 months, P7M",
        "1 Year, P1Y",
        "1d, P1D",
        "'1 day ', P1D",
        "'Fortnightly ', P14D",
        "3hr, PT3H",
        "5 mins, PT5M",
        "tens, ''",
        "tend, ''",
        "30m, ''",
        "bi-weekly, ''",
        "sub-minute, ''",
        "'\"100, 50, 20, 10, and 5 year simulations\"', ''",
        "0 days, ''",
        "'', ''",
        "NULL, ''"
    })
    void duration(String text, String expected) {
        assertThat(resolutions.duration(text), equalTo(expected));
        if (!expected.isEmpty()) {
            assertThat(expected, matchesPattern(XSD_DURATION));
            assertThat(XSDDatatype.XSDduration.isValid(expected), equalTo(true));
        }
    }

    @Test
    void crsRules() {
        assertThat(resolutions.metres(resolution("100", null), List.of(srs("27700"), srs("4326"))), equalTo(""));
        assertThat(resolutions.metres(resolution("100", null), List.of(srs("27700"))), equalTo("100"));
        assertThat(resolutions.metres(resolution("100", null), List.of(srs("27700"), srs("unknown"))), equalTo("100"));
        assertThat(resolutions.metres(resolution("100", null), List.of(srs("unknown"))), equalTo(""));
        assertThat(resolutions.metres(resolution("100", null), List.of()), equalTo(""));
    }

    private static SpatialReferenceSystem srs(String code) {
        return SpatialReferenceSystem.builder().code(code).build();
    }

    private static final String XSD_DURATION =
        "^P(?!$)(\\d+Y)?(\\d+M)?(\\d+D)?(T(?=\\d)(\\d+H)?(\\d+M)?(\\d+S)?)?$";
}
