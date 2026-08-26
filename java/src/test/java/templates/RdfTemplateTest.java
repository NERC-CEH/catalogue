package templates;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;
import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
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
import uk.ac.ceh.gateway.catalogue.templateHelpers.ContactUri;
import uk.ac.ceh.gateway.catalogue.templateHelpers.FundingUri;
import uk.ac.ceh.gateway.catalogue.templateHelpers.LicenceUri;
import uk.ac.ceh.gateway.catalogue.templateHelpers.UriNormaliser;

import java.io.File;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@Slf4j
@DisplayName("Rdf template")
public class RdfTemplateTest {
    Configuration configuration;
    JsonMapper objectMapper;
    JenaLookupService jena;

    @SneakyThrows
    private String expected(String filename) {
        val expected = Objects.requireNonNull(getClass().getResourceAsStream(filename));
        return IOUtils.toString(expected, StandardCharsets.UTF_8);
    }

    @SneakyThrows
    private String template(String templateFilename, Object model) {
        val string = FreeMarkerTemplateUtils.processTemplateIntoString(
            configuration.getTemplate(templateFilename),
            model
        );
        log.debug("Template: {}",templateFilename);
        log.debug(string);
        return string;
    }

    private void compare(String expected, String actual, boolean fragment) {
        if (fragment) {
            assertThat(actual.trim(), equalTo(expected.trim()));
            return;
        }

        Model expectedModel = ModelFactory.createDefaultModel();
        RDFDataMgr.read(expectedModel, new StringReader(expected), "https://example.com/id/", Lang.TTL);

        Model actualModel = ModelFactory.createDefaultModel();
        RDFDataMgr.read(actualModel, new StringReader(actual), "https://example.com/id/", Lang.TTL);

        if (log.isDebugEnabled()) {
            actualModel.listStatements().forEachRemaining(s -> log.debug(s.toString()));
        }

        assertTrue(
            actualModel.isIsomorphicWith(expectedModel),
            () -> "RDF models differ.\nExtra triples in actual: " + actualModel.difference(expectedModel).size()
                + "\nMissing triples from actual: " + expectedModel.difference(actualModel).size()
        );
    }

    @SneakyThrows
    @BeforeEach
    void init() {
        configuration = new Configuration(Configuration.VERSION_2_3_23);
        configuration.setDirectoryForTemplateLoading(new File("../templates"));
        objectMapper = JsonMapper.builder().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build();
        jena = mock(JenaLookupService.class);
        configuration.setSharedVariable("jena", jena);
        val uriNormaliser = new UriNormaliser();
        configuration.setSharedVariable("uriNormaliser", uriNormaliser);
        configuration.setSharedVariable("contactUri", new ContactUri(uriNormaliser));
        configuration.setSharedVariable("fundingUri", new FundingUri(uriNormaliser));
        configuration.setSharedVariable("licenceUris", new LicenceUri());
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

            given(jena.relationships(geminiDocument.getUri(), "http://purl.org/dc/terms/isPartOf")).willReturn(List.of(
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000012345").build(),
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000054321").build()
            ));
            given(jena.relationships(geminiDocument.getUri(), "http://purl.org/dc/terms/replaces")).willReturn(List.of(
                Link.builder().href("https://catalogue.ceh.ac.uk/id/111112345").build(),
                Link.builder().href("https://catalogue.ceh.ac.uk/id/111154321").build()
            ));
            given(jena.relationships(geminiDocument.getUri(), "http://purl.org/dc/terms/relation")).willReturn(List.of(
                Link.builder().href("https://catalogue.ceh.ac.uk/id/222212345").build(),
                Link.builder().href("https://catalogue.ceh.ac.uk/id/222254321").build()
            ));
            given(jena.relationships(geminiDocument.getUri(), "https://digital.ceh.ac.uk/ontology/doo/utilises")).willReturn(List.of(
                Link.builder().href("https://catalogue.ceh.ac.uk/id/333312345").build(),
                Link.builder().href("https://catalogue.ceh.ac.uk/id/333354321").build()
            ));

            // when
            val actual = template("rdf/ttl.ftl", geminiDocument);

            //then
            compare(expected, actual, false);
        }

