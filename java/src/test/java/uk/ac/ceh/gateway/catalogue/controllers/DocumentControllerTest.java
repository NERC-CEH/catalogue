package uk.ac.ceh.gateway.catalogue.controllers;

import freemarker.template.Configuration;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StreamUtils;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.ef.*;
import uk.ac.ceh.gateway.catalogue.erammp.ErammpDatacube;
import uk.ac.ceh.gateway.catalogue.erammp.ErammpModel;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.imp.CaseStudy;
import uk.ac.ceh.gateway.catalogue.imp.Model;
import uk.ac.ceh.gateway.catalogue.imp.ModelApplication;
import uk.ac.ceh.gateway.catalogue.infrastructure.InfrastructureRecord;
import uk.ac.ceh.gateway.catalogue.model.*;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModel;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModelApplication;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringActivity;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringFacility;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringNetwork;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringProgramme;
import uk.ac.ceh.gateway.catalogue.osdp.Agent;
import uk.ac.ceh.gateway.catalogue.osdp.Dataset;
import uk.ac.ceh.gateway.catalogue.osdp.Publication;
import uk.ac.ceh.gateway.catalogue.osdp.Sample;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;
import uk.ac.ceh.gateway.catalogue.profiles.ProfileService;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.sa.SampleArchive;
import uk.ac.ceh.gateway.catalogue.serviceagreement.GitRepoServiceAgreementService;
import uk.ac.ceh.gateway.catalogue.services.MetricsService;
import uk.ac.ceh.gateway.catalogue.templateHelpers.CodeLookupService;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_HTML;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.*;
import static uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig.EIDC_PUBLISHER_USERNAME;
import static uk.ac.ceh.gateway.catalogue.model.MetadataInfo.PUBLIC_GROUP;

@Slf4j
@WithMockCatalogueUser
@ActiveProfiles("test")
@DisplayName("DocumentController")
@Import({
    SecurityConfig.class,
    SecurityConfigCrowd.class,
    DevelopmentUserStoreConfig.class
})
@WebMvcTest(
    controllers=DocumentController.class,
    properties={"spring.freemarker.template-loader-path=file:../templates","metrics.users.excluded=i_am_excluded"}
)
class DocumentControllerTest {
    @MockBean private CatalogueService catalogueService;
    @MockBean private CodeLookupService codeLookupService;
    @MockBean private DocumentRepository documentRepository;
    @MockBean private JenaLookupService jenaLookupService;
    @MockBean(name="permission") private PermissionService permissionService;
    @MockBean private ProfileService profileService;
    @MockBean private MetricsService metricsService;

    @Autowired private MockMvc mvc;
    @Autowired private Configuration configuration;

    private DocumentController controller;
    private final String linkedDocumentId = "0a6c7c4c-0515-40a8-b84e-7ffe622b2579";
    private final String id = "fe26bd48-0f81-4a37-8a28-58427b7e20bd";
    private final String catalogueKey = "eidc";
    private List<String> metricsExcludedUsers = Arrays.asList("bob","alice","i_am_excluded");
    public static final String HTML = "html";
    public static final String JSON = "json";

    @BeforeEach
    void setup() {
        controller = new DocumentController(metricsService, metricsExcludedUsers, documentRepository);
    }

    private void givenUserIsPermittedToView() {
        given(permissionService.toAccess(any(CatalogueUser.class), eq(id), eq("VIEW")))
            .willReturn(true);
    }

    @SneakyThrows
    private void givenFreemarkerConfiguration() {
        configuration.setSharedVariable("catalogues", catalogueService);
        configuration.setSharedVariable("codes", codeLookupService);
        configuration.setSharedVariable("jena", jenaLookupService);
        configuration.setSharedVariable("permission", permissionService);
        configuration.setSharedVariable("profile", profileService);
    }

    private void givenProfileNotActive() {
        given(profileService.isActive("service-agreement"))
            .willReturn(false);
    }

    private void givenCatalogue() {
        given(catalogueService.retrieve(catalogueKey))
            .willReturn(
                Catalogue.builder()
                    .id(catalogueKey)
                    .title("Env Data Centre")
                    .url("https://example.com")
                    .contactUrl("")
                    .logo("eidc.png")
                    .build()
            );
    }

