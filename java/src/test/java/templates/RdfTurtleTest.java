package templates;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import uk.ac.ceh.gateway.catalogue.gemini.Funding;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceConstraint;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.model.Supplemental;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringActivity;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringFacility;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringNetwork;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringProgramme;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;
import uk.ac.ceh.gateway.catalogue.templateHelpers.UriNormaliser;

import java.io.File;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;

import static org.apache.jena.rdf.model.ResourceFactory.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

@Slf4j
@DisplayName("RDF Turtle templating")
@ExtendWith(MockitoExtension.class)
public class RdfTurtleTest {

    Configuration configuration;
    Model model;
    @Mock JenaLookupService jenaLookupService;

    @SneakyThrows
    @BeforeEach
    void init() {
        configuration = new Configuration(Configuration.VERSION_2_3_32);
        configuration.setDirectoryForTemplateLoading(new File("../templates"));
        configuration.setSharedVariable("jena", jenaLookupService);
        configuration.setSharedVariable("uriNormaliser", new UriNormaliser());

        model = ModelFactory.createDefaultModel();
    }

    @SneakyThrows
    private void template(String templateFilename, Object document) {
        val string = FreeMarkerTemplateUtils.processTemplateIntoString(
            configuration.getTemplate(templateFilename),
            document
        );
        log.debug("Template: {}",templateFilename);
        log.debug(string);
        RDFDataMgr.read(model, new StringReader(string), "https://example.com/id/", Lang.TTL);
        if (log.isDebugEnabled()) {
            model.listStatements().forEachRemaining(s -> log.debug(s.toString()));
        }
    }

    @Nested
    @DisplayName("Monitoring things")
    class Monitoring {

        @Test
        void loadActivity() {
            //given
            val activity = new MonitoringActivity()
                .setId("9371")
                .setUri("https://example.com/id/9371")
                .setTitle("Kelp");

            //when
            template("rdf/monitoring/activity.ftl", activity);

            //then
            assertTrue(
                model.contains(
                    createStatement(
                        createResource("https://example.com/id/9371"),
                        createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                        createResource("https://digital.ceh.ac.uk/ontology/doo/EnvironmentalMonitoringActivity")
                    )
                )
            );
        }

        @Test
        void loadFacility() {
            //given
            val facility = new MonitoringFacility()
                .setId("1234")
                .setUri("https://example.com/id/1234")
                .setTitle("Test");

            //when
            template("rdf/monitoring/facility.ftl", facility);

            //then
            assertTrue(
                model.contains(
                    createStatement(
                        createResource("https://example.com/id/1234"),
                        createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                        createResource("https://digital.ceh.ac.uk/ontology/doo/EnvironmentalMonitoringFacility")
                    )
                )
            );
        }

        @Test
        void loadNetwork() {
            //given
            val network = new MonitoringNetwork()
                .setId("7453")
                .setUri("https://example.com/id/7453")
                .setTitle("Newton");

            //when
            template("rdf/monitoring/network.ftl", network);

            //then
            assertTrue(
                model.contains(
                    createStatement(
                        createResource("https://example.com/id/7453"),
                        createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                        createResource("https://digital.ceh.ac.uk/ontology/doo/EnvironmentalMonitoringNetwork")
                    )
                )
            );
        }

        @Test
        void loadProgramme() {
            //given
            val programme = new MonitoringProgramme()
                .setId("5566")
                .setUri("https://example.com/id/5566")
                .setTitle("Rainfall");

            //when
            template("rdf/monitoring/programme.ftl", programme);

            //then
            assertTrue(
                model.contains(
                    createStatement(
                        createResource("https://example.com/id/5566"),
                        createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                        createResource("https://digital.ceh.ac.uk/ontology/doo/EnvironmentalMonitoringProgramme")
                    )
                )
            );
        }
    }