        @Test
        @SneakyThrows
        @DisplayName("no trailing comma when last fileset has no observed properties")
        void datasetWithEmptyTrailingFileset() {
            val doc = objectMapper.readValue(
                expected("rdf/datastore/eidc-gemini-multi-fileset.raw"), GeminiDocument.class);

            given(jena.relationships(doc.getUri(), "http://purl.org/dc/terms/isPartOf")).willReturn(List.of());
            given(jena.relationships(doc.getUri(), "http://purl.org/dc/terms/replaces")).willReturn(List.of());
            given(jena.relationships(doc.getUri(), "http://purl.org/dc/terms/relation")).willReturn(List.of());

            val actual = template("rdf/ttl.ftl", doc);

            // If trailing comma is present, Jena throws RiotException on invalid Turtle
            Model model = ModelFactory.createDefaultModel();
            RDFDataMgr.read(model, new StringReader(actual), doc.getUri() + "/", Lang.TTL);

            Resource subject = model.createResource(doc.getUri());
            Property varMeasured = model.createProperty("https://schema.org/variableMeasured");
            assertTrue(model.contains(subject, varMeasured, model.createResource("https://prop-a.example.com")));
            assertTrue(model.contains(subject, varMeasured, model.createResource("https://prop-b.example.com")));
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
            compare(expected, actual, true);
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

            given(jena.relationships(facilityDocument.getUri(), "http://purl.org/dc/terms/relation")).willReturn(List.of(
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000000001").build(),
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000000002").build()
            ));
            given(jena.relationships(facilityDocument.getUri(), "https://digital.ceh.ac.uk/ontology/doo/hasChildFacility")).willReturn(List.of(
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000000003").build()
            ));
            given(jena.relationships(facilityDocument.getUri(), "http://purl.org/dc/terms/replaces")).willReturn(List.of(
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000000004").build()
            ));
            given(jena.relationships(facilityDocument.getUri(), "http://purl.org/dc/terms/isPartOf")).willReturn(List.of(
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000000005").build()
            ));

            //when
            val actual = template("rdf/monitoring/facility.ftl", facilityDocument);

            //then
            compare(expected, actual, false);
        }

        @Test
        @SneakyThrows
        @DisplayName("network ttl")
        void network() {
            //given
            val expected = expected("rdf/monitoring/network.ttl");

            val networkDocument = objectMapper.readValue(expected("rdf/datastore/monitoring-network.raw"), MonitoringNetwork.class);

            given(jena.relationships(networkDocument.getUri(), "http://purl.org/dc/terms/relation")).willReturn(List.of(
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000000001").build(),
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000000002").build()
            ));
            given(jena.relationships(networkDocument.getUri(), "https://digital.ceh.ac.uk/ontology/doo/hasChildNetwork")).willReturn(List.of(
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000000003").build()
            ));
            given(jena.relationships(networkDocument.getUri(), "http://purl.org/dc/terms/replaces")).willReturn(List.of(
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000000004").build()
            ));

            //when
            val actual = template("rdf/monitoring/network.ftl", networkDocument);

            //then
            compare(expected, actual, false);
        }

        @Test
        @SneakyThrows
        @DisplayName("programme ttl")
        void programme() {
            //given
            val expected = expected("rdf/monitoring/programme.ttl");

            val programmeDocument = objectMapper.readValue(expected("rdf/datastore/monitoring-programme.raw"), MonitoringProgramme.class);

            given(jena.relationships(programmeDocument.getUri(), "http://purl.org/dc/terms/relation")).willReturn(List.of(
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000000001").build(),
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000000002").build()
            ));
            given(jena.relationships(programmeDocument.getUri(), "http://purl.org/dc/terms/replaces")).willReturn(List.of(
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000000003").build()
            ));
            given(jena.relationships(programmeDocument.getUri(), "https://digital.ceh.ac.uk/ontology/doo/utilises")).willReturn(List.of(
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000000004").build()
            ));
            given(jena.relationships(programmeDocument.getUri(), "https://digital.ceh.ac.uk/ontology/doo/hasChildProgramme")).willReturn(List.of(
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000000005").build()
            ));
            given(jena.relationships(programmeDocument.getUri(), "https://digital.ceh.ac.uk/ontology/doo/triggers")).willReturn(List.of(
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000000006").build()
            ));

            //when
            val actual = template("rdf/monitoring/programme.ftl", programmeDocument);

            //then
            compare(expected, actual, false);
        }

        @Test
        @SneakyThrows
        @DisplayName("activity ttl")
        void activity() {
            //given
            val expected = expected("rdf/monitoring/activity.ttl");

            val activityDocument = objectMapper.readValue(expected("rdf/datastore/monitoring-activity.raw"), MonitoringActivity.class);

            given(jena.relationships(activityDocument.getUri(),  "https://digital.ceh.ac.uk/ontology/doo/uses")).willReturn(List.of(
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000000001").build(),
                Link.builder().href("https://catalogue.ceh.ac.uk/id/000000002").build()
            ));

            //when
            val actual = template("rdf/monitoring/activity.ftl", activityDocument);

            //then
            compare(expected, actual, false);
        }
    }
}