    private void givenDefaultCatalogue() {
        given(catalogueService.defaultCatalogue())
            .willReturn(
                Catalogue.builder()
                    .id(catalogueKey)
                    .title("Env Data Centre")
                    .url("https://example.com")
                    .contactUrl("")
                    .logo("eidc.png")
                    .build()
            );
    }

    private void givenCodeLookup() {
        given(codeLookupService.lookup("metadata.recordType", "dataset"))
            .willReturn("Dataset");
        given(codeLookupService.lookup("publication.state", "public"))
            .willReturn("Public");
    }

    @SneakyThrows
    private String expectedResponse(String filename) {
        return StreamUtils.copyToString(
            getClass().getResourceAsStream(filename),
            StandardCharsets.UTF_8
        );
    }

    @SneakyThrows
    private void givenMetadataDocument(MetadataDocument doc) {
        log.debug(doc.toString());
        doc.setId(id);
        doc.setTitle("Test title");
        doc.setDescription("This is a multiline description.\n\nContinued on another line.");
        doc.setUri("https://example.com/" + id);
        doc.setMetadataDate(LocalDateTime.of(2021, 5, 12, 9, 30, 23));
        doc.setMetadata(MetadataInfo.builder()
            .catalogue(catalogueKey)
            .state("public")
            .build());
        given(documentRepository.read(id))
            .willReturn(doc);
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> provideMetadataDocuments() {
        val activity = new Activity();
        activity.setEfMetadata(new Metadata());

        val facility = new Facility();
        facility.setEfMetadata(new Metadata());

        val gemini = new GeminiDocument();
        gemini.setId("da9d9beb-3fe5-4799-a4ed-c558d55159e6");
        gemini.setType("dataset");
        val bbox = BoundingBox.builder()
            .northBoundLatitude("59.456")
            .eastBoundLongitude("2.574")
            .southBoundLatitude("31.109")
            .westBoundLongitude("-1.091")
            .build();
        gemini.setBoundingBoxes(Collections.singletonList(bbox));
        gemini.setOnlineResources(List.of(
            OnlineResource.builder()
                .url("https://example.com/maps/da9d9beb-3fe5-4799-a4ed-c558d55159e6?request=getCapabilities&service=WMS")
                .build()
        ));

        val caseStudy = new CaseStudy();
        caseStudy.setType("dataset");

        val impModel = new Model();
        impModel.setType("dataset");

        val impModelApplication = new ModelApplication();
        impModelApplication.setType("dataset");

        val original = new GeminiDocument();
        val metadataInfo = MetadataInfo.builder()
            .state("published")
            .catalogue("eidc")
            .build();
        metadataInfo.addPermission(Permission.VIEW, PUBLIC_GROUP);
        original.setMetadata(metadataInfo);
        original.setTitle("Test title");
        log.info("Original: {}", original);


        val link = LinkDocument.builder()
            .linkedDocumentId("cbde2ff1-cae3-4189-9489-ef1f4435fadc")
            .original(original)
            .additionalKeywords(new ArrayList<>())
            .build();

        log.debug(link.toString());

        val network = new Network();
        network.setEfMetadata(new Metadata());

        val programme = new Programme();
        programme.setEfMetadata(new Metadata());

        return Stream.of(
            Arguments.of(activity, TEXT_HTML, HTML, null),
            Arguments.of(activity, APPLICATION_JSON, JSON, null),
            Arguments.of(activity, EF_INSPIRE_XML, EF_INSPIRE_XML_SHORT, null),
            Arguments.of(new Agent(), TEXT_HTML, HTML, null),
            Arguments.of(new Agent(), APPLICATION_JSON, JSON, null),
            Arguments.of(new CehModel(), TEXT_HTML, HTML, null),
            Arguments.of(new CehModel(), APPLICATION_JSON, JSON, null),
            Arguments.of(new CehModelApplication(), TEXT_HTML, HTML, null),
            Arguments.of(new CehModelApplication(), APPLICATION_JSON, JSON, null),
            Arguments.of(new Dataset(), TEXT_HTML, HTML, null),
            Arguments.of(new Dataset(), APPLICATION_JSON, JSON, null),
            Arguments.of(new DataType(), TEXT_HTML, HTML, null),
            Arguments.of(new DataType(), APPLICATION_JSON, JSON, null),
            Arguments.of(new ErammpModel(), TEXT_HTML, HTML, null),
            Arguments.of(new ErammpModel(), APPLICATION_JSON, JSON, null),
            Arguments.of(new ErammpDatacube(), TEXT_HTML, HTML, null),
            Arguments.of(new ErammpDatacube(), APPLICATION_JSON, JSON, null),
            Arguments.of(new InfrastructureRecord(), TEXT_HTML, HTML, null),
            Arguments.of(new InfrastructureRecord(), APPLICATION_JSON, JSON, null),
            Arguments.of(facility, TEXT_HTML, HTML, null),
            Arguments.of(facility, APPLICATION_JSON, JSON, null),
            Arguments.of(facility, EF_INSPIRE_XML, EF_INSPIRE_XML_SHORT, null),
            Arguments.of(gemini, TEXT_HTML, HTML, null),
            Arguments.of(gemini, APPLICATION_JSON, JSON, "gemini.json"),
            Arguments.of(gemini, GEMINI_XML, GEMINI_XML_SHORT,  "gemini.xml"),
            Arguments.of(gemini, RDF_SCHEMAORG_JSON, RDF_SCHEMAORG_SHORT, "gemini-schema-org.json"),
            Arguments.of(gemini, CEDA_YAML_JSON, CEDA_YAML_SHORT, "gemini-ceda-yaml.json"),
            Arguments.of(gemini, RDF_TTL, RDF_TTL_SHORT, "gemini.ttl"),
            Arguments.of(caseStudy, TEXT_HTML, HTML, null),
            Arguments.of(caseStudy, APPLICATION_JSON, JSON, null),
            Arguments.of(impModel, TEXT_HTML, HTML, null),
            Arguments.of(impModel, APPLICATION_JSON, JSON, null),
            Arguments.of(impModelApplication, TEXT_HTML, HTML, null),
            Arguments.of(impModelApplication, APPLICATION_JSON, JSON, null),
            Arguments.of(link, TEXT_HTML, HTML, null),
            Arguments.of(link, APPLICATION_JSON, JSON, "link.json"),
            Arguments.of(new uk.ac.ceh.gateway.catalogue.osdp.Model(), TEXT_HTML, HTML, null),
            Arguments.of(new uk.ac.ceh.gateway.catalogue.osdp.Model(), APPLICATION_JSON, JSON, null),
            Arguments.of(new MonitoringActivity(), TEXT_HTML, HTML, null),
            Arguments.of(new MonitoringActivity(), APPLICATION_JSON, JSON, null),
            Arguments.of(new MonitoringFacility(), TEXT_HTML, HTML, null),
            Arguments.of(new MonitoringFacility(), APPLICATION_JSON, JSON, null),
            Arguments.of(new MonitoringNetwork(), TEXT_HTML, HTML, null),
            Arguments.of(new MonitoringNetwork(), APPLICATION_JSON, JSON, null),
            Arguments.of(new MonitoringProgramme(), TEXT_HTML, HTML, null),
            Arguments.of(new MonitoringProgramme(), APPLICATION_JSON, JSON, null),
            Arguments.of(network, TEXT_HTML, HTML, null),
            Arguments.of(network, EF_INSPIRE_XML, EF_INSPIRE_XML_SHORT, null),
            Arguments.of(network, APPLICATION_JSON, JSON, null),
            Arguments.of(programme, TEXT_HTML, HTML, null),
            Arguments.of(programme, EF_INSPIRE_XML, EF_INSPIRE_XML_SHORT, null),
            Arguments.of(programme, APPLICATION_JSON, JSON, null),
            Arguments.of(new Publication(), TEXT_HTML, HTML, null),
            Arguments.of(new Publication(), APPLICATION_JSON, JSON, null),
            Arguments.of(new Sample(), TEXT_HTML, HTML, null),
            Arguments.of(new Sample(), APPLICATION_JSON, JSON, null),
            Arguments.of(new SampleArchive(), TEXT_HTML, HTML, null),
            Arguments.of(new SampleArchive(), APPLICATION_JSON, JSON, null)
        );
    }

    @SuppressWarnings("unused")
    @ParameterizedTest(name = "[{index}] GET as {1}, {3}, {0}")
    @MethodSource("provideMetadataDocuments")
    @SneakyThrows
    void getMetadataDocumentAsMediaType(MetadataDocument doc, MediaType mediaType, String shortName, String filename) {
        //given
        givenUserIsPermittedToView();
        givenMetadataDocument(doc);
        givenCatalogue();
        givenFreemarkerConfiguration();
        givenProfileNotActive();
        givenCodeLookup();

        //when
        val result = mvc.perform(
            get("/documents/{id}", id)
                .accept(mediaType)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(mediaType));

        if (filename == null) {
            result.andDo(print());
        } else {
            if (mediaType.isCompatibleWith(APPLICATION_JSON)) {
                result.andExpect(content().json(expectedResponse(filename)));
            } else if (mediaType.isCompatibleWith(TEXT_HTML)) {
                result.andExpect(content().string(expectedResponse(filename)));
            } else if (mediaType.isCompatibleWith(MediaType.APPLICATION_XML)) {
                result.andExpect(content().xml(expectedResponse(filename)));
            }
        }
    }

    @ParameterizedTest(name = "[{index}] GET using format {2}, {3}, {0}")
    @MethodSource("provideMetadataDocuments")
    @SneakyThrows
    void getMetadataDocumentUsingFormatQueryParam(MetadataDocument doc, MediaType mediaType, String shortName, String filename) {
        //given
        givenUserIsPermittedToView();
        givenMetadataDocument(doc);
        givenCatalogue();
        givenFreemarkerConfiguration();
        givenProfileNotActive();
        givenCodeLookup();


        //when
        val result = mvc.perform(
            get("/documents/{id}", id)
                .queryParam("format", shortName)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(mediaType));

        if (filename == null) {
            result.andDo(print());
        } else {
            if (mediaType.isCompatibleWith(APPLICATION_JSON)) {
                result.andExpect(content().json(expectedResponse(filename)));
            } else if (mediaType.isCompatibleWith(TEXT_HTML)) {
                result.andExpect(content().string(expectedResponse(filename)));
            } else if (mediaType.isCompatibleWith(MediaType.APPLICATION_XML)) {
                result.andExpect(content().xml(expectedResponse(filename)));
            }
        }
    }

    @Test
    @SneakyThrows
    void getMetadataDocumentUsingFileExtension() {
        //given
        givenUserIsPermittedToView();

        //when
        mvc.perform(
            get("/documents/{id}.xml", id)
        )
            .andExpect(status().isOk())
            .andExpect(forwardedUrl("/documents/" + id + "?format=gemini"));

    }

    @Test
    @SneakyThrows
    void getUploadPage() {
        //given
        givenDefaultCatalogue();
        givenFreemarkerConfiguration();

        //when
        mvc.perform(
            get("/documents/upload")
        )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(TEXT_HTML));
    }