    @Nested
    @DisplayName("Combined catalogue Turtle (FusekiExportService path)")
    class CombinedCatalogueTurtle {

        @SneakyThrows
        private void combinedTemplate(String unprefixedTemplate, Object document) {
            val catalogueModel = new HashMap<String, Object>();
            catalogueModel.put("baseUri", "https://example.com");
            catalogueModel.put("catalogue", "eidc");
            catalogueModel.put("title", "Test");
            catalogueModel.put("records", List.of());

            val catalogueTtl = FreeMarkerTemplateUtils.processTemplateIntoString(
                configuration.getTemplate("rdf/catalogue.ttl.ftl"), catalogueModel
            );
            val recordTtl = FreeMarkerTemplateUtils.processTemplateIntoString(
                configuration.getTemplate(unprefixedTemplate), document
            );
            val combined = catalogueTtl + "\n" + recordTtl;
            log.debug("Combined: {}", combined);
            RDFDataMgr.read(model, new StringReader(combined), "https://example.com/id/", Lang.TTL);
        }

        @Test
        void unprefixedActivityParses() {
            val activity = new MonitoringActivity()
                .setId("9371")
                .setUri("https://example.com/id/9371")
                .setTitle("Kelp");
            combinedTemplate("rdf/monitoring/unprefixed/activity.ftl", activity);
            assertTrue(model.contains(
                createStatement(
                    createResource("https://example.com/id/9371"),
                    createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                    createResource("https://digital.ceh.ac.uk/ontology/doo/EnvironmentalMonitoringActivity")
                )
            ));
        }

        @Test
        void unprefixedFacilityParses() {
            val facility = new MonitoringFacility()
                .setId("1234")
                .setUri("https://example.com/id/1234")
                .setTitle("Test");
            combinedTemplate("rdf/monitoring/unprefixed/facility.ftl", facility);
            assertTrue(model.contains(
                createStatement(
                    createResource("https://example.com/id/1234"),
                    createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                    createResource("https://digital.ceh.ac.uk/ontology/doo/EnvironmentalMonitoringFacility")
                )
            ));
        }

        @Test
        void unprefixedNetworkParses() {
            val network = new MonitoringNetwork()
                .setId("7453")
                .setUri("https://example.com/id/7453")
                .setTitle("Newton");
            combinedTemplate("rdf/monitoring/unprefixed/network.ftl", network);
            assertTrue(model.contains(
                createStatement(
                    createResource("https://example.com/id/7453"),
                    createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                    createResource("https://digital.ceh.ac.uk/ontology/doo/EnvironmentalMonitoringNetwork")
                )
            ));
        }

        @Test
        void unprefixedProgrammeParses() {
            val programme = new MonitoringProgramme()
                .setId("5566")
                .setUri("https://example.com/id/5566")
                .setTitle("Rainfall");
            combinedTemplate("rdf/monitoring/unprefixed/programme.ftl", programme);
            assertTrue(model.contains(
                createStatement(
                    createResource("https://example.com/id/5566"),
                    createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                    createResource("https://digital.ceh.ac.uk/ontology/doo/EnvironmentalMonitoringProgramme")
                )
            ));
        }
    }

    @Nested
    @DisplayName("Gemini documents")
    class Dataset {

        private void givenRelationshipMemberOf(String uri) {
            given(jenaLookupService.relationships(uri, "http://purl.org/dc/terms/isPartOf"))
                .willReturn(
                    List.of(
                        Link.builder().href("https://example.com/id/283746").build(),
                        Link.builder().href("https://example.com/id/932425").build()
                    )
                );
        }

        private void givenEidcIncomingRelation(String uri) {
            given(jenaLookupService.incomingEidcRelations(uri))
                .willReturn(
                    List.of(
                        Link.builder().href("https://example.com/id/66677744").build(),
                        Link.builder().href("https://example.com/id/22567822").build()
                    )
                );
        }

