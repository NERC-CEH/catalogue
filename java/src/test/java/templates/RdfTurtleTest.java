package templates;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
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
import uk.ac.ceh.gateway.catalogue.gemini.AccessLimitation;
import uk.ac.ceh.gateway.catalogue.gemini.DistributionInfo;
import uk.ac.ceh.gateway.catalogue.gemini.Funding;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceConstraint;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.model.Supplemental;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.model.Fileset;
import uk.ac.ceh.gateway.catalogue.model.ObservedProperty;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringActivity;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringFacility;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringNetwork;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringProgramme;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;
import uk.ac.ceh.gateway.catalogue.templateHelpers.ContactUri;
import uk.ac.ceh.gateway.catalogue.templateHelpers.FundingUri;
import uk.ac.ceh.gateway.catalogue.templateHelpers.FormatUri;
import uk.ac.ceh.gateway.catalogue.templateHelpers.LicenceUri;
import uk.ac.ceh.gateway.catalogue.templateHelpers.UriNormaliser;

import java.io.File;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;

import static org.apache.jena.rdf.model.ResourceFactory.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesRegex;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import java.util.Optional;
import static org.mockito.Mockito.verifyNoInteractions;
import uk.ac.ceh.gateway.catalogue.templateHelpers.KeywordUri;
import uk.ac.ceh.gateway.catalogue.vocabularies.KeywordVocabularySolrQueryService;

@Slf4j
@DisplayName("RDF Turtle templating")
@ExtendWith(MockitoExtension.class)
public class RdfTurtleTest {

    Configuration configuration;
    Model model;
    @Mock JenaLookupService jenaLookupService;
    @Mock KeywordVocabularySolrQueryService keywordVocabulary;

