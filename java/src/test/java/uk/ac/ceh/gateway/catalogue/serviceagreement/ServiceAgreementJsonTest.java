package uk.ac.ceh.gateway.catalogue.serviceagreement;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

class ServiceAgreementJsonTest {

    // mirrors WebConfig#objectMapper
    private final JsonMapper mapper = JsonMapper.builder()
        .changeDefaultPropertyInclusion(v -> v.withValueInclusion(JsonInclude.Include.NON_EMPTY))
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .findAndAddModules()
        .build();

    @Test
    void fileGroupsRoundTrip() {
        //given
        var group = new FileGroup();
        group.setPattern("site*/CP_*.csv");
        group.setCount(200);
        group.setFormat("CSV");
        group.setSize("443 MB");
        group.setDescription("Chamber point measurements, one file per site visit");
        var agreement = new ServiceAgreement();
        agreement.setFileGroups(List.of(group));

        //when
        var json = mapper.writeValueAsString(agreement);
        var actual = mapper.readValue(json, ServiceAgreement.class);

        //then
        assertThat(actual.getFileGroups(), equalTo(List.of(group)));
    }

    @Test
    void legacyAgreementWithoutGroupsLoads() {
        //given
        var json = """
            {"fileNamingConvention": "site_date.csv"}
            """;

        //when
        var actual = mapper.readValue(json, ServiceAgreement.class);

        //then
        assertThat(actual.getFileGroups(), nullValue());
        assertThat(actual.getFileNamingConvention(), equalTo("site_date.csv"));
    }
}
