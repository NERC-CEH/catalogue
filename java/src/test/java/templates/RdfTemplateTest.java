package templates;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringActivity;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringFacility;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringNetwork;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringProgramme;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@Slf4j
@DisplayName("Rdf template")
public class RdfTemplateTest {
    Configuration configuration;
    ObjectMapper objectMapper;
    JenaLookupService jena;

    @SneakyThrows
    private String expected(String filename) {
        val expected = Objects.requireNonNull(getClass().getResourceAsStream(filename));
        return IOUtils.toString(expected, StandardCharsets.UTF_8);
    }

    @SneakyThrows
    private String template(String templateFilename, Object model) {
        return FreeMarkerTemplateUtils.processTemplateIntoString(
            configuration.getTemplate(templateFilename),
            model
        );
    }

    private void compare(String expected, String actual) {
        assertThat(actual.trim(), equalTo(expected.trim()));
    }

    @SneakyThrows
    @BeforeEach
    void init() {
        configuration = new Configuration(Configuration.VERSION_2_3_23);
        configuration.setDirectoryForTemplateLoading(new File("../templates"));
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jena = mock(JenaLookupService.class);
        configuration.setSharedVariable("jena", jena);
    }

    @Nested
    @DisplayName("Eidc documents")
    class Eidc {

        @Test
        @SneakyThrows
        @DisplayName("full dataset ttl")
        void dataset() {
            // given
            val expected = expected("rdf/eidc/gemini.ttl");

            val geminiDocument = objectMapper.readValue(expected("rdf/datastore/eidc-gemini.raw"), GeminiDocument.class);

            String predicateURI="https://vocabs.ceh.ac.uk/eidc#";
            given(jena.relationships(geminiDocument.getUri(), predicateURI + "memberOf")).willReturn(List.of(
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000012345").build(),
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000054321").build()
            ));
            given(jena.relationships(geminiDocument.getUri(), predicateURI + "supersedes")).willReturn(List.of(
                Link.builder().href("https://catalogue.ceh.ac.uk/id/111112345").build(),
                Link.builder().href("https://catalogue.ceh.ac.uk/id/111154321").build()
            ));
            given(jena.relationships(geminiDocument.getUri(), predicateURI + "relatedTo")).willReturn(List.of(
                Link.builder().href("https://catalogue.ceh.ac.uk/id/222212345").build(),
                Link.builder().href("https://catalogue.ceh.ac.uk/id/222254321").build()
            ));

            // when
            val actual = template("rdf/ttl.ftl", geminiDocument);

            //then
            compare(expected, actual);
        }

        @Test
        @SneakyThrows
        @DisplayName("aggregation part only")
        void aggregation() {
            // given
            val expected = expected("rdf/eidc/aggregation.ttl");

            val gemini = objectMapper.readValue(expected("rdf/datastore/eidc-gemini.raw"), GeminiDocument.class);

            given(jena.incomingEidcRelations(gemini.getUri())).willReturn(List.of(
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000012345").build(),
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000054321").build()
            ));

            // when
            val actual = template("rdf/turtle/_aggregation.ftl", gemini);

            //then
            compare(expected, actual);
        }
    }

    @Nested
    @DisplayName("Monitoring documents")
    class Monitoring {

        @Test
        @SneakyThrows
        @DisplayName("facility ttl")
        void facility() {
            //given
            val expected = expected("rdf/monitoring/facility.ttl");

            val facilityDocument = objectMapper.readValue(expected("rdf/datastore/monitoring-facility.raw"), MonitoringFacility.class);

            String predicateURI="http://onto.ceh.ac.uk/EF#";
            given(jena.relationships(facilityDocument.getUri(), predicateURI + "associatedWith")).willReturn(List.of(
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000000001").build(),
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000000002").build()
            ));
            given(jena.relationships(facilityDocument.getUri(), predicateURI + "narrower")).willReturn(List.of(
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000000003").build()
            ));
            given(jena.relationships(facilityDocument.getUri(), predicateURI + "supersedes")).willReturn(List.of(
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000000004").build()
            ));
            given(jena.relationships(facilityDocument.getUri(), predicateURI + "belongsTo")).willReturn(List.of(
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000000005").build()
            ));

            //when
            val actual = template("rdf/monitoring/facility.ftl", facilityDocument);

            //then
            compare(expected, actual);
        }

        @Test
        @SneakyThrows
        @DisplayName("network ttl")
        void network() {
            //given
            val expected = expected("rdf/monitoring/network.ttl");

            val networkDocument = objectMapper.readValue(expected("rdf/datastore/monitoring-network.raw"), MonitoringNetwork.class);

            String predicateURI="http://onto.ceh.ac.uk/EF#";
            given(jena.relationships(networkDocument.getUri(), predicateURI + "associatedWith")).willReturn(List.of(
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000000001").build(),
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000000002").build()
            ));
            given(jena.relationships(networkDocument.getUri(), predicateURI + "narrower")).willReturn(List.of(
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000000003").build()
            ));
            given(jena.relationships(networkDocument.getUri(), predicateURI + "supersedes")).willReturn(List.of(
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000000004").build()
            ));

            //when
            val actual = template("rdf/monitoring/network.ftl", networkDocument);

            //then
            compare(expected, actual);
        }

        @Test
        @SneakyThrows
        @DisplayName("programme ttl")
        void programme() {
            //given
            val expected = expected("rdf/monitoring/programme.ttl");

            val programmeDocument = objectMapper.readValue(expected("rdf/datastore/monitoring-programme.raw"), MonitoringProgramme.class);

            String predicateURI="http://onto.ceh.ac.uk/EF#";
            given(jena.relationships(programmeDocument.getUri(), predicateURI + "associatedWith")).willReturn(List.of(
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000000001").build(),
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000000002").build()
            ));
            given(jena.relationships(programmeDocument.getUri(), predicateURI + "supersedes")).willReturn(List.of(
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000000003").build()
            ));
            given(jena.relationships(programmeDocument.getUri(), predicateURI + "utilises")).willReturn(List.of(
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000000004").build()
            ));
            given(jena.relationships(programmeDocument.getUri(), predicateURI + "hasChild")).willReturn(List.of(
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000000005").build()
            ));
            given(jena.relationships(programmeDocument.getUri(), predicateURI + "triggers")).willReturn(List.of(
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000000006").build()
            ));

            //when
            val actual = template("rdf/monitoring/programme.ftl", programmeDocument);

            //then
            compare(expected, actual);
        }

        @Test
        @SneakyThrows
        @DisplayName("activity ttl")
        void activity() {
            //given
            val expected = expected("rdf/monitoring/activity.ttl");

            val activityDocument = objectMapper.readValue(expected("rdf/datastore/monitoring-activity.raw"), MonitoringActivity.class);

            String predicateURI="http://onto.ceh.ac.uk/EF#";
            given(jena.relationships(activityDocument.getUri(), predicateURI + "uses")).willReturn(List.of(
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000000001").build(),
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000000002").build()
            ));

            //when
            val actual = template("rdf/monitoring/activity.ftl", activityDocument);

            //then
            compare(expected, actual);
        }
    }
}