    @SneakyThrows
    @BeforeEach
    void init() {
        configuration = new Configuration(Configuration.VERSION_2_3_32);
        configuration.setDirectoryForTemplateLoading(new File("../templates"));
        configuration.setSharedVariable("jena", jenaLookupService);
        val uriNormaliser = new UriNormaliser();
        configuration.setSharedVariable("uriNormaliser", uriNormaliser);
        configuration.setSharedVariable("contactUri", new ContactUri(uriNormaliser));
        configuration.setSharedVariable("fundingUri", new FundingUri(uriNormaliser));
        configuration.setSharedVariable("licenceUris", new LicenceUri());
        configuration.setSharedVariable("formatUris", new FormatUri());
        configuration.setSharedVariable("keywordUri", new KeywordUri(uriNormaliser, keywordVocabulary));

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
        @DisplayName("Funding is emitted as a frapo:Grant (dri-one #324)")
        class FrapoGrant {

            private static final String WAS_GENERATED_BY = "http://www.w3.org/ns/prov#wasGeneratedBy";
            private static final String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
            private static final String FRAPO_GRANT = "http://purl.org/cerif/frapo/Grant";
            private static final String FRAPO_FUNDING_AGENCY = "http://purl.org/cerif/frapo/FundingAgency";
            private static final String FRAPO_HAS_GRANT_NUMBER = "http://purl.org/cerif/frapo/hasGrantNumber";
            private static final String FRAPO_AWARDS = "http://purl.org/cerif/frapo/awards";
            private static final String FRAPO_FUNDS = "http://purl.org/cerif/frapo/funds";
            private static final String FOAF_NAME = "http://xmlns.com/foaf/0.1/name";
            private static final String OWL_SAME_AS = "http://www.w3.org/2002/07/owl#sameAs";
            private static final String RDFS_LABEL = "http://www.w3.org/2000/01/rdf-schema#label";

            private GeminiDocument dataset(String id) {
                return (GeminiDocument) new GeminiDocument()
                    .setType("dataset")
                    .setId(id)
                    .setUri("https://example.com/id/" + id)
                    .setTitle("Frapo grant");
            }

            @Test
            @DisplayName("an award number and a ROR funder produce a full frapo:Grant / frapo:FundingAgency pair")
            void grantAndRorFunderAreEmitted() {
                val document = dataset("grantror");
                document.setFunding(List.of(
                    Funding.builder()
                        .funderName("UK Research and Innovation")
                        .funderIdentifier("https://ror.org/00cwqg982")
                        .awardTitle("Test grant")
                        .awardNumber("NE/R016429/1")
                        .awardURI("https://gtr.ukri.org/projects?ref=NE/R016429/1")
                        .build()
                ));

                template("rdf/ttl.ftl", document);

                val record = createResource("https://example.com/id/grantror");
                val grant = model.listObjectsOfProperty(record, createProperty(WAS_GENERATED_BY)).next().asResource();

                assertTrue(model.contains(grant, createProperty(RDF_TYPE), createResource(FRAPO_GRANT)));
                assertTrue(model.contains(grant, createProperty(FRAPO_HAS_GRANT_NUMBER), model.createLiteral("NE/R016429/1")));
                assertTrue(model.contains(grant, createProperty(RDFS_LABEL), model.createLiteral("Test grant")));
                assertTrue(model.contains(grant, createProperty(FRAPO_FUNDS), record));
                assertTrue(model.contains(
                    grant,
                    createProperty(OWL_SAME_AS),
                    createResource("https://gtr.ukri.org/projects?ref=NE/R016429/1")
                ));

                val funder = createResource("https://ror.org/00cwqg982");
                assertTrue(model.contains(funder, createProperty(RDF_TYPE), createResource(FRAPO_FUNDING_AGENCY)));
                assertTrue(model.contains(funder, createProperty(FRAPO_AWARDS), grant));
            }

            @Test
            @DisplayName("a funder identifier gets no foaf:name from the record's own funderName")
            void funderIdentifierGetsNoNameFromRecordText() {
                val document = dataset("fundernoname");
                document.setFunding(List.of(
                    Funding.builder()
                        .funderName("BBSRC")
                        .funderIdentifier("https://ror.org/00cwqg982")
                        .awardNumber("BB/X000001/1")
                        .build()
                ));

                template("rdf/ttl.ftl", document);

                val funder = createResource("https://ror.org/00cwqg982");
                assertTrue(model.contains(funder, createProperty(RDF_TYPE), createResource(FRAPO_FUNDING_AGENCY)));
                assertFalse(model.contains(funder, createProperty(FOAF_NAME), (org.apache.jena.rdf.model.RDFNode) null));
            }

            @Test
            @DisplayName("two records naming one funder differently leave it with a single description")
            void oneFunderIsNotGivenCompetingNames() {
                // The defect this guards: funderIdentifier is an externally-governed shared node,
                // so "Biotechnology and Biological Sciences Research Council" from one record and
                // "BBSRC" from another would both persist on it - the corruption dri-one #320
                // removed from contactDetail and organisationRORs.
                val first = dataset("funderone");
                first.setFunding(List.of(Funding.builder()
                    .funderName("Biotechnology and Biological Sciences Research Council")
                    .funderIdentifier("https://ror.org/00cwqg982")
                    .awardNumber("BB/X000001/1")
                    .build()));
                template("rdf/ttl.ftl", first);
                val afterFirst = model.listStatements(
                    createResource("https://ror.org/00cwqg982"), null, (org.apache.jena.rdf.model.RDFNode) null
                ).toList().size();

                val second = dataset("fundertwo");
                second.setFunding(List.of(Funding.builder()
                    .funderName("BBSRC")
                    .funderIdentifier("https://ror.org/00cwqg982")
                    .awardNumber("BB/X000002/1")
                    .build()));
                template("rdf/ttl.ftl", second);

                // Only the type plus one frapo:awards per grant - never a competing name.
                assertFalse(model.contains(
                    createResource("https://ror.org/00cwqg982"),
                    createProperty(FOAF_NAME),
                    (org.apache.jena.rdf.model.RDFNode) null
                ));
                assertThat(afterFirst, equalTo(2));
            }

            @Test
            @DisplayName("a grant node minted from awardNumber is stable across differing awardURI spellings")
            void grantIdentityIsStableAcrossAwardUriVariants() {
                val document = dataset("grantstable");
                document.setFunding(List.of(
                    Funding.builder()
                        .awardTitle("Grant A")
                        .awardNumber("NE/S008926/1")
                        .awardURI("http://gtr.ukri.org/projects?ref=NE%2FS008926%2F1")
                        .build(),
                    Funding.builder()
                        .awardTitle("Grant A")
                        .awardNumber("NE/S008926/1")
                        .awardURI("https://gtr.ukri.org/projects?ref=NE/S008926/1")
                        .build()
                ));

                template("rdf/ttl.ftl", document);

                assertThat(
                    model.listObjectsOfProperty(createProperty(WAS_GENERATED_BY)).toSet().size(),
                    equalTo(1)
                );
                assertThat(
                    model.listResourcesWithProperty(createProperty(FRAPO_HAS_GRANT_NUMBER)).toList().size(),
                    equalTo(1)
                );
            }

            @Test
            @DisplayName("a funder identifier that is not a ROR is still emitted, also without a name")
            void nonRorFunderIsStillEmitted() {
                val document = dataset("nonror");
                document.setFunding(List.of(
                    Funding.builder()
                        .funderName("Some Other Funder")
                        .funderIdentifier("https://example.org/funder/123")
                        .awardNumber("AB123")
                        .build()
                ));

                template("rdf/ttl.ftl", document);

                val funder = createResource("https://example.org/funder/123");
                assertTrue(model.contains(funder, createProperty(RDF_TYPE), createResource(FRAPO_FUNDING_AGENCY)));
                // No foaf:name here either: a non-ROR funderIdentifier is still an externally-owned
                // identifier, so the same rule applies as for a ROR.
                assertFalse(model.contains(funder, createProperty(FOAF_NAME), (org.apache.jena.rdf.model.RDFNode) null));
                assertTrue(model.contains(
                    funder,
                    createProperty(FRAPO_AWARDS),
                    model.listObjectsOfProperty(
                        createResource("https://example.com/id/nonror"),
                        createProperty(WAS_GENERATED_BY)
                    ).next().asResource()
                ));
            }

            @Test
            @DisplayName("without an awardNumber, awardURI is still the grant's identity (current behaviour)")
            void awardUriIsIdentityWhenNoAwardNumber() {
                val document = dataset("nonum");
                document.setFunding(List.of(
                    Funding.builder()
                        .awardTitle("Grant without a number")
                        .awardURI("https://testaward.ac.uk")
                        .build()
                ));

                template("rdf/ttl.ftl", document);

                val grant = createResource("https://testaward.ac.uk");
                assertTrue(model.contains(grant, createProperty(RDF_TYPE), createResource(FRAPO_GRANT)));
                assertFalse(model.contains(grant, createProperty(FRAPO_HAS_GRANT_NUMBER), (org.apache.jena.rdf.model.RDFNode) null));
            }
        }

        @Nested
        @DisplayName("A person is one node across records (dri-one #319)")
        class PersonIdentity {

            private static final String CREATOR = "http://purl.org/dc/terms/creator";
            private static final String CONTACT_POINT = "http://www.w3.org/ns/dcat#contactPoint";
            private static final String FOAF_NAME = "http://xmlns.com/foaf/0.1/name";

            private GeminiDocument dataset(String id) {
                return (GeminiDocument) new GeminiDocument()
                    .setType("dataset")
                    .setId(id)
                    .setUri("https://example.com/id/" + id)
                    .setTitle("Person identity");
            }

            private ResponsibleParty author(String familyName, String givenName) {
                return ResponsibleParty.builder()
                    .familyName(familyName)
                    .givenName(givenName)
                    .organisationName("UK Centre for Ecology & Hydrology")
                    .build();
            }

            @Test
            @DisplayName("an author on two records is one node, where they used to be two")
            void sharedAcrossRecords() {
                val first = dataset("personone");
                first.setAuthors(List.of(author("Wood", "Claire")));
                val second = dataset("persontwo");
                second.setAuthors(List.of(author("Wood", "Claire")));

                template("rdf/ttl.ftl", first);
                template("rdf/ttl.ftl", second);

                val creators = model.listObjectsOfProperty(createProperty(CREATOR)).toSet();
                assertThat(creators.size(), equalTo(1));
                assertThat(
                    creators.iterator().next().asResource().getURI(),
                    matchesRegex("https://example\\.com/id/person_[0-9a-f]{16}")
                );
            }

            @Test
            @DisplayName("moving an author up the list no longer changes who they are")
            void independentOfPosition() {
                val first = dataset("posone");
                first.setAuthors(List.of(author("Smart", "Simon"), author("Wood", "Claire")));
                val second = dataset("postwo");
                second.setAuthors(List.of(author("Wood", "Claire"), author("Smart", "Simon")));

                template("rdf/ttl.ftl", first);
                template("rdf/ttl.ftl", second);

                assertThat(model.listObjectsOfProperty(createProperty(CREATOR)).toSet().size(), equalTo(2));
            }

            @Test
            @DisplayName("an author who is also the contact point is one node the record links twice")
            void sharedAcrossRolesInOneRecord() {
                val document = dataset("bothroles");
                document.setAuthors(List.of(author("Wood", "Claire")));
                document.setContactPoints(List.of(author("Wood", "Claire")));

                template("rdf/ttl.ftl", document);

                val record = createResource("https://example.com/id/bothroles");
                val person = model.listObjectsOfProperty(record, createProperty(CREATOR)).next().asResource();
                assertTrue(model.contains(record, createProperty(CONTACT_POINT), person));
                assertThat(
                    model.listResourcesWithProperty(
                        createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                        createResource("http://xmlns.com/foaf/0.1/Person")
                    ).toList().size(),
                    equalTo(1)
                );
            }

            @Test
            @DisplayName("the node carries the name it was derived from, so it can be reconciled later")
            void nodeIsSelfDescribing() {
                val document = dataset("selfdesc");
                document.setAuthors(List.of(author("Wood", "Claire")));

                template("rdf/ttl.ftl", document);

                val person = model.listObjectsOfProperty(createProperty(CREATOR)).next().asResource();
                assertTrue(model.contains(person, createProperty(FOAF_NAME), model.createLiteral("Wood, C.")));
                assertTrue(model.contains(
                    person,
                    createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                    createResource("http://xmlns.com/foaf/0.1/Person")
                ));
            }

            @Test
            @DisplayName("an ISNI identifies the author, which isIsni() never did before #319")
            void isni() {
                val document = dataset("isnitest");
                document.setAuthors(List.of(
                    ResponsibleParty.builder()
                        .familyName("Wood")
                        .givenName("Claire")
                        .nameIdentifier("https://isni.org/isni/0000000121032683")
                        .build()
                ));

                template("rdf/ttl.ftl", document);

                assertTrue(model.contains(
                    createResource("https://example.com/id/isnitest"),
                    createProperty(CREATOR),
                    createResource("https://isni.org/isni/0000000121032683")
                ));
            }

            @Test
            @DisplayName("an organisation without a ROR stays on its record-scoped node")
            void organisationWithoutRor() {
                val document = dataset("orgtest");
                document.setContactPoints(List.of(
                    ResponsibleParty.builder().organisationName("Butterfly Conservation").build()
                ));

                template("rdf/ttl.ftl", document);

                assertTrue(model.contains(
                    createResource("https://example.com/id/orgtest"),
                    createProperty(CONTACT_POINT),
                    createResource("https://example.com/id/orgtest_c0")
                ));
            }
        }

        @Nested
        @DisplayName("Record text is not written onto shared authority URIs (dri-one #320)")
        class SharedAuthorityData {

            private static final String SKOS_PREF_LABEL = "http://www.w3.org/2004/02/skos/core#prefLabel";
            private static final String RDFS_LABEL = "http://www.w3.org/2000/01/rdf-schema#label";
            private static final String FOAF_NAME = "http://xmlns.com/foaf/0.1/name";
            private static final String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
            private static final String SKOS_CONCEPT = "http://www.w3.org/2004/02/skos/core#Concept";
            private static final String FOAF_ORGANIZATION = "http://xmlns.com/foaf/0.1/Organization";
            private static final String SDO_VARIABLE_MEASURED = "https://schema.org/variableMeasured";
            private static final String SOSA_OBSERVED_PROPERTY = "http://www.w3.org/ns/sosa/observedProperty";

            private GeminiDocument dataset(String id) {
                return (GeminiDocument) new GeminiDocument()
                    .setType("dataset")
                    .setId(id)
                    .setUri("https://example.com/id/" + id)
                    .setTitle("Shared authority data");
            }

            @Test
            @DisplayName("a keyword with a shared concept URI gets no prefLabel or rdfs:label from record text")
            void keywordConceptGetsNoLabelFromRecordText() {
                val document = dataset("kwlabeltest");
                document.setKeywordsOther(List.of(
                    Keyword.builder().value("Scoland").URI("http://sws.geonames.org/2638360/").build()
                ));

                template("rdf/ttl.ftl", document);

                val concept = createResource("https://sws.geonames.org/2638360");
                assertTrue(model.contains(concept, createProperty(RDF_TYPE), createResource(SKOS_CONCEPT)));
                assertFalse(model.contains(concept, createProperty(SKOS_PREF_LABEL), (org.apache.jena.rdf.model.RDFNode) null));
                assertFalse(model.contains(concept, createProperty(RDFS_LABEL), (org.apache.jena.rdf.model.RDFNode) null));
            }

            @Test
            @DisplayName("a keyword with no URI still renders as a plain literal in the subject list")
            void keywordWithoutUriStillWorks() {
                val document = dataset("kwnouritest");
                document.setKeywordsOther(List.of(
                    Keyword.builder().value("Freeform keyword").build()
                ));

                template("rdf/ttl.ftl", document);

                assertTrue(model.contains(
                    createResource("https://example.com/id/kwnouritest"),
                    createProperty("http://purl.org/dc/terms/subject"),
                    model.createLiteral("Freeform keyword")
                ));
            }

            @Test
            @DisplayName("an observed property with a shared concept URI gets no prefLabel or rdfs:label from record text")
            void observedPropertyConceptGetsNoLabelFromRecordText() {
                val document = dataset("optest");
                document.setFileset(List.of(
                    Fileset.builder()
                        .filesetName("data.csv")
                        .observedProperty(List.of(
                            ObservedProperty.builder()
                                .title("Wrong title")
                                .value("Wrong value")
                                .uri("https://vocab.nerc.ac.uk/collection/P07/current/CFSN0381/")
                                .build()
                        ))
                        .build()
                ));

                template("rdf/ttl.ftl", document);

                val concept = createResource("https://vocab.nerc.ac.uk/collection/P07/current/CFSN0381/");
                assertTrue(model.contains(concept, createProperty(RDF_TYPE), createResource(SKOS_CONCEPT)));
                assertFalse(model.contains(concept, createProperty(SKOS_PREF_LABEL), (org.apache.jena.rdf.model.RDFNode) null));
                assertFalse(model.contains(concept, createProperty(RDFS_LABEL), (org.apache.jena.rdf.model.RDFNode) null));
            }

            @Test
            @DisplayName("an observed property with a uri also gets a sosa:observedProperty triple alongside sdo:variableMeasured (dri-one #326)")
            void observedPropertyWithUriAlsoGetsSosaObservedProperty() {
                val document = dataset("opsosatest");
                document.setFileset(List.of(
                    Fileset.builder()
                        .filesetName("data.csv")
                        .observedProperty(List.of(
                            ObservedProperty.builder()
                                .title("Wrong title")
                                .value("Wrong value")
                                .uri("https://vocab.nerc.ac.uk/collection/P07/current/CFSN0381/")
                                .build()
                        ))
                        .build()
                ));

                template("rdf/ttl.ftl", document);

                val subject = createResource("https://example.com/id/opsosatest");
                val concept = createResource("https://vocab.nerc.ac.uk/collection/P07/current/CFSN0381/");
                assertTrue(model.contains(subject, createProperty(SDO_VARIABLE_MEASURED), concept));
                assertTrue(model.contains(subject, createProperty(SOSA_OBSERVED_PROPERTY), concept));
            }

            @Test
            @DisplayName("an observed property without a uri gets only the unchanged sdo:variableMeasured literal, no sosa:observedProperty (dri-one #326)")
            void observedPropertyWithoutUriGetsNoSosaObservedProperty() {
                val document = dataset("opnourisosatest");
                document.setFileset(List.of(
                    Fileset.builder()
                        .filesetName("data.csv")
                        .observedProperty(List.of(
                            ObservedProperty.builder()
                                .title("Free text property")
                                .build()
                        ))
                        .build()
                ));

                template("rdf/ttl.ftl", document);

                val subject = createResource("https://example.com/id/opnourisosatest");
                assertTrue(model.contains(
                    subject,
                    createProperty(SDO_VARIABLE_MEASURED),
                    model.createLiteral("Free text property")
                ));
                assertFalse(model.contains(
                    subject,
                    createProperty(SOSA_OBSERVED_PROPERTY),
                    (org.apache.jena.rdf.model.RDFNode) null
                ));
            }

            @Test
            @DisplayName("an organisation-only contact identified by a ROR gets no foaf:name from record text")
            void organisationOnlyContactOnRorGetsNoNameFromRecordText() {
                val document = dataset("orgrortest");
                document.setContactPoints(List.of(
                    ResponsibleParty.builder()
                        .organisationName("University of the West of England")
                        .organisationIdentifier("https://ror.org/00pggkr55")
                        .build()
                ));

                template("rdf/ttl.ftl", document);

                val ror = createResource("https://ror.org/00pggkr55");
                assertTrue(model.contains(ror, createProperty(RDF_TYPE), createResource(FOAF_ORGANIZATION)));
                assertFalse(model.contains(ror, createProperty(FOAF_NAME), (org.apache.jena.rdf.model.RDFNode) null));
                assertTrue(model.contains(
                    createResource("https://example.com/id/orgrortest"),
                    createProperty("http://www.w3.org/ns/dcat#contactPoint"),
                    ror
                ));
            }

            @Test
            @DisplayName("organisationRORs never writes a person's typed affiliation onto the shared ROR node")
            void organisationRorsGetsNoNameFromRecordText() {
                val document = dataset("rorafftest");
                document.setAuthors(List.of(
                    ResponsibleParty.builder()
                        .familyName("Wood")
                        .givenName("Claire")
                        .organisationName("University of the West of England")
                        .organisationIdentifier("https://ror.org/00pggkr55")
                        .build()
                ));

                template("rdf/ttl.ftl", document);

                val ror = createResource("https://ror.org/00pggkr55");
                assertTrue(model.contains(ror, createProperty(RDF_TYPE), createResource(FOAF_ORGANIZATION)));
                assertFalse(model.contains(ror, createProperty(FOAF_NAME), (org.apache.jena.rdf.model.RDFNode) null));
            }

            @Test
            @DisplayName("organisationRORs stays parseable and name-free even with whitespace and an embedded quote")
            void organisationRorsHandlesMessyTextSafely() {
                val document = dataset("rormessytest");
                document.setAuthors(List.of(
                    ResponsibleParty.builder()
                        .familyName("Wood")
                        .givenName("Claire")
                        .organisationName("  \"UK Centre for Ecology & Hydrology\"  ")
                        .organisationIdentifier("https://ror.org/00pggkr55")
                        .build()
                ));

                template("rdf/ttl.ftl", document);

                val ror = createResource("https://ror.org/00pggkr55");
                assertTrue(model.contains(ror, createProperty(RDF_TYPE), createResource(FOAF_ORGANIZATION)));
                assertFalse(model.contains(ror, createProperty(FOAF_NAME), (org.apache.jena.rdf.model.RDFNode) null));
                assertThat(
                    model.listStatements(ror, null, (org.apache.jena.rdf.model.RDFNode) null).toList().size(),
                    equalTo(1)
                );
            }
        }

        @Nested
        @DisplayName("contributorRole/role reach RDF via pro:RoleInTime (dri-one #323)")
        class ContributorRoleModel {

            private static final String HOLDS_ROLE_IN_TIME = "http://purl.org/spar/pro/holdsRoleInTime";
            private static final String WITH_ROLE = "http://purl.org/spar/pro/withRole";
            private static final String RELATES_TO_ENTITY = "http://purl.org/spar/pro/relatesToEntity";
            private static final String ROLE_IN_TIME_TYPE = "http://purl.org/spar/pro/RoleInTime";
            private static final String CONTACT_POINT = "http://www.w3.org/ns/dcat#contactPoint";
            private static final String CREATOR = "http://purl.org/dc/terms/creator";

            private GeminiDocument dataset(String id) {
                return (GeminiDocument) new GeminiDocument()
                    .setType("dataset")
                    .setId(id)
                    .setUri("https://example.com/id/" + id)
                    .setTitle("Contributor role");
            }

            @Test
            @DisplayName("a contributorRole of dataCurator produces a pro:RoleInTime node with scoro:data-curator")
            void contributorRoleDataCurator() {
                val document = dataset("roletest");
                document.setContactPoints(List.of(
                    ResponsibleParty.builder()
                        .familyName("Wood")
                        .givenName("Claire")
                        .contributorRole("dataCurator")
                        .build()
                ));

                template("rdf/ttl.ftl", document);

                val record = createResource("https://example.com/id/roletest");
                val person = model.listObjectsOfProperty(record, createProperty(CONTACT_POINT)).next().asResource();

                assertTrue(model.contains(person, createProperty(HOLDS_ROLE_IN_TIME)));
                val roleInTime = model.listObjectsOfProperty(person, createProperty(HOLDS_ROLE_IN_TIME))
                    .next().asResource();
                assertTrue(model.contains(
                    roleInTime,
                    createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                    createResource(ROLE_IN_TIME_TYPE)
                ));
                assertTrue(model.contains(
                    roleInTime,
                    createProperty(WITH_ROLE),
                    createResource("http://purl.org/spar/scoro/data-curator")
                ));
                assertTrue(model.contains(roleInTime, createProperty(RELATES_TO_ENTITY), record));
            }

            @Test
            @DisplayName("a role of author produces a pro:RoleInTime node with pro:author")
            void roleAuthor() {
                val document = dataset("roleauthor");
                document.setAuthors(List.of(
                    ResponsibleParty.builder()
                        .familyName("Smith")
                        .givenName("John")
                        .role("author")
                        .build()
                ));

                template("rdf/ttl.ftl", document);

                val record = createResource("https://example.com/id/roleauthor");
                val person = model.listObjectsOfProperty(record, createProperty(CREATOR)).next().asResource();

                val roleInTime = model.listObjectsOfProperty(person, createProperty(HOLDS_ROLE_IN_TIME))
                    .next().asResource();
                assertTrue(model.contains(
                    roleInTime,
                    createProperty(WITH_ROLE),
                    createResource("http://purl.org/spar/pro/author")
                ));
                assertTrue(model.contains(roleInTime, createProperty(RELATES_TO_ENTITY), record));
            }

            @Test
            @DisplayName("no pro:holdsRoleInTime triple when contributorRole and role are both blank")
            void noRoleTripleWhenBlank() {
                val document = dataset("roleblank");
                document.setContactPoints(List.of(
                    ResponsibleParty.builder()
                        .familyName("Wood")
                        .givenName("Claire")
                        .build()
                ));

                template("rdf/ttl.ftl", document);

                assertFalse(model.contains(null, createProperty(HOLDS_ROLE_IN_TIME)));
            }
        }

        @Nested
        @DisplayName("Dangling references to unpublished records are not linked (dri-one #327)")
        class DanglingReferences {

            private static final String IS_PART_OF = "http://purl.org/dc/terms/isPartOf";
            private static final String REPLACES = "http://purl.org/dc/terms/replaces";
            private static final String RELATION = "http://purl.org/dc/terms/relation";
            private static final String UTILISES = "https://digital.ceh.ac.uk/ontology/doo/utilises";

            private GeminiDocument dataset(String id) {
                return (GeminiDocument) new GeminiDocument()
                    .setType("dataset")
                    .setId(id)
                    .setUri("https://example.com/id/" + id)
                    .setTitle("Dangling references");
            }

            @Test
            @DisplayName("doo:utilises: a withdrawn target is not linked, a published one still is")
            void utilisesFiltersWithdrawnTargets() {
                // doo:utilises (dri-one #325) was added to _body.ftl after the availability filter
                // (dri-one #327) and did not pick it up, so it was the one relationship predicate
                // that could still point at a withdrawn record.
                val document = dataset("dangleutilises");
                given(jenaLookupService.relationships(document.getUri(), IS_PART_OF)).willReturn(List.of());
                given(jenaLookupService.relationships(document.getUri(), REPLACES)).willReturn(List.of());
                given(jenaLookupService.relationships(document.getUri(), RELATION)).willReturn(List.of());
                given(jenaLookupService.relationships(document.getUri(), UTILISES)).willReturn(List.of(
                    Link.builder().href("https://example.com/id/facilityLive").availability("Available").build(),
                    Link.builder().href("https://example.com/id/facilityGone").availability("Deleted").build()
                ));

                template("rdf/ttl.ftl", document);

                assertTrue(model.contains(
                    createResource(document.getUri()),
                    createProperty(UTILISES),
                    createResource("https://example.com/id/facilityLive")
                ));
                assertFalse(model.contains(
                    createResource(document.getUri()),
                    createProperty(UTILISES),
                    createResource("https://example.com/id/facilityGone")
                ));
            }

            @Test
            @DisplayName("doo:utilises: a facility with an operational status, not an availability, is still linked")
            void utilisesKeepsFacilityTargets() {
                // A monitoring facility's ?availability binds from doo:operationalStatus
                // ("Operational"/"Closed"), never "Deleted", so the filter must be a no-op for the
                // usual case rather than dropping every facility link.
                val document = dataset("dangleutilisesfacility");
                given(jenaLookupService.relationships(document.getUri(), IS_PART_OF)).willReturn(List.of());
                given(jenaLookupService.relationships(document.getUri(), REPLACES)).willReturn(List.of());
                given(jenaLookupService.relationships(document.getUri(), RELATION)).willReturn(List.of());
                given(jenaLookupService.relationships(document.getUri(), UTILISES)).willReturn(List.of(
                    Link.builder().href("https://example.com/id/cosmosMorley").availability("Operational").build(),
                    Link.builder().href("https://example.com/id/cosmosClosed").availability("Closed").build()
                ));

                template("rdf/ttl.ftl", document);

                assertTrue(model.contains(
                    createResource(document.getUri()),
                    createProperty(UTILISES),
                    createResource("https://example.com/id/cosmosMorley")
                ));
                assertTrue(model.contains(
                    createResource(document.getUri()),
                    createProperty(UTILISES),
                    createResource("https://example.com/id/cosmosClosed")
                ));
            }

            @Test
            @DisplayName("isPartOf: a withdrawn target is not linked, a published one still is")
            void isPartOfFiltersWithdrawnTargets() {
                val document = dataset("danglepartof");
                given(jenaLookupService.relationships(document.getUri(), IS_PART_OF)).willReturn(List.of(
                    Link.builder().href("https://example.com/id/published").availability("Available").build(),
                    Link.builder().href("https://example.com/id/withdrawn").availability("Deleted").build()
                ));
                given(jenaLookupService.relationships(document.getUri(), REPLACES)).willReturn(List.of());
                given(jenaLookupService.relationships(document.getUri(), RELATION)).willReturn(List.of());

                template("rdf/ttl.ftl", document);

                assertTrue(model.contains(
                    createResource(document.getUri()),
                    createProperty(IS_PART_OF),
                    createResource("https://example.com/id/published")
                ));
                assertFalse(model.contains(
                    createResource(document.getUri()),
                    createProperty(IS_PART_OF),
                    createResource("https://example.com/id/withdrawn")
                ));
            }

            @Test
            @DisplayName("replaces: a withdrawn target is not linked")
            void replacesFiltersWithdrawnTargets() {
                val document = dataset("danglereplaces");
                given(jenaLookupService.relationships(document.getUri(), IS_PART_OF)).willReturn(List.of());
                given(jenaLookupService.relationships(document.getUri(), REPLACES)).willReturn(List.of(
                    Link.builder().href("https://example.com/id/replacesPublished").availability("Available").build(),
                    Link.builder().href("https://example.com/id/replacesWithdrawn").availability("Deleted").build()
                ));
                given(jenaLookupService.relationships(document.getUri(), RELATION)).willReturn(List.of());

                template("rdf/ttl.ftl", document);

                assertTrue(model.contains(
                    createResource(document.getUri()),
                    createProperty(REPLACES),
                    createResource("https://example.com/id/replacesPublished")
                ));
                assertFalse(model.contains(
                    createResource(document.getUri()),
                    createProperty(REPLACES),
                    createResource("https://example.com/id/replacesWithdrawn")
                ));
            }

            @Test
            @DisplayName("relation: a withdrawn target is not linked")
            void relationFiltersWithdrawnTargets() {
                val document = dataset("danglerelation");
                given(jenaLookupService.relationships(document.getUri(), IS_PART_OF)).willReturn(List.of());
                given(jenaLookupService.relationships(document.getUri(), REPLACES)).willReturn(List.of());
                given(jenaLookupService.relationships(document.getUri(), RELATION)).willReturn(List.of(
                    Link.builder().href("https://example.com/id/relationPublished").availability("Available").build(),
                    Link.builder().href("https://example.com/id/relationWithdrawn").availability("Deleted").build()
                ));

                template("rdf/ttl.ftl", document);

                assertTrue(model.contains(
                    createResource(document.getUri()),
                    createProperty(RELATION),
                    createResource("https://example.com/id/relationPublished")
                ));
                assertFalse(model.contains(
                    createResource(document.getUri()),
                    createProperty(RELATION),
                    createResource("https://example.com/id/relationWithdrawn")
                ));
            }

            @Test
            @DisplayName("a target with no availability recorded is still linked (unknown is not the same as withdrawn)")
            void targetWithNoAvailabilityIsStillLinked() {
                val document = dataset("dangleunknown");
                given(jenaLookupService.relationships(document.getUri(), IS_PART_OF)).willReturn(List.of(
                    Link.builder().href("https://example.com/id/noAvailabilityRecorded").build()
                ));
                given(jenaLookupService.relationships(document.getUri(), REPLACES)).willReturn(List.of());
                given(jenaLookupService.relationships(document.getUri(), RELATION)).willReturn(List.of());

                template("rdf/ttl.ftl", document);

                assertTrue(model.contains(
                    createResource(document.getUri()),
                    createProperty(IS_PART_OF),
                    createResource("https://example.com/id/noAvailabilityRecorded")
                ));
            }
        }

        @Nested
        @DisplayName("A bare keyword literal is promoted to a concept already in the vocabularies (dri-one #321)")
        class LiteralSubjectPromotion {

            private static final String SUBJECT = "http://purl.org/dc/terms/subject";
            private static final String THEME = "http://purl.org/dc/terms/theme";
            private static final String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
            private static final String SKOS_CONCEPT = "http://www.w3.org/2004/02/skos/core#Concept";
            private static final String GEMET_SOIL_MOISTURE = "https://www.eionet.europa.eu/gemet/concept/7842";

            private GeminiDocument dataset(String id) {
                return (GeminiDocument) new GeminiDocument()
                    .setType("dataset")
                    .setId(id)
                    .setUri("https://example.com/id/" + id)
                    .setTitle("Literal subject promotion");
            }

            private uk.ac.ceh.gateway.catalogue.vocabularies.Keyword concept(String label, String url) {
                return new uk.ac.ceh.gateway.catalogue.vocabularies.Keyword(label, "GEMET", url);
            }

            @Test
            @SneakyThrows
            @DisplayName("a keyword with no URI whose label matches one known concept becomes that concept")
            void unambiguousLiteralBecomesAConceptIri() {
                given(keywordVocabulary.resolveExactLabel("Soil moisture"))
                    .willReturn(Optional.of(concept("Soil moisture", GEMET_SOIL_MOISTURE)));

                val document = dataset("kwpromote");
                document.setKeywordsOther(List.of(Keyword.builder().value("Soil moisture").build()));

                template("rdf/ttl.ftl", document);

                assertTrue(model.contains(
                    createResource("https://example.com/id/kwpromote"),
                    createProperty(SUBJECT),
                    createResource(GEMET_SOIL_MOISTURE)
                ));
                assertFalse(model.contains(
                    createResource("https://example.com/id/kwpromote"),
                    createProperty(SUBJECT),
                    model.createLiteral("Soil moisture")
                ));
            }

            @Test
            @SneakyThrows
            @DisplayName("a promoted keyword is typed as a concept, exactly as one carrying its own URI is")
            void promotedKeywordIsTypedAsAConcept() {
                given(keywordVocabulary.resolveExactLabel("Soil moisture"))
                    .willReturn(Optional.of(concept("Soil moisture", GEMET_SOIL_MOISTURE)));

                val document = dataset("kwpromotetype");
                document.setKeywordsOther(List.of(Keyword.builder().value("Soil moisture").build()));

                template("rdf/ttl.ftl", document);

                assertTrue(model.contains(
                    createResource(GEMET_SOIL_MOISTURE),
                    createProperty(RDF_TYPE),
                    createResource(SKOS_CONCEPT)
                ));
            }

            @Test
            @SneakyThrows
            @DisplayName("dcterms:theme is promoted too, and agrees with the dcterms:subject node")
            void themeIsPromotedAsWell() {
                given(keywordVocabulary.resolveExactLabel("Soil moisture"))
                    .willReturn(Optional.of(concept("Soil moisture", GEMET_SOIL_MOISTURE)));

                val document = dataset("kwtheme");
                document.setKeywordsTheme(List.of(Keyword.builder().value("Soil moisture").build()));

                template("rdf/ttl.ftl", document);

                val record = createResource("https://example.com/id/kwtheme");
                assertTrue(model.contains(record, createProperty(THEME), createResource(GEMET_SOIL_MOISTURE)));
                assertTrue(model.contains(record, createProperty(SUBJECT), createResource(GEMET_SOIL_MOISTURE)));
            }

            @Test
            @SneakyThrows
            @DisplayName("a keyword matching no known concept stays a literal")
            void unmatchedLiteralStaysALiteral() {
                given(keywordVocabulary.resolveExactLabel("Freeform keyword")).willReturn(Optional.empty());

                val document = dataset("kwnomatch");
                document.setKeywordsOther(List.of(Keyword.builder().value("Freeform keyword").build()));

                template("rdf/ttl.ftl", document);

                assertTrue(model.contains(
                    createResource("https://example.com/id/kwnomatch"),
                    createProperty(SUBJECT),
                    model.createLiteral("Freeform keyword")
                ));
            }

            @Test
            @SneakyThrows
            @DisplayName("an ambiguous keyword stays a literal rather than being guessed at")
            void ambiguousLiteralStaysALiteral() {
                // The service reports ambiguity as "no single concept"; see
                // KeywordVocabularySolrQueryServiceTest for how two candidates produce that.
                given(keywordVocabulary.resolveExactLabel("Soil moisture")).willReturn(Optional.empty());

                val document = dataset("kwambiguous");
                document.setKeywordsOther(List.of(Keyword.builder().value("Soil moisture").build()));

                template("rdf/ttl.ftl", document);

                assertTrue(model.contains(
                    createResource("https://example.com/id/kwambiguous"),
                    createProperty(SUBJECT),
                    model.createLiteral("Soil moisture")
                ));
            }

            @Test
            @SneakyThrows
            @DisplayName("a keyword that carries its own URI is not looked up at all")
            void keywordWithItsOwnUriIsNotLookedUp() {
                val document = dataset("kwownuri");
                document.setKeywordsOther(List.of(
                    Keyword.builder().value("Scotland").URI("http://sws.geonames.org/2638360/").build()
                ));

                template("rdf/ttl.ftl", document);

                assertTrue(model.containsResource(createResource("https://sws.geonames.org/2638360")));
                verifyNoInteractions(keywordVocabulary);
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

            private void assertLicenceCanonicalises(String id, String suppliedLicenceUri, String expectedSpdxUri) {
                val uri = "https://example.com/id/" + id;
                val document = new GeminiDocument()
                    .setType("dataset")
                    .setUseConstraints(List.of(
                        ResourceConstraint.builder().code("license").uri(suppliedLicenceUri).build()
                    ))
                    .setUri(uri)
                    .setId(id)
                    .setTitle("Test");

                template("rdf/ttl.ftl", document);

                assertTrue(
                    model.contains(
                        createStatement(
                            createResource(uri),
                            createProperty("http://purl.org/dc/terms/license"),
                            createResource(expectedSpdxUri)
                        )
                    ),
                    () -> "expected " + suppliedLicenceUri + " to canonicalise to " + expectedSpdxUri
                );
            }

            @Test
            @DisplayName("three non-SPDX spellings of the Open Government Licence converge on the SPDX URI (dri-one #327)")
            void openGovernmentLicenceSpellingsConverge() {
                val ogl = "https://spdx.org/licenses/OGL-UK-3.0.ttl";
                assertLicenceCanonicalises("oglspell1", "https://eidc.ac.uk/licences/OGL/plain", ogl);
                assertLicenceCanonicalises("oglspell2", "https://nationalarchives.gov.uk/doc/open-government-licence/", ogl);
                assertLicenceCanonicalises(
                    "oglspell3", "https://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/", ogl
                );
            }

            @Test
            @DisplayName("four spellings of Creative Commons Attribution converge on the SPDX URI (dri-one #327)")
            void creativeCommonsAttributionSpellingsConverge() {
                val ccBy = "https://spdx.org/licenses/CC-BY-4.0.ttl";
                assertLicenceCanonicalises("ccby1", "https://creativecommons.org/licenses/by/4.0/", ccBy);
                assertLicenceCanonicalises("ccby2", "https://creativecommons.org/licenses/by/4.0/deed.en", ccBy);
                assertLicenceCanonicalises("ccby3", "http://creativecommons.org/licenses/by-nd/4.0", ccBy.replace("BY", "BY-ND"));
                assertLicenceCanonicalises("ccby4", "https://creativecommons.org/licenses/by-nc/4.0", ccBy.replace("BY", "BY-NC"));
            }

            @Test
            @DisplayName("a catalogue-local licence is one node whichever eidc host it was entered under (dri-one #327)")
            void eidcHostSplitLicenceConverges() {
                val uriA = "https://example.com/id/hostsplitA";
                val documentA = new GeminiDocument()
                    .setType("dataset")
                    .setUseConstraints(List.of(
                        ResourceConstraint.builder().code("license").uri("https://eidc.ac.uk/licences/ecn/plain").build()
                    ))
                    .setUri(uriA).setId("hostsplitA").setTitle("Test A");

                val uriB = "https://example.com/id/hostsplitB";
                val documentB = new GeminiDocument()
                    .setType("dataset")
                    .setUseConstraints(List.of(
                        ResourceConstraint.builder().code("license").uri("https://eidc.ceh.ac.uk/licences/ecn/plain").build()
                    ))
                    .setUri(uriB).setId("hostsplitB").setTitle("Test B");

                template("rdf/ttl.ftl", documentA);
                template("rdf/ttl.ftl", documentB);

                val licenceA = model.listObjectsOfProperty(
                    createResource(uriA), createProperty("http://purl.org/dc/terms/license")
                ).next();
                val licenceB = model.listObjectsOfProperty(
                    createResource(uriB), createProperty("http://purl.org/dc/terms/license")
                ).next();
                assertThat(licenceA, equalTo(licenceB));
                assertThat(licenceA.toString(), equalTo("https://eidc.ac.uk/licences/ecn/plain"));
            }

            @Test
            @DisplayName("an aggregate emits no minted licence node, having no rights block to reference it")
            void aggregateGetsNoOrphanLicenceNode() {
                // rightsDetail was called unconditionally, but turtle/_aggregation.ftl includes no
                // rights block - so a collection with a free-text licence gained a described
                // :licence_<hash> node that nothing pointed at.
                val uri = "https://example.com/id/orphanlicence";
                val document = new GeminiDocument()
                    .setType("collection")
                    .setUseConstraints(List.of(
                        ResourceConstraint.builder().code("license").value("Bespoke click-through licence text").build()
                    ))
                    .setUri(uri)
                    .setId("orphanlicence")
                    .setTitle("Aggregate with a licence");

                template("rdf/ttl.ftl", document);

                assertFalse(
                    model.contains(
                        null,
                        createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                        createResource("http://purl.org/dc/terms/LicenseDocument")
                    ),
                    "a collection has no rights block, so it must not describe a licence node"
                );
            }

            @Test
            @DisplayName("a dataset still emits its minted licence node (regression on the guard above)")
            void datasetStillGetsItsLicenceNode() {
                val uri = "https://example.com/id/keptlicence";
                val document = new GeminiDocument()
                    .setType("dataset")
                    .setUseConstraints(List.of(
                        ResourceConstraint.builder().code("license").value("Bespoke click-through licence text").build()
                    ))
                    .setUri(uri)
                    .setId("keptlicence")
                    .setTitle("Dataset with a licence");

                template("rdf/ttl.ftl", document);

                assertTrue(model.contains(
                    null,
                    createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                    createResource("http://purl.org/dc/terms/LicenseDocument")
                ));
            }

            @Test
            @DisplayName("a licence with only free text is minted a stable node, not a blank node (dri-one #327)")
            void freeTextLicenceIsMintedNotBlank() {
                val uri = "https://example.com/id/mintlicence";
                val document = new GeminiDocument()
                    .setType("dataset")
                    .setUseConstraints(List.of(
                        ResourceConstraint.builder().code("license").value("Bespoke click-through licence text").build()
                    ))
                    .setUri(uri)
                    .setId("mintlicence")
                    .setTitle("Test");

                template("rdf/ttl.ftl", document);

                val licenceObjects = model.listObjectsOfProperty(
                    createResource(uri), createProperty("http://purl.org/dc/terms/license")
                ).toList();
                assertThat(licenceObjects.size(), equalTo(1));
                assertTrue(licenceObjects.get(0).isURIResource(), "expected a named node, not a blank node");
                assertTrue(model.contains(
                    licenceObjects.get(0).asResource(),
                    createProperty("http://www.w3.org/2000/01/rdf-schema#label"),
                    model.createLiteral("Bespoke click-through licence text")
                ));
            }

            @Test
            @DisplayName("the same free-text licence value mints the same node on two different records (dri-one #327)")
            void freeTextLicenceIsDeterministic() {
                val text = "Bespoke click-through licence text";

                val uriA = "https://example.com/id/mintdeta";
                val documentA = new GeminiDocument()
                    .setType("dataset")
                    .setUseConstraints(List.of(ResourceConstraint.builder().code("license").value(text).build()))
                    .setUri(uriA).setId("mintdeta").setTitle("Test A");

                val uriB = "https://example.com/id/mintdetb";
                val documentB = new GeminiDocument()
                    .setType("dataset")
                    .setUseConstraints(List.of(ResourceConstraint.builder().code("license").value(text).build()))
                    .setUri(uriB).setId("mintdetb").setTitle("Test B");

                template("rdf/ttl.ftl", documentA);
                template("rdf/ttl.ftl", documentB);

                val licenceA = model.listObjectsOfProperty(
                    createResource(uriA), createProperty("http://purl.org/dc/terms/license")
                ).next();
                val licenceB = model.listObjectsOfProperty(
                    createResource(uriB), createProperty("http://purl.org/dc/terms/license")
                ).next();
                assertThat(licenceA, equalTo(licenceB));
            }

            @Test
            @DisplayName("an accessRights statement with only free text is minted a stable node, not a blank node (dri-one #327)")
            void freeTextAccessRightsIsMintedNotBlank() {
                val uri = "https://example.com/id/mintaccess";
                val document = (GeminiDocument) new GeminiDocument()
                    .setType("dataset")
                    .setUri(uri)
                    .setId("mintaccess")
                    .setTitle("Test");
                document.setAccessLimitation(
                    AccessLimitation.builder().value("No limitations to public access").build()
                );

                template("rdf/ttl.ftl", document);

                val accessObjects = model.listObjectsOfProperty(
                    createResource(uri), createProperty("http://purl.org/dc/terms/accessRights")
                ).toList();
                assertThat(accessObjects.size(), equalTo(1));
                assertTrue(accessObjects.get(0).isURIResource(), "expected a named node, not a blank node");
                assertTrue(model.contains(
                    accessObjects.get(0).asResource(),
                    createProperty("http://www.w3.org/2000/01/rdf-schema#label"),
                    model.createLiteral("No limitations to public access")
                ));
            }
        }

        @Nested
        @DisplayName("Blank node identity (dri-one #334)")
        class BlankNodeIdentity {

            private static final String FORMAT = "http://purl.org/dc/terms/format";
            private static final String RIGHTS = "http://purl.org/dc/terms/rights";
            private static final String MEMBER = "http://xmlns.com/foaf/0.1/member";
            private static final String HOLDS_ROLE = "http://purl.org/spar/pro/holdsRoleInTime";
            private static final String COPYRIGHT_NOTICE = "http://schema.theodi.org/odrs#copyrightNotice";
            private static final String FOAF_NAME = "http://xmlns.com/foaf/0.1/name";
            private static final String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
            private static final String RDFS_LABEL = "http://www.w3.org/2000/01/rdf-schema#label";
            private static final String IMT = "http://purl.org/dc/terms/IMT";
            private static final String CSV_IANA = "https://www.iana.org/assignments/media-types/text/csv";

            /**
             * A dataset with one download, so the dcat:distribution block — and
             * with it dcterms:format — actually renders.
             */
            private GeminiDocument dataset(String id) {
                return (GeminiDocument) new GeminiDocument()
                    .setType("dataset")
                    .setOnlineResources(List.of(
                        OnlineResource.builder()
                            .url("https://data-package.ceh.ac.uk/data/" + id)
                            .function("download")
                            .build()
                    ))
                    .setId(id)
                    .setUri("https://example.com/id/" + id)
                    .setTitle("Blank node identity");
            }

            private GeminiDocument withFormat(String id, String name, String type) {
                val document = dataset(id);
                document.setDistributionFormats(List.of(
                    DistributionInfo.builder().name(name).type(type).version("unknown").build()
                ));
                return document;
            }

            private GeminiDocument withCopyright(String id, String notice) {
                val document = dataset(id);
                document.setUseConstraints(List.of(
                    ResourceConstraint.builder().code("copyright").value(notice).build()
                ));
                return document;
            }

            private GeminiDocument withAffiliation(
                String id, String familyName, String givenName, String organisation, String ror
            ) {
                val document = dataset(id);
                document.setAuthors(List.of(
                    ResponsibleParty.builder()
                        .familyName(familyName)
                        .givenName(givenName)
                        .organisationName(organisation)
                        .organisationIdentifier(ror)
                        .role("author")
                        .build()
                ));
                return document;
            }

            private List<RDFNode> objectsOf(String subjectUri, String property) {
                return model.listObjectsOfProperty(
                    createResource(subjectUri), createProperty(property)
                ).toList();
            }

            /** @return every object of {@code property} anywhere in the model. */
            private List<RDFNode> allObjectsOf(String property) {
                return model.listObjectsOfProperty(createProperty(property)).toList();
            }

            @Test
            @DisplayName("no site fixed by #334 emits a blank node any more")
            void noneOfTheFixedSitesIsBlank() {
                val document = withFormat("nobnodes", "Comma-separated values (CSV)", "text/csv");
                document.setUseConstraints(List.of(
                    ResourceConstraint.builder().code("copyright").value("© UKCEH 2026").build()
                ));
                document.setAuthors(List.of(
                    ResponsibleParty.builder()
                        .familyName("Wood")
                        .givenName("Claire")
                        .organisationName("University of Exeter")
                        .role("author")
                        .build()
                ));

                template("rdf/ttl.ftl", document);

                for (val property : List.of(FORMAT, RIGHTS, MEMBER, HOLDS_ROLE)) {
                    val objects = allObjectsOf(property);
                    assertFalse(objects.isEmpty(), () -> property + " emitted nothing to assert about");
                    objects.forEach(object -> assertFalse(
                        object.isAnon(), () -> property + " still points at a blank node: " + object
                    ));
                }
            }

            @Test
            @DisplayName("dcat:distribution is deliberately still a blank node")
            void distributionRemainsBlank() {
                template("rdf/ttl.ftl", withFormat("distblank", "Comma-separated values (CSV)", "text/csv"));

                val distributions = allObjectsOf("http://www.w3.org/ns/dcat#distribution");
                assertThat(distributions.size(), equalTo(1));
                assertTrue(
                    distributions.getFirst().isAnon(),
                    "#334 left dcat:distribution out of scope: naming it means deciding whether a "
                        + "record's several download URLs are one distribution or several, which the "
                        + "issue did not settle. Its 1.001:1 ratio means there is nothing to "
                        + "deduplicate either way, and a blank node still traverses fine — "
                        + "?d dcat:distribution/dcterms:format ?f reaches the format regardless."
                );
            }

            @Test
            @DisplayName("a format with a media type is the IANA registration, shared across records")
            void mediaTypeBecomesTheIanaNode() {
                template("rdf/ttl.ftl", withFormat("fmtA", "Comma-separated values (CSV)", "text/csv"));
                template("rdf/ttl.ftl", withFormat("fmtB", "CSV", "text/csv"));

                assertThat(
                    "two records naming the same media type differently converge on one node",
                    allObjectsOf(FORMAT).stream().distinct().toList(),
                    equalTo(List.<RDFNode>of(createResource(CSV_IANA)))
                );
                assertTrue(model.contains(
                    createResource(CSV_IANA), createProperty(RDF_TYPE), createResource(IMT)
                ));
            }

            @Test
            @DisplayName("the IANA node carries no label taken from record text (dri-one #320)")
            void ianaNodeGetsNoRecordText() {
                template("rdf/ttl.ftl", withFormat("fmtnolabel", "Comma-separated values (CSV)", "text/csv"));

                val ianaNode = createResource(CSV_IANA);
                assertThat(
                    "the media type is externally governed, so only its type is ours to assert",
                    model.listStatements(ianaNode, null, (RDFNode) null).toList().size(), equalTo(1)
                );
            }

            @Test
            @DisplayName("a format with no media type is minted from its name, and keeps its label")
            void formatWithoutMediaTypeIsMinted() {
                template("rdf/ttl.ftl", withFormat("fmtmint", "Shapefile", ""));

                val format = allObjectsOf(FORMAT).getFirst();
                assertFalse(format.isAnon(), "a format with no media type should still be identified");
                assertThat(format.asResource().getURI(), matchesRegex("https://example\\.com/id/format_[0-9a-f]{16}"));
                assertTrue(model.contains(
                    format.asResource(), createProperty(RDFS_LABEL), model.createLiteral("Shapefile")
                ));
            }

            @Test
            @DisplayName("the same format name spelled in two cases is one node")
            void formatNameCaseFolds() {
                template("rdf/ttl.ftl", withFormat("fmtcaseA", "GeoJSON", ""));
                template("rdf/ttl.ftl", withFormat("fmtcaseB", "geojson", ""));

                assertThat(
                    allObjectsOf(FORMAT).stream().distinct().toList().size(), equalTo(1)
                );
            }

            @Test
            @DisplayName("a format with neither name nor media type emits no dcterms:format at all")
            void emptyFormatIsSuppressed() {
                template("rdf/ttl.ftl", withFormat("fmtempty", "", ""));

                assertTrue(
                    allObjectsOf(FORMAT).isEmpty(),
                    "an unidentifiable format should be suppressed, not given an empty node"
                );
            }

            @Test
            @DisplayName("something that is not a media type does not become an IANA URI")
            void malformedMediaTypeFallsBackToMinting() {
                template("rdf/ttl.ftl", withFormat("fmtbadtype", "Shapefile", "not a media type"));

                val format = allObjectsOf(FORMAT).getFirst();
                assertThat(format.asResource().getURI(), matchesRegex("https://example\\.com/id/format_[0-9a-f]{16}"));
            }

            @Test
            @DisplayName("the same copyright notice on two records is one node")
            void copyrightNoticeIsSharedAcrossRecords() {
                val notice = "This dataset is owned by UK Centre for Ecology & Hydrology";
                template("rdf/ttl.ftl", withCopyright("cpyA", notice));
                template("rdf/ttl.ftl", withCopyright("cpyB", "  this dataset is owned by UK Centre for Ecology & Hydrology  "));

                val rightsA = objectsOf("https://example.com/id/cpyA", RIGHTS);
                assertThat(
                    "case and surrounding whitespace should not fork the notice",
                    objectsOf("https://example.com/id/cpyB", RIGHTS), equalTo(rightsA)
                );
                assertTrue(model.contains(
                    rightsA.getFirst().asResource(),
                    createProperty(COPYRIGHT_NOTICE),
                    model.createLiteral(notice)
                ));
            }

            @Test
            @DisplayName("a copyright notice with no text emits no dcterms:rights")
            void emptyCopyrightNoticeIsSuppressed() {
                template("rdf/ttl.ftl", withCopyright("cpyempty", ""));

                assertTrue(
                    objectsOf("https://example.com/id/cpyempty", RIGHTS).isEmpty(),
                    "an empty notice should be suppressed, not minted as a node standing for nothing"
                );
            }

            @Test
            @DisplayName("the same affiliation on two records is one organisation node, carrying its name")
            void affiliationIsSharedAcrossRecords() {
                template("rdf/ttl.ftl", withAffiliation("orgA", "Wood", "Claire", "University of Exeter", ""));
                template("rdf/ttl.ftl", withAffiliation("orgB", "Dodd", "Ben", "University of Exeter", ""));

                val organisations = allObjectsOf(MEMBER);
                assertThat(
                    "one organisation named on two records is one node",
                    organisations.stream().distinct().toList().size(), equalTo(1)
                );
                val organisation = organisations.getFirst().asResource();
                assertThat(organisation.getURI(), matchesRegex("https://example\\.com/id/organisation_[0-9a-f]{16}"));
                assertTrue(model.contains(
                    organisation, createProperty(FOAF_NAME), model.createLiteral("University of Exeter")
                ));
            }

            @Test
            @DisplayName("an affiliation with a ROR still uses the ROR, and mints nothing")
            void rorAffiliationIsUnchanged() {
                template("rdf/ttl.ftl",
                    withAffiliation("orgror", "Wood", "Claire", "UK Centre for Ecology & Hydrology", "https://ror.org/00pggkr55"));

                assertThat(
                    allObjectsOf(MEMBER),
                    equalTo(List.<RDFNode>of(createResource("https://ror.org/00pggkr55")))
                );
                assertFalse(
                    model.contains(createResource("https://ror.org/00pggkr55"), createProperty(FOAF_NAME), (RDFNode) null),
                    "a ROR is externally governed and must not be given the record's typed name (dri-one #320)"
                );
            }

            @Test
            @DisplayName("an organisation-only contact is not made a member of itself")
            void organisationContactHasNoAffiliation() {
                val document = dataset("orgself");
                document.setAuthors(List.of(
                    ResponsibleParty.builder()
                        .organisationName("UK Environmental Change Network")
                        .role("author")
                        .build()
                ));

                template("rdf/ttl.ftl", document);

                assertTrue(
                    allObjectsOf(MEMBER).isEmpty(),
                    "the contact is the organisation, so foaf:member would assert membership of a "
                        + "second node carrying its own name"
                );
            }

            @Test
            @DisplayName("a definite article still forks an organisation — the accepted limitation")
            void organisationVariantsStillFork() {
                template("rdf/ttl.ftl", withAffiliation("orgvarA", "Wood", "Claire", "University of Edinburgh", ""));
                template("rdf/ttl.ftl", withAffiliation("orgvarB", "Dodd", "Ben", "The University of Edinburgh", ""));

                assertThat(
                    "minting makes the variants visible and joinable; reconciling them is data cleanup",
                    allObjectsOf(MEMBER).stream().distinct().toList().size(), equalTo(2)
                );
            }

            @Test
            @DisplayName("a role statement is a node that can be pointed at, not a blank one")
            void roleInTimeIsIdentified() {
                template("rdf/ttl.ftl", withAffiliation("roleA", "Wood", "Claire", "University of Exeter", ""));

                val roles = allObjectsOf(HOLDS_ROLE);
                assertThat(roles.size(), equalTo(1));
                val role = roles.getFirst().asResource();
                assertThat(role.getURI(), matchesRegex("https://example\\.com/id/role_[0-9a-f]{16}"));
                assertTrue(model.contains(
                    role, createProperty("http://purl.org/spar/pro/withRole"),
                    createResource("http://purl.org/spar/pro/author")
                ));
                assertTrue(model.contains(
                    role, createProperty("http://purl.org/spar/pro/relatesToEntity"),
                    createResource("https://example.com/id/roleA")
                ));
            }

            @Test
            @DisplayName("the same person in the same role on two records gets a node per record")
            void roleInTimeIsScopedToItsRecord() {
                template("rdf/ttl.ftl", withAffiliation("roleB", "Wood", "Claire", "University of Exeter", ""));
                template("rdf/ttl.ftl", withAffiliation("roleC", "Wood", "Claire", "University of Exeter", ""));

                assertThat(
                    "a RoleInTime states a role held on one record, so it cannot be shared between records",
                    allObjectsOf(HOLDS_ROLE).stream().distinct().toList().size(), equalTo(2)
                );
            }
        }
    }

    /**
     * Every {@code template(...)} call here parses the rendered Turtle with
     * {@link RDFDataMgr}, which throws on a malformed literal — so these tests
     * fail at the render, exactly as Fuseki's parser did in production, rather
     * than on an assertion about the model.
     */
    @Nested
    @DisplayName("Escaping literals for Turtle (dri-one #344)")
    class LiteralEscaping {

        private static final String GRANT_NUMBER = "http://purl.org/cerif/frapo/hasGrantNumber";
        private static final String TITLE = "http://purl.org/dc/terms/title";
        private static final String DESCRIPTION = "http://purl.org/dc/terms/description";
        private static final String SUBJECT = "http://purl.org/dc/terms/subject";
        private static final String FAMILY_NAME = "http://xmlns.com/foaf/0.1/familyName";
        private static final String FOAF_NAME = "http://xmlns.com/foaf/0.1/name";
        private static final String ADMS_IDENTIFIER = "http://www.w3.org/ns/adms#identifier";

        private GeminiDocument dataset(String id) {
            return (GeminiDocument) new GeminiDocument()
                .setType("dataset")
                .setId(id)
                .setUri("https://example.com/id/" + id)
                .setTitle("Literal escaping");
        }

        private List<RDFNode> objectsOf(String property) {
            return model.listObjectsOfProperty(createProperty(property)).toList();
        }

        private String soleLiteral(String property) {
            val objects = objectsOf(property);
            assertThat(property + " should have produced exactly one literal", objects.size(), equalTo(1));
            return objects.getFirst().asLiteral().getString();
        }

        /**
         * The literal that broke production. A Royal Society International
         * Collaboration Award is written with backslashes, {@code \R} is not a
         * legal Turtle escape, and the PUT is all-or-nothing — so this one
         * award number rejected the whole 20MB graph for a week.
         */
        @Test
        @DisplayName("a Royal Society award number survives the round trip")
        void royalSocietyAwardNumber() {
            val document = dataset("rsgrant");
            document.setFunding(List.of(
                Funding.builder().awardNumber("ICA\\R1\\180100").build()
            ));

            template("rdf/ttl.ftl", document);

            assertThat(
                "the backslashes must reach the consumer intact, not merely parse",
                soleLiteral(GRANT_NUMBER), equalTo("ICA\\R1\\180100")
            );
        }

        @Test
        @DisplayName("a Windows path pasted into a description survives the round trip")
        void backslashInDescription() {
            val document = dataset("winpath");
            document.setDescription("Source data held at C:\\Reports\\2026\\raw.csv");

            template("rdf/ttl.ftl", document);

            assertThat(soleLiteral(DESCRIPTION), equalTo("Source data held at C:\\Reports\\2026\\raw.csv"));
        }

        @Test
        @DisplayName("a backslash in a contact's name survives, on the raw literals too")
        void backslashInContactName() {
            val document = dataset("bsname");
            document.setAuthors(List.of(
                ResponsibleParty.builder()
                    .familyName("O\\Brien")
                    .givenName("Sinead")
                    .organisationName("Institute of \\Something")
                    .role("author")
                    .build()
            ));

            template("rdf/ttl.ftl", document);

            assertThat(
                "foaf:familyName interpolates directly rather than through displayLiteral",
                soleLiteral(FAMILY_NAME), equalTo("O\\Brien")
            );
            assertTrue(
                objectsOf(FOAF_NAME).stream()
                    .anyMatch(o -> o.asLiteral().getString().equals("Institute of \\Something")),
                "the minted organisation node's foaf:name should carry the backslash intact"
            );
        }

        @Test
        @SneakyThrows
        @DisplayName("a backslash in a keyword with no URI survives the literal fallback")
        void backslashInKeywordLiteral() {
            given(keywordVocabulary.resolveExactLabel("Soil\\moisture")).willReturn(Optional.empty());

            val document = dataset("bskeyword");
            document.setKeywordsOther(List.of(Keyword.builder().value("Soil\\moisture").build()));

            template("rdf/ttl.ftl", document);

            assertTrue(
                objectsOf(SUBJECT).stream()
                    .anyMatch(o -> o.isLiteral() && o.asLiteral().getString().equals("Soil\\moisture")),
                "the keyword literal fallback stripped quotes but not backslashes"
            );
        }

        @Test
        @DisplayName("a backslash in an alternate identifier survives")
        void backslashInAdmsIdentifier() {
            val document = dataset("bsident");
            document.setResourceIdentifiers(List.of(
                ResourceIdentifier.builder().code("REF\\2026\\001").codeSpace("local").build()
            ));

            template("rdf/ttl.ftl", document);

            assertTrue(
                objectsOf(ADMS_IDENTIFIER).stream()
                    .anyMatch(o -> o.isLiteral() && o.asLiteral().getString().equals("local/REF\\2026\\001")),
                "adms:identifier interpolates the code directly into a literal"
            );
        }

        @Test
        @DisplayName("a backslash in a monitoring facility's title survives its own displayLiteral")
        void backslashInMonitoringTitle() {
            val facility = new MonitoringFacility()
                .setId("bsfacility")
                .setUri("https://example.com/id/bsfacility")
                .setTitle("Site A\\B");

            template("rdf/monitoring/facility.ftl", facility);

            assertThat(
                "the monitoring templates carry a second copy of displayLiteral",
                soleLiteral(TITLE), equalTo("Site A\\B")
            );
        }

        @Test
        @DisplayName("a trailing backslash does not escape the literal's own closing quote")
        void trailingBackslash() {
            val document = dataset("bstrailing");
            document.setDescription("Ends with a backslash\\");

            template("rdf/ttl.ftl", document);

            assertThat(soleLiteral(DESCRIPTION), equalTo("Ends with a backslash\\"));
        }

        @Test
        @DisplayName("a doubled backslash is not collapsed")
        void doubledBackslash() {
            val document = dataset("bsdouble");
            document.setDescription("UNC path \\\\server\\share");

            template("rdf/ttl.ftl", document);

            assertThat(soleLiteral(DESCRIPTION), equalTo("UNC path \\\\server\\share"));
        }

        @Test
        @DisplayName("a double quote still becomes an apostrophe, and a line break a space")
        void quotesAndNewlinesKeepTheirLongstandingTreatment() {
            val document = dataset("bsquote");
            document.setDescription("A \"quoted\" phrase\nand a second line");

            template("rdf/ttl.ftl", document);

            assertThat(
                "substituting rather than escaping these is lossy but is what every "
                    + "already-published literal carries; see templates/rdf/_turtle.ftl",
                soleLiteral(DESCRIPTION), equalTo("A 'quoted' phrase and a second line")
            );
        }

        @Test
        @DisplayName("a record with a backslash in every text field still parses")
        void backslashEverywhere() {
            val document = dataset("bsall");
            document.setTitle("Title\\A");
            document.setDescription("Description\\B");
            document.setLineage("Lineage\\C");
            document.setAuthors(List.of(
                ResponsibleParty.builder()
                    .familyName("Family\\D").givenName("Given\\E")
                    .organisationName("Org\\F").email("a\\b@example.com")
                    .role("author").build()
            ));
            document.setFunding(List.of(
                Funding.builder().awardNumber("Award\\G").awardTitle("Grant\\H").build()
            ));
            document.setUseConstraints(List.of(
                ResourceConstraint.builder().code("copyright").value("Copyright\\I").build(),
                ResourceConstraint.builder().code("license").value("Licence\\J").build()
            ));
            document.setDistributionFormats(List.of(
                DistributionInfo.builder().name("Format\\K").type("").version("unknown").build()
            ));
            document.setOnlineResources(List.of(
                OnlineResource.builder()
                    .url("https://data-package.ceh.ac.uk/data/bsall").function("download").build()
            ));

            // The render is the assertion: a malformed literal anywhere in it throws.
            template("rdf/ttl.ftl", document);

            assertThat(soleLiteral(TITLE), equalTo("Title\\A"));
            assertThat(soleLiteral(DESCRIPTION), equalTo("Description\\B"));
        }
    }

    @Nested
    @DisplayName("What the export asserts about a contact (dri-one #348)")
    class ContactAssertions {

        private static final String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
        private static final String FOAF_NAME = "http://xmlns.com/foaf/0.1/name";
        private static final String FAMILY_NAME = "http://xmlns.com/foaf/0.1/familyName";
        private static final String GIVEN_NAME = "http://xmlns.com/foaf/0.1/givenName";
        private static final String MEMBER = "http://xmlns.com/foaf/0.1/member";
        private static final String HAS_EMAIL = "http://www.w3.org/2006/vcard/ns#hasEmail";
        private static final String HOLDS_ROLE = "http://purl.org/spar/pro/holdsRoleInTime";

        private static final String ORCID = "https://orcid.org/0000-0002-0394-2998";
        private static final String ISNI = "https://isni.org/isni/0000000121032683";
        private static final String ROR = "https://ror.org/00pggkr55";

        private GeminiDocument dataset(String id) {
            return (GeminiDocument) new GeminiDocument()
                .setType("dataset")
                .setId(id)
                .setUri("https://example.com/id/" + id)
                .setTitle("Contact assertions");
        }

        private ResponsibleParty.ResponsiblePartyBuilder person() {
            return ResponsibleParty.builder()
                .familyName("Wood").givenName("Claire")
                .email("claire.wood@example.com")
                .organisationName("UK Centre for Ecology & Hydrology")
                .role("author");
        }

        private GeminiDocument withAuthor(String id, ResponsibleParty author) {
            val document = dataset(id);
            document.setAuthors(List.of(author));
            return document;
        }

        /** Every predicate the model asserts about one subject. */
        private List<String> predicatesOf(String subject) {
            return model.listStatements(createResource(subject), null, (RDFNode) null)
                .toList().stream()
                .map(s -> s.getPredicate().getURI())
                .distinct().sorted().toList();
        }

        @Test
        @DisplayName("no contact's email address is published, whatever their role")
        void noEmailAnywhere() {
            val document = dataset("noemail");
            document.setAuthors(List.of(person().build()));
            document.setContactPoints(List.of(
                person().familyName("Emmett").givenName("Bridget").email("bridget@example.com").build()
            ));
            document.setPublishers(List.of(
                ResponsibleParty.builder()
                    .organisationName("NERC EDS Environmental Information Data Centre")
                    .organisationIdentifier("https://ror.org/04xw4m193")
                    .email("info@eidc.ac.uk")
                    .build()
            ));

            template("rdf/ttl.ftl", document);

            assertTrue(
                model.listObjectsOfProperty(createProperty(HAS_EMAIL)).toList().isEmpty(),
                "the record page withholds author addresses per role; this export had no "
                    + "role context and published everyone's"
            );
        }

        @Test
        @DisplayName("an ORCID gets its type, its affiliation and its role — and nothing else")
        void orcidCarriesNoRecordText() {
            template("rdf/ttl.ftl", withAuthor("orcid", person().nameIdentifier(ORCID).build()));

            assertThat(
                "writing record text onto a shared external identifier is what dri-one #320 "
                    + "forbids, and what accumulated 281 conflicting names in production",
                predicatesOf(ORCID),
                equalTo(List.of(HOLDS_ROLE, RDF_TYPE, MEMBER))
            );
        }

        @Test
        @DisplayName("an ISNI is treated exactly as an ORCID is")
        void isniCarriesNoRecordText() {
            template("rdf/ttl.ftl", withAuthor("isni", person().nameIdentifier(ISNI).build()));

            assertThat(predicatesOf(ISNI), equalTo(List.of(HOLDS_ROLE, RDF_TYPE, MEMBER)));
        }

        @Test
        @DisplayName("an ORCID keeps its stated affiliation, and the organisation node with it")
        void orcidKeepsItsAffiliation() {
            template("rdf/ttl.ftl", withAuthor("orcidaff", person().nameIdentifier(ORCID).build()));

            val organisations = model.listObjectsOfProperty(
                createResource(ORCID), createProperty(MEMBER)
            ).toList();
            assertThat(organisations.size(), equalTo(1));

            val organisation = organisations.getFirst().asResource();
            assertThat(organisation.getURI(), matchesRegex("https://example\\.com/id/organisation_[0-9a-f]{16}"));
            assertTrue(
                model.contains(organisation, createProperty(FOAF_NAME),
                    model.createLiteral("UK Centre for Ecology & Hydrology")),
                "the organisation node is minted from its name, so the name identifies it "
                    + "and asserting it is safe — unlike on the ORCID"
            );
        }

        @Test
        @DisplayName("an ORCID with a ROR affiliation points at the ROR itself")
        void orcidWithRorAffiliation() {
            template("rdf/ttl.ftl", withAuthor("orcidror",
                person().nameIdentifier(ORCID).organisationIdentifier(ROR).build()));

            assertThat(
                model.listObjectsOfProperty(createResource(ORCID), createProperty(MEMBER)).toList(),
                equalTo(List.<RDFNode>of(createResource(ROR)))
            );
        }

        @Test
        @DisplayName("a contact with no persistent identifier keeps its name on the minted node")
        void mintedPersonKeepsItsName() {
            template("rdf/ttl.ftl", withAuthor("minted", person().build()));

            val minted = model.listSubjectsWithProperty(createProperty(FOAF_NAME)).toList().stream()
                .filter(s -> s.getURI() != null && s.getURI().contains("/id/person_"))
                .findFirst().orElseThrow();

            assertTrue(model.contains(minted, createProperty(FOAF_NAME), model.createLiteral("Wood, C.")));
            assertTrue(model.contains(minted, createProperty(FAMILY_NAME), model.createLiteral("Wood")));
            assertTrue(model.contains(minted, createProperty(GIVEN_NAME), model.createLiteral("Claire")));
            assertFalse(
                model.contains(minted, createProperty(HAS_EMAIL), (RDFNode) null),
                "the node is keyed on the name, so the name is safe to assert; the email is not"
            );
        }

        @Test
        @DisplayName("a ROR-identified organisation contact still carries only its type")
        void rorOrganisationIsStillTypeOnly() {
            val document = dataset("rororg");
            document.setPublishers(List.of(
                ResponsibleParty.builder()
                    .organisationName("UK Centre for Ecology & Hydrology")
                    .organisationIdentifier(ROR)
                    .email("enquiries@example.com")
                    .build()
            ));

            template("rdf/ttl.ftl", document);

            assertThat(
                "a ROR-identified organisation is the organisation, so it takes no name, "
                    + "no email and no membership of itself (dri-one #320, #334)",
                predicatesOf(ROR), equalTo(List.of(RDF_TYPE))
            );
        }
    }

}