        @Test
        void loadGeminiDataset() {
            //given
            val uri = "https://example.com/id/387";
            val document = new GeminiDocument()
                .setType("dataset")
                .setId("387")
                .setUri(uri)
                .setTitle("Test")
                .setDescription("Description\n\nwith multiple line indents")
                .setResourceIdentifiers(
                    List.of(
                        ResourceIdentifier.builder().code("https://example.com/id/283746").build(),
                        ResourceIdentifier.builder().code("932425").build()
                    )
                );

            givenRelationshipMemberOf(uri);

            //when
            template( "rdf/ttl.ftl", document);

            //then
            assertTrue(
                model.contains(
                    createStatement(
                        createResource(uri),
                        createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                        createResource("http://www.w3.org/ns/dcat#Dataset")
                    )
                )
            );
        }

        @Test
        void loadGeminiAggregation() {
            //given
            val uri = "https://example.com/id/99987654";
            val document = new GeminiDocument()
                .setType("aggregate")
                .setId("99987654")
                .setUri(uri)
                .setTitle("Aggregation");

            givenEidcIncomingRelation(uri);

            //when
            template( "rdf/ttl.ftl", document);

            //then
            assertTrue(
                model.contains(
                    createStatement(
                        createResource(uri),
                        createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                        createResource("http://purl.org/dc/dcmitype/Collection")
                    )
                )
            );
        }

        @Test
        void loadGeminiService() {
            //given
            val uri = "https://example.com/id/47583";
            val document = new GeminiDocument()
                .setType("service")
                .setId("47583")
                .setUri(uri)
                .setTitle("Service");

            //when
            template( "rdf/ttl.ftl", document);

            //then
            assertTrue(
                model.contains(
                    createStatement(
                        createResource(uri),
                        createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                        createResource("http://purl.org/dc/dcmitype/Service")
                    )
                )
            );
        }

        @Test
        void loadGeminiSoftware() {
            //given
            val uri = "https://example.com/id/12678007";
            val document = new GeminiDocument()
                .setType("software")
                .setId("12678007")
                .setUri(uri)
                .setTitle("Software");

            //when
            template( "rdf/ttl.ftl", document);

            //then
            assertTrue(
                model.contains(
                    createStatement(
                        createResource(uri),
                        createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                        createResource("http://purl.org/dc/dcmitype/Software")
                    )
                )
            );
        }

        @Test
        void loadCatalogue() {
            //given
            val document = new HashMap<String, Object>();
            document.put("baseUri", "https://example.com");
            document.put("catalogue", "eidc");
            document.put("title", "Test");
            document.put("records", List.of("283746", "932425"));

            //when
            template( "rdf/catalogue.ttl.ftl", document);

            //then
            assertTrue(
                model.contains(
                    createStatement(
                        createResource("https://example.com/eidc/documents"),
                        createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                        createResource("http://www.w3.org/ns/dcat#Catalog")
                    )
                )
            );
        }

        @Test
        void loadGeminiWithNonOrcidContact() {
            // Contacts without ORCID/ROR fall back to prefixed-name identifiers (:docId_c0).
            // This test catches regressions where the `:` prefix is dropped, producing invalid Turtle.
            val uri = "https://example.com/id/conttest";
            GeminiDocument document = new GeminiDocument();
            document.setType("dataset");
            document.setId("conttest");
            document.setUri(uri);
            document.setTitle("Contact test");
            document.setContactPoints(List.of(
                ResponsibleParty.builder()
                    .familyName("Smith")
                    .givenName("John")
                    .organisationName("Test Organisation")
                    .build()
            ));

            template("rdf/ttl.ftl", document);

            assertTrue(model.contains(
                createStatement(
                    createResource(uri),
                    createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                    createResource("http://www.w3.org/ns/dcat#Dataset")
                )
            ));
        }

