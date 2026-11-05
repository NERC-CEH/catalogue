package uk.ac.ceh.gateway.catalogue.templateHelpers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.ac.ceh.gateway.catalogue.gemini.SpatialReferenceSystem;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@DisplayName("Canonical EPSG IRIs for spatial reference systems")
class CrsUriTest {

    private static final String BNG = "http://www.opengis.net/def/crs/EPSG/0/27700";

    private final CrsUri crsUri = new CrsUri();

    @ParameterizedTest(name = "[{0}] in [{1}] -> [{2}]")
    @CsvSource(nullValues = "NULL", value = {
        "http://www.opengis.net/def/crs/EPSG/0/27700, NULL, http://www.opengis.net/def/crs/EPSG/0/27700",
        "27700, urn:ogc:def:crs:EPSG, http://www.opengis.net/def/crs/EPSG/0/27700",
        "EPSG:29903, NULL, http://www.opengis.net/def/crs/EPSG/0/29903",
        "https://epsg.io/29901, NULL, http://www.opengis.net/def/crs/EPSG/0/29901",
        "CRS:84, urn:ogc:def:crs:EPSG, http://www.opengis.net/def/crs/OGC/1.3/CRS84",
        "https://data.ordnancesurvey.co.uk/datasets/opennames, NULL, ''",
        "unknown, NULL, ''",
        "'', '', ''",
        "NULL, NULL, ''",
        "27700, NULL, http://www.opengis.net/def/crs/EPSG/0/27700",
        "EPSG:abc, NULL, ''",
        "http://www.opengis.net/def/crs/EPSG/0/, NULL, ''",
        "102028, NULL, ''",
        "29903, NULL, http://www.opengis.net/def/crs/EPSG/0/29903",
        "'EPSG: 29903', NULL, http://www.opengis.net/def/crs/EPSG/0/29903",
        "epsg:27700, NULL, http://www.opengis.net/def/crs/EPSG/0/27700",
        "http://www.opengis.net/def/crs/EPSG/0/27700/, NULL, ''",
        "0999, NULL, ''",
        "1023, NULL, ''",
        "1024, NULL, http://www.opengis.net/def/crs/EPSG/0/1024",
        "32767, NULL, http://www.opengis.net/def/crs/EPSG/0/32767",
        "32768, NULL, ''",
        "102028, urn:ogc:def:crs:EPSG, http://www.opengis.net/def/crs/EPSG/0/102028"
    })
    void canonicalises(String code, String codeSpace, String expected) {
        SpatialReferenceSystem srs = SpatialReferenceSystem.builder().code(code).codeSpace(codeSpace).build();
        assertThat(crsUri.canonical(srs), equalTo(expected));
    }

    @Test
    void nullSystemYieldsBlank() {
        assertThat(crsUri.canonical(null), equalTo(""));
    }

    @Test
    void titleIsIgnored() {
        SpatialReferenceSystem srs = SpatialReferenceSystem.builder().code("27700").title("British National Grid").build();
        assertThat(crsUri.canonical(srs), equalTo(BNG));
    }
}
