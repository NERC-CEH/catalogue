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

    @Test
    void mediaTypesAndByteSizesRoundTrip() {
        //given
        var file = new File();
        file.setName("a.csv");
        file.setFormat("Comma-separated values (CSV)");
        file.setFormatType("text/csv");
        file.setBytes(33554432L);
        var group = new FileGroup();
        group.setPattern("site*/CP_*.csv");
        group.setFormatType("text/csv");
        group.setMinBytes(2097152L);
        group.setMaxBytes(2621440L);
        group.setTotalBytes(4294967296L);
        var agreement = new ServiceAgreement();
        agreement.setFiles(List.of(file));
        agreement.setFileGroups(List.of(group));

        //when
        var json = mapper.writeValueAsString(agreement);
        var actual = mapper.readValue(json, ServiceAgreement.class);

        //then
        assertThat(json.contains("\"formatType\":\"text/csv\""), equalTo(true));
        assertThat(json.contains("\"bytes\":33554432"), equalTo(true));
        assertThat(json.contains("\"minBytes\":2097152"), equalTo(true));
        assertThat(json.contains("\"maxBytes\":2621440"), equalTo(true));
        assertThat(json.contains("\"totalBytes\":4294967296"), equalTo(true));
        assertThat(actual.getFiles(), equalTo(List.of(file)));
        assertThat(actual.getFileGroups(), equalTo(List.of(group)));
    }

    @Test
    void legacyFileWithoutNewFieldsLoadsWithThemNull() {
        //given
        var json = """
            {"files": [{"name": "a.csv", "format": "csv", "size": "32Mb"}],
             "fileGroups": [{"pattern": "a*", "format": "csv", "size": "1 MB"}]}
            """;

        //when
        var actual = mapper.readValue(json, ServiceAgreement.class);

        //then
        var file = actual.getFiles().get(0);
        assertThat(file.getSize(), equalTo("32Mb"));
        assertThat(file.getFormatType(), nullValue());
        assertThat(file.getBytes(), nullValue());
        var group = actual.getFileGroups().get(0);
        assertThat(group.getFormatType(), nullValue());
        assertThat(group.getMinBytes(), nullValue());
        assertThat(group.getMaxBytes(), nullValue());
        assertThat(group.getTotalBytes(), nullValue());
    }
}