        @Test
        void loadGeminiWithFundingNoAwardUri() {
            // Funding without awardURI falls back to prefixed-name identifier (:docId_fund0).
            // This test catches regressions where the `:` prefix is dropped on that branch.
            val uri = "https://example.com/id/fundtest";
            GeminiDocument document = new GeminiDocument();
            document.setType("dataset");
            document.setId("fundtest");
            document.setUri(uri);
            document.setTitle("Funding test");
            document.setFunding(List.of(Funding.builder().awardTitle("Grant X").build()));

            template("rdf/ttl.ftl", document);

            assertTrue(model.contains(
                createStatement(
                    createResource(uri),
                    createProperty("http://www.w3.org/ns/prov#wasGeneratedBy"),
                    model.listObjectsOfProperty(
                        createResource(uri),
                        createProperty("http://www.w3.org/ns/prov#wasGeneratedBy")
                    ).next().asResource()
                )
            ));
        }

        @Test
        void loadGeminiWithCitationNoUrl() {
            // Incoming citations without a URL fall back to prefixed-name identifier (:docId_citation0).
            // This test catches regressions where the `:` prefix is dropped on that branch.
            val uri = "https://example.com/id/citationtest";
            GeminiDocument document = new GeminiDocument();
            document.setType("dataset");
            document.setId("citationtest");
            document.setUri(uri);
            document.setTitle("Citation test");
            document.setIncomingCitations(List.of(Supplemental.builder().description("Test citation desc").build()));

            template("rdf/ttl.ftl", document);

            assertTrue(model.contains(
                createStatement(
                    createResource(uri),
                    createProperty("http://purl.org/dc/terms/isReferencedBy"),
                    model.listObjectsOfProperty(
                        createResource(uri),
                        createProperty("http://purl.org/dc/terms/isReferencedBy")
                    ).next().asResource()
                )
            ));
        }

        @Nested
        @DisplayName("Externally-supplied URIs are canonicalised (dri-one #318)")
        class UriCanonicalisation {

            private GeminiDocument dataset(String id) {
                return (GeminiDocument) new GeminiDocument()
                    .setType("dataset")
                    .setId(id)
                    .setUri("https://example.com/id/" + id)
                    .setTitle("URI canonicalisation");
            }

            @Test
            @DisplayName("a keyword concept URI loses its stray trailing slash and gains https")
            void keywordUriCanonicalised() {
                val document = dataset("kwtest");
                document.setKeywordsOther(List.of(
                    Keyword.builder().value("Scotland").URI("http://sws.geonames.org/2638360/").build()
                ));

                template("rdf/ttl.ftl", document);

                assertTrue(model.containsResource(createResource("https://sws.geonames.org/2638360")));
                assertFalse(model.containsResource(createResource("http://sws.geonames.org/2638360/")));
            }

            @Test
            @DisplayName("keywordList and keywordDetail agree on the canonical node")
            void keywordListAndDetailAgree() {
                val document = dataset("kwagree");
                document.setKeywordsOther(List.of(
                    Keyword.builder().value("Scotland").URI("http://sws.geonames.org/2638360/").build()
                ));

                template("rdf/ttl.ftl", document);

                val concept = createResource("https://sws.geonames.org/2638360");
                assertTrue(model.contains(
                    createResource("https://example.com/id/kwagree"),
                    createProperty("http://purl.org/dc/terms/subject"),
                    concept
                ));
                assertTrue(model.contains(
                    concept,
                    createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                    createResource("http://www.w3.org/2004/02/skos/core#Concept")
                ));
            }

