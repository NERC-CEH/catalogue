package uk.ac.ceh.gateway.catalogue.templateHelpers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@DisplayName("EU frequency IRIs for ISO maintenance frequencies")
class FrequencyUriTest {

    private static final String BASE = "http://publications.europa.eu/resource/authority/frequency/";

    private final FrequencyUri frequencyUri = new FrequencyUri();

    @ParameterizedTest(name = "[{0}] -> [{1}]")
    @CsvSource(nullValues = "NULL", value = {
        "continual, CONT",
        "daily, DAILY",
        "weekly, WEEKLY",
        "fortnightly, BIWEEKLY",
        "monthly, MONTHLY",
        "quarterly, QUARTERLY",
        "biannually, ANNUAL_2",
        "annually, ANNUAL",
        "irregular, IRREG",
        "unknown, UNKNOWN",
        "asNeeded, AS_NEEDED",
        "notPlanned, NOT_PLANNED"
    })
    void mapsIsoCodes(String iso, String eu) {
        assertThat(frequencyUri.uri(iso), equalTo(BASE + eu));
    }

    @ParameterizedTest(name = "[{0}] -> blank")
    @CsvSource(nullValues = "NULL", value = {"'', ", "NULL", "sometimes", "'  '"})
    void unmappedYieldsBlank(String iso) {
        assertThat(frequencyUri.uri(iso), equalTo(""));
    }
}