    @Test
    public void checkCanUploadFile() throws Exception {
        //Given
        InputStream inputStream = new ByteArrayInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?><root></root>".getBytes());
        val multipartFile = new MockMultipartFile("file", "test", MediaType.APPLICATION_XML_VALUE, inputStream);
        String documentType = "GEMINI_DOCUMENT";
        GeminiDocument document = new GeminiDocument();
        document.setUri("https://catalogue.ceh.ac.uk/id/123-test");
        String message = "new file upload";
        String catalogue = "catalogue";

        given(permissionService.userCanCreate(catalogue)).willReturn(true);
        given(documentRepository.save(
            any(CatalogueUser.class),
            any(InputStream.class),
            eq(MediaType.APPLICATION_XML),
            eq(documentType),
            eq(catalogue),
            eq(message))
        ).willReturn(document);

        //When
        mvc.perform(
            multipart("/documents")
                .file(multipartFile)
                .param("type", documentType)
                .param("catalogue", catalogue)
                .header("remote-user", EIDC_PUBLISHER_USERNAME)
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(header().string("location", "https://catalogue.ceh.ac.uk/id/123-test"));
    }

    @Test
    public void checkCanCreateModelDocument() throws Exception {
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        Model document = new Model();
        document.setUri("https://catalogue.ceh.ac.uk/id/123-test");
        String message = "new Model Document";
        String catalogue = "catalogue";

        given(documentRepository.saveNew(user, document, catalogue, message)).willReturn(document);

        //When
        ResponseEntity<MetadataDocument> actual = controller.newModelDocument(user, document, catalogue);

        //Then
        verify(documentRepository).saveNew(user, document, catalogue, message);
        assertThat("Should have 201 CREATED status", actual.getStatusCode(), equalTo(HttpStatus.CREATED));
    }

    @Test
    public void checkCanEditModelDocument() throws Exception {
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        Model document = new Model();
        document.setUri("https://catalogue.ceh.ac.uk/id/123-test");
        String fileId = "test";
        String message = "Edited document: test";

        given(documentRepository.read(fileId)).willReturn(new Model().setMetadata(MetadataInfo.builder().build()));
        given(documentRepository.save(user, document, fileId, message)).willReturn(document);

        //When
        ResponseEntity<MetadataDocument> actual = controller.updateModelDocument(user, fileId, document);

        //Then
        verify(documentRepository).save(user, document, fileId, "Edited document: test");
        verify(documentRepository).read(fileId);
        assertThat("Should have 200 OK status", actual.getStatusCode(), equalTo(HttpStatus.OK));
    }

    @Test
    public void checkCanCreateGeminiDocument() throws Exception {
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        GeminiDocument document = new GeminiDocument();
        document.setUri("https://catalogue.ceh.ac.uk/id/123-test");
        String message = "new Gemini Document";
        String catalogue = "catalogue";

        given(documentRepository.saveNew(user, document, catalogue, message)).willReturn(document);

        //When
        ResponseEntity<MetadataDocument> actual = controller.newGeminiDocument(user, document, catalogue);

        //Then
        verify(documentRepository).saveNew(user, document, catalogue, message);
        assertThat("Should have 201 CREATED status", actual.getStatusCode(), equalTo(HttpStatus.CREATED));
    }

    @Test
    public void checkCanEditGeminiDocument() throws Exception {
        //Given
        String fileId = "test";
        String message = "Edited document: test";
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        MetadataDocument document = new GeminiDocument()
            .setId(fileId)
            .setUri("https://catalogue.ceh.ac.uk/id/123-test")
            .setMetadata(MetadataInfo.builder().build());

        given(documentRepository.read(fileId)).willReturn(new Model().setMetadata(MetadataInfo.builder().build()));
        given(documentRepository.save(user, document, fileId, message)).willReturn(document);

        //When
        ResponseEntity<MetadataDocument> actual = controller.updateGeminiDocument(user, fileId, (GeminiDocument) document);

        //Then
        verify(documentRepository).read(fileId);
        verify(documentRepository).save(user, document, fileId, "Edited document: test");
        assertThat("Should have 200 OK status", actual.getStatusCode(), equalTo(HttpStatus.OK));
    }

    @Test
    public void checkCanCreateLinkedDocument() throws Exception {
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        LinkDocument document = LinkDocument.builder().linkedDocumentId(linkedDocumentId).build();
        document.setUri("https://catalogue.ceh.ac.uk/id/123-test");
        String message = "new Linked Document";
        String catalogue = "catalogue";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("catalogue.ceh.ac.uk");

        given(documentRepository.saveNew(user, document, catalogue, message)).willReturn(document);

        //When
        ResponseEntity<MetadataDocument> actual = controller.newLinkDocument(user, document, catalogue);

        //Then
        verify(documentRepository).saveNew(user, document, catalogue, message);
        assertThat("Should have 201 CREATED status", actual.getStatusCode(), equalTo(HttpStatus.CREATED));
    }

    @Test
    public void checkCanEditLinkedDocument() throws Exception {
        //Given
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        LinkDocument document = LinkDocument.builder().linkedDocumentId(linkedDocumentId).build();
        document.setUri("https://catalogue.ceh.ac.uk/id/123-test");
        String fileId = "test";
        String message = "Edited document: test";

        given(documentRepository.read(fileId)).willReturn(new Model().setMetadata(MetadataInfo.builder().build()));
        given(documentRepository.save(user, document, fileId, message)).willReturn(document);

        //When
        ResponseEntity<MetadataDocument> actual = controller.updateLinkDocument(user, fileId, document);

        //Then
        verify(documentRepository).read(fileId);
        verify(documentRepository).save(user, document, fileId, message);
        assertThat("Should have 200 OK status", actual.getStatusCode(), equalTo(HttpStatus.OK));
    }

    @Test
    public void cannotViewNonPublicMetadataDocumentThroughLinkDocument() throws Exception {
        //given
        HttpServletRequest request = mock(HttpServletRequest.class);
        MetadataDocument master = new GeminiDocument().setMetadata(
            MetadataInfo.builder().state("draft").build()
        );
        LinkDocument linkDocument = LinkDocument.builder().linkedDocumentId("master").original(master).build();
        given(documentRepository.read("test")).willReturn(linkDocument);

        //when
        MetadataDocument actual = controller.readMetadata(CatalogueUser.PUBLIC_USER, "test", request);

        //then
        assertThat(
            "should not be able to view master record through linked document",
            actual.getClass(),
            equalTo(LinkDocument.class)
        );
    }

    @Test
    public void checkCanDeleteAFile() throws Exception {
        //Given
        CatalogueUser user = mock(CatalogueUser.class);

        //When
        controller.deleteDocument(user, "id");

        //Then
        verify(documentRepository).delete(user, "id");
    }

    @Test
    public void checkCanReadDocumentAtRevision() throws Exception {
        //Given
        CatalogueUser user = CatalogueUser.PUBLIC_USER;
        String file = "myFile";
        String latestRevisionId = "latestRev";
        MetadataInfo info = MetadataInfo.builder().build();
        MetadataDocument document = new GeminiDocument();
        document.setMetadata(info);
        given(documentRepository.read(file, latestRevisionId))
            .willReturn(document);

        //When
        controller.readMetadata(user, file, latestRevisionId);

        //Then
        verify(documentRepository).read(file, latestRevisionId);
    }

    @Test
    public void checkCanReadDocumentLatestRevision() throws Exception {
        //Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        CatalogueUser user = CatalogueUser.PUBLIC_USER;
        String file = "myFile";
        MetadataInfo info = MetadataInfo.builder().build();
        MetadataDocument document = new GeminiDocument();
        document.setMetadata(info);
        given(documentRepository.read(file))
            .willReturn(document);

        //When
        controller.readMetadata(user, file, request);

        //Then
        verify(documentRepository).read(file);
    }

    @Test
    @SneakyThrows
    public void metricsServiceNotCalledWhenUserExcluded() {
        //Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        CatalogueUser user = new CatalogueUser("i_am_excluded", "test@example.com");
        String file = "myFile";
        MetadataInfo info = MetadataInfo.builder().build();
        MetadataDocument document = new GeminiDocument();
        document.setMetadata(info);
        given(documentRepository.read(file))
            .willReturn(document);

        //When
        controller.readMetadata(user, file, request);

        //then
        verify(metricsService, never()).recordView(any(), any());
    }

    @Test
    @SneakyThrows
    public void metricsServiceNotCalledWhenNonExcludedUser() {
        //Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        CatalogueUser user = new CatalogueUser("any_old_user", "test@example.com");
        String file = "myFile";
        MetadataInfo info = MetadataInfo.builder().state(GitRepoServiceAgreementService.PUBLISHED).build();
        MetadataDocument document = new GeminiDocument();
        document.setMetadata(info);
        given(documentRepository.read(file))
            .willReturn(document);

        //When
        controller.readMetadata(user, file, request);

        //then
        verify(metricsService).recordView(eq(file), any());
    }

    @Test
    @SneakyThrows
    public void metricsServiceNotCalledOnDraftDocuments() {
        //Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        CatalogueUser user = CatalogueUser.PUBLIC_USER;
        String file = "myFile";
        MetadataInfo info = MetadataInfo.builder().state(GitRepoServiceAgreementService.DRAFT).build();
        MetadataDocument document = new GeminiDocument();
        document.setMetadata(info);
        given(documentRepository.read(file))
            .willReturn(document);

        //When
        controller.readMetadata(user, file, request);

        //Then
        verify(metricsService, never()).recordView(any(), any());
    }
}