            @Test
            @DisplayName("a malformed keyword URI becomes a label rather than a dead-end node")
            void malformedKeywordUriFallsBackToLabel() {
                val document = dataset("kwbad");
                document.setKeywordsOther(List.of(
                    Keyword.builder()
                        .value("Rainfall rate")
                        .URI("hhttp://vocab.nerc.ac.uk/collection/N07/current/RAUT/")
                        .build()
                ));

                template("rdf/ttl.ftl", document);

                assertTrue(model.contains(
                    createResource("https://example.com/id/kwbad"),
                    createProperty("http://purl.org/dc/terms/subject"),
                    model.createLiteral("Rainfall rate")
                ));
                assertTrue(model.listStatements(null, null, (org.apache.jena.rdf.model.RDFNode) null)
                    .toList().stream()
                    .noneMatch(st -> st.getObject().toString().contains("vocab.nerc.ac.uk")));
            }

            @Test
            @DisplayName("a NERC vocabulary concept keeps the trailing slash that makes it resolve")
            void significantTrailingSlashKept() {
                val document = dataset("kwslash");
                document.setKeywordsOther(List.of(
                    Keyword.builder()
                        .value("Rainfall rate")
                        .URI("http://vocab.nerc.ac.uk/collection/N07/current/RAUT/")
                        .build()
                ));

                template("rdf/ttl.ftl", document);

                assertTrue(model.containsResource(
                    createResource("http://vocab.nerc.ac.uk/collection/N07/current/RAUT/")
                ));
            }

            @Test
            @DisplayName("two spellings of one grant award collapse to a single prov:Activity")
            void fundingAwardUrisConverge() {
                val document = dataset("fundconv");
                document.setFunding(List.of(
                    Funding.builder()
                        .awardTitle("Grant A")
                        .awardURI("http://gtr.ukri.org/projects?ref=NE%2FS008926%2F1")
                        .build(),
                    Funding.builder()
                        .awardTitle("Grant A")
                        .awardURI("https://gtr.ukri.org/projects?ref=NE/S008926/1")
                        .build()
                ));

                template("rdf/ttl.ftl", document);

                val grant = createResource("https://gtr.ukri.org/projects?ref=NE/S008926/1");
                assertTrue(model.contains(
                    createResource("https://example.com/id/fundconv"),
                    createProperty("http://www.w3.org/ns/prov#wasGeneratedBy"),
                    grant
                ));
                assertThat(
                    model.listObjectsOfProperty(createProperty("http://www.w3.org/ns/prov#wasGeneratedBy"))
                        .toSet()
                        .size(),
                    equalTo(1)
                );
            }

            @Test
            @DisplayName("an http ORCID is emitted as the https identifier it shares with its twin")
            void orcidUpgradedToHttps() {
                val document = dataset("orcidtest");
                document.setAuthors(List.of(
                    ResponsibleParty.builder()
                        .familyName("Smith")
                        .givenName("John")
                        .organisationName("Test Organisation")
                        .nameIdentifier("http://orcid.org/0000-0001-2345-6789")
                        .build()
                ));

                template("rdf/ttl.ftl", document);

                assertTrue(model.contains(
                    createResource("https://example.com/id/orcidtest"),
                    createProperty("http://purl.org/dc/terms/creator"),
                    createResource("https://orcid.org/0000-0001-2345-6789")
                ));
                assertFalse(model.containsResource(createResource("http://orcid.org/0000-0001-2345-6789")));
            }
        }

        @Nested
        class Rights {

            @Test
            void rights() {
                //given
                val uri = "https://example.com/id/9837";
                val document = new GeminiDocument()
                    .setType("dataset")
                    .setUseConstraints(List.of(
                        ResourceConstraint.builder().code("license").uri("https://example.com/licences/ogl/plain").value("OGL").build(),
                        ResourceConstraint.builder().code("other").uri("https://example.com/other").build()
                    ))
                    .setUri(uri)
                    .setId("9837")
                    .setTitle("Test");

                //when
                template("rdf/ttl.ftl", document);

                //then
                assertTrue(
                    model.contains(
                        createStatement(
                            createResource(uri),
                            createProperty("http://purl.org/dc/terms/license"),
                            createResource("https://spdx.org/licenses/OGL-UK-3.0.ttl")
                        )
                    )
                );
            }
        }
    }
}
