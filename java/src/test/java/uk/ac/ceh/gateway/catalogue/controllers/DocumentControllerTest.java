package uk.ac.ceh.gateway.catalogue.controllers;

import freemarker.template.Configuration;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StreamUtils;
import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.DownloadUrlProperties;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.geometry.BoundingBox;
import uk.ac.ceh.gateway.catalogue.infrastructure.InfrastructureRecord;
import uk.ac.ceh.gateway.catalogue.researchActivity.ResearchActivity;
import uk.ac.ceh.gateway.catalogue.metrics.MetricsService;
import uk.ac.ceh.gateway.catalogue.model.*;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModel;
import uk.ac.ceh.gateway.catalogue.modelceh.CehModelApplication;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringActivity;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringFacility;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringNetwork;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringProgramme;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;
import uk.ac.ceh.gateway.catalogue.profiles.ProfileService;
import uk.ac.ceh.gateway.catalogue.quality.MultiDocumentTypeMetadataQualityService;
import uk.ac.ceh.gateway.catalogue.quality.Results;
import uk.ac.ceh.gateway.catalogue.repository.CachedDataRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.sa.SampleArchive;
import uk.ac.ceh.gateway.catalogue.serviceagreement.GitRepoServiceAgreementService;
import uk.ac.ceh.gateway.catalogue.templateHelpers.CodeLookupService;
import uk.ac.ceh.gateway.catalogue.templateHelpers.DownloadOrderDetailsService;
import uk.ac.ceh.gateway.catalogue.templateHelpers.FileDetailsService;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;
import uk.ac.ceh.gateway.catalogue.AbstractMvcTest;

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
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_HTML;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.*;
import static uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig.EIDC_PUBLISHER_USERNAME;
import static uk.ac.ceh.gateway.catalogue.model.MetadataInfo.PUBLIC_GROUP;

@Slf4j
@WithMockCatalogueUser
@ActiveProfiles({"test", "server-eidc", "search-basic"})
@DisplayName("DocumentController")
@Import({
    SecurityConfig.class,
    SecurityConfigCrowd.class,
    DevelopmentUserStoreConfig.class
})

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class DocumentControllerTest extends AbstractMvcTest {
    @MockitoBean private CatalogueService catalogueService;
    @MockitoBean private CodeLookupService codeLookupService;
    @MockitoBean private DocumentRepository documentRepository;
    @MockitoBean private JenaLookupService jenaLookupService;
    @MockitoBean(name="permission") private PermissionService permissionService;
    @MockitoBean private ProfileService profileService;
    @MockitoBean private MetricsService metricsService;
    @MockitoBean private FileDetailsService fileDetailsService;
    @MockitoBean private MultiDocumentTypeMetadataQualityService metadataQualityService;
    @MockitoBean private JenaLookupService jenaService;

    @NotNull DownloadUrlProperties downloadUrlProperties;

    /*
         Cannot make this a MockBean because DownloadOrder cannot be instantiated independently
         of the DownloadOrderDetailsService. It is needed in the given() method of the mock.
        */
    private DownloadOrderDetailsService downloadOrderDetailsService;
    @Autowired private Configuration configuration;

    @MockitoBean private CachedDataRepository cachedDataRepository;
    private DocumentController controller;
    private final String linkedDocumentId = "0a6c7c4c-0515-40a8-b84e-7ffe622b2579";
    private final String id = "fe26bd48-0f81-4a37-8a28-58427b7e20bd";
    private final String catalogueKey = "eidc";
    private final List<String> metricsExcludedUsers = Arrays.asList("bob","alice","i_am_excluded");
    public static final String HTML = "html";
    public static final String JSON = "json";

    @BeforeEach
    void setup() {
        controller = new DocumentController(metricsService, metricsExcludedUsers, documentRepository, jenaService, cachedDataRepository);
        DownloadUrlProperties downloadUrlProperties = mock(DownloadUrlProperties.class);
        when(downloadUrlProperties.getRegexOrder()).thenReturn("https://order-eidc\\.ceh\\.ac\\.uk/resources/.{8}/order\\?*.*");
        when(downloadUrlProperties.getRegexPackage()).thenReturn("https://data-package\\.ceh\\.ac\\.uk/.*");
        when(downloadUrlProperties.getRegexDatastore()).thenReturn("https://catalogue\\.ceh\\.ac\\.uk/datastore/eidchub/.*");
        when(downloadUrlProperties.getRegexCeda()).thenReturn("https://data\\.ceda\\.ac\\.uk/eidc/.*");
        when(downloadUrlProperties.getRegexSupportingDocs()).thenReturn("https://data-package\\.ceh\\.ac\\.uk/sd/.*");
        when(downloadUrlProperties.getRegexOrderManDownload()).thenReturn("http(s?)://catalogue\\.ceh\\.ac\\.uk/download\\?fileIdentifier=.*");
        this.downloadOrderDetailsService =
            new DownloadOrderDetailsService(downloadUrlProperties);
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
        configuration.setSharedVariable("downloadOrderDetails", downloadOrderDetailsService);
        configuration.setSharedVariable("fileDetails", fileDetailsService);
        configuration.setSharedVariable("metadataQuality", metadataQualityService);
        configuration.setSharedVariable("downloadUrlRegexes", downloadUrlProperties);
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

    private void givenMetadataQuality() {
        given(metadataQualityService.check(id))
            .willReturn(new Results(Collections.emptyList(), id));
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

    private void givenRoCrateServiceFrom() {
        given(fileDetailsService.getDetailsFor(anyString(), anyBoolean(), anyString()))
            .willReturn(new ArrayList<>());
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> provideMetadataDocuments() {
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

        return Stream.of(
            Arguments.of(new CehModel(), TEXT_HTML, HTML, null),
            Arguments.of(new CehModel(), APPLICATION_JSON, JSON, null),
            Arguments.of(new CehModelApplication(), TEXT_HTML, HTML, null),
            Arguments.of(new CehModelApplication(), APPLICATION_JSON, JSON, null),
            Arguments.of(new DataType(), TEXT_HTML, HTML, null),
            Arguments.of(new DataType(), APPLICATION_JSON, JSON, null),
            Arguments.of(new InfrastructureRecord(), TEXT_HTML, HTML, null),
            Arguments.of(new InfrastructureRecord(), APPLICATION_JSON, JSON, null),
            Arguments.of(new ResearchActivity(), TEXT_HTML, HTML, null),
            Arguments.of(new ResearchActivity(), APPLICATION_JSON, JSON, null),
            Arguments.of(gemini, TEXT_HTML, HTML, null),
            Arguments.of(gemini, APPLICATION_JSON, JSON, "gemini.json"),
            Arguments.of(gemini, GEMINI_XML, GEMINI_XML_SHORT,  "gemini.xml"),
            Arguments.of(gemini, RDF_SCHEMAORG_JSON, RDF_SCHEMAORG_SHORT, "gemini-schema-org.json"),
            Arguments.of(gemini, ROCRATE_JSON, ROCRATE_SHORT, "rocrate.json"),
            Arguments.of(gemini, ROCRATE_ATTACHED_JSON, ROCRATE_ATTACHED_SHORT, "rocrate-attached.json"),
            Arguments.of(gemini, RDF_TTL, RDF_TTL_SHORT, "gemini.ttl"),
            Arguments.of(link, TEXT_HTML, HTML, null),
            Arguments.of(link, APPLICATION_JSON, JSON, "link.json"),
            Arguments.of(new MonitoringActivity(), TEXT_HTML, HTML, null),
            Arguments.of(new MonitoringActivity(), APPLICATION_JSON, JSON, null),
            Arguments.of(new MonitoringFacility(), TEXT_HTML, HTML, null),
            Arguments.of(new MonitoringFacility(), APPLICATION_JSON, JSON, null),
            Arguments.of(new MonitoringNetwork(), TEXT_HTML, HTML, null),
            Arguments.of(new MonitoringNetwork(), APPLICATION_JSON, JSON, null),
            Arguments.of(new MonitoringProgramme(), TEXT_HTML, HTML, null),
            Arguments.of(new MonitoringProgramme(), APPLICATION_JSON, JSON, null),
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
        givenDefaultCatalogue();
        givenFreemarkerConfiguration();
        givenProfileNotActive();
        givenCodeLookup();
        givenRoCrateServiceFrom();
        givenMetadataQuality();

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
        givenDefaultCatalogue();
        givenFreemarkerConfiguration();
        givenProfileNotActive();
        givenCodeLookup();
        givenRoCrateServiceFrom();
        givenMetadataQuality();

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
    @SneakyThrows
    @DisplayName("a create response carries the new record's revision as its ETag")
    public void postEmitsETagOfCreatedDocument() {
        //Given a create that will commit as document "123-test" at a known revision
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        GeminiDocument document = new GeminiDocument();
        document.setUri("https://catalogue.ceh.ac.uk/id/123-test");
        GeminiDocument created = new GeminiDocument();
        created.setId("123-test");
        created.setUri("https://catalogue.ceh.ac.uk/id/123-test");
        given(documentRepository.saveNew(user, document, "catalogue", "new Gemini Document"))
            .willReturn(created);
        given(cachedDataRepository.getDocumentRevisionToken("123-test")).willReturn("metaRev1:rawRev1");

        //When the document is created
        ResponseEntity<MetadataDocument> actual = controller.newGeminiDocument(user, document, "catalogue");

        //Then the response carries that revision, which is the only thing the editor can use as the
        //precondition for its very next save - it holds the model in memory rather than re-reading it
        assertThat(actual.getStatusCode(), equalTo(HttpStatus.CREATED));
        assertThat("the create response must carry an ETag or the editor's next PUT has no If-Match",
            actual.getHeaders().getETag(), is("\"metaRev1:rawRev1\""));
    }

    /*
     * The journey reported from the data centre: create a record, save it, carry on editing in the same
     * open editor (ticking the service-agreement box on the admin tab) and save again. The editor never
     * re-reads the document between those two saves, so the second save's If-Match can only be the ETag
     * the create returned. When the create returns none, that second save arrives bare and is rejected
     * with 428 - and the only way out is to leave the editor and come back, forcing a fresh GET.
     */
    @Test
    @SneakyThrows
    @DisplayName("create then edit again without leaving the editor: the second save has a precondition")
    public void roundTripCreateThenPutSucceedsWithoutLeavingTheEditor() {
        //Given a create committing as "123-test" at "metaRev1:rawRev1"
        CatalogueUser user = new CatalogueUser("test", "test@example.com");
        GeminiDocument document = new GeminiDocument();
        document.setUri("https://catalogue.ceh.ac.uk/id/123-test");
        GeminiDocument created = new GeminiDocument();
        created.setId("123-test");
        created.setUri("https://catalogue.ceh.ac.uk/id/123-test");
        given(documentRepository.saveNew(user, document, "catalogue", "new Gemini Document"))
            .willReturn(created);
        given(cachedDataRepository.getDocumentRevisionToken("123-test"))
            .willReturn("metaRev1:rawRev1", "metaRev1:rawRev2");

        //When the record is created and its ETag kept, as the editor's model does
        String etagFromCreate = controller.newGeminiDocument(user, document, "catalogue")
            .getHeaders().getETag();

        //And the user carries on editing and saves again, sending that ETag back as If-Match
        GeminiDocument existing = new GeminiDocument();
        existing.setMetadata(MetadataInfo.builder().catalogue("eidc").state("draft").build());
        given(documentRepository.read("123-test")).willReturn(existing);
        GeminiDocument secondEdit = new GeminiDocument();
        given(documentRepository.save(eq(user), eq(secondEdit), eq("123-test"), any(), eq("metaRev1:rawRev1")))
            .willReturn(secondEdit);

        ResponseEntity<MetadataDocument> secondSave =
            controller.updateGeminiDocument(user, "123-test", secondEdit, etagFromCreate);

        //Then the second save is accepted rather than rejected for a missing precondition
        assertThat(secondSave.getStatusCode(), equalTo(HttpStatus.OK));
        verify(documentRepository).save(eq(user), eq(secondEdit), eq("123-test"), any(), eq("metaRev1:rawRev1"));
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
    public void cannotViewNonPublicMetadataDocumentThroughLinkDocument() throws Exception {
        //given
        HttpServletRequest request = mock(HttpServletRequest.class);
        MetadataDocument master = new GeminiDocument().setMetadata(
            MetadataInfo.builder().state("draft").build()
        );
        LinkDocument linkDocument = LinkDocument.builder().linkedDocumentId("master").original(master).build();
        given(documentRepository.read("test")).willReturn(linkDocument);

        //when
        ResponseEntity<MetadataDocument> response = controller.readMetadata(CatalogueUser.PUBLIC_USER, "test", request);
        MetadataDocument actual = response.getBody();

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

    @Test
    @SneakyThrows
    void checkCloneIsCorrect() {
        givenFreemarkerConfiguration();
        givenDefaultCatalogue();

        given(permissionService.toAccess(any(CatalogueUser.class), eq(id), eq("VIEW"))).willReturn(true);
        given(permissionService.toAccess(any(CatalogueUser.class), eq(id), eq("EDIT"))).willReturn(true);
        given(permissionService.userCanEdit(id)).willReturn(true);

        GeminiDocument source = new GeminiDocument();
        source.setId(id);
        source.setVersion(7);
        source.setMetadata(
            MetadataInfo.builder()
                .catalogue(catalogueKey)
                .state("public")
                .build()
        );
        source.setResourceIdentifiers(List.of(
            uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier.builder()
                .code("some-code")
                .codeSpace("some-space")
                .build()
        ));
        source.setIncomingCitations(List.of(
            Supplemental.builder().name("citation 1").build()
        ));

        given(documentRepository.read(id)).willReturn(source);

        given(documentRepository.saveNew(any(CatalogueUser.class), any(GeminiDocument.class), eq(catalogueKey), anyString()))
            .willAnswer(invocation -> invocation.getArgument(1));

        mvc.perform(post("/documents/{id}/clone", id))
            .andExpect(status().isSeeOther());

        ArgumentCaptor<GeminiDocument> captor = ArgumentCaptor.forClass(GeminiDocument.class);
        verify(documentRepository).saveNew(any(CatalogueUser.class), captor.capture(), eq(catalogueKey), anyString());
        GeminiDocument cloned = captor.getValue();

        assertThat("Version should be incremented", cloned.getVersion(), equalTo(8));
        assertThat("Resource identifiers should be cleared", cloned.getResourceIdentifiers(), equalTo(java.util.Collections.emptyList()));
        assertThat("Incoming citations should be cleared", cloned.getIncomingCitations(), equalTo(java.util.Collections.emptyList()));
        assertThat("Dataset reference date should be cleared", cloned.getDatasetReferenceDate(), equalTo(null));
    }

    @Test
    public void putWithoutIfMatchIsRejectedAsPreconditionRequired() {
        //Given an editor update with no If-Match header
        CatalogueUser user = new CatalogueUser("test", "test@ceh.ac.uk");
        GeminiDocument doc = new GeminiDocument();

        //When/Then updating without the precondition header is refused
        assertThrows(MetadataPreconditionRequiredException.class, () ->
            controller.updateGeminiDocument(user, "doc1", doc, null));
    }

    @Test
    public void putWithIfMatchSavesWithThatRevision() throws Exception {
        //Given a matching read for the metadata graft and a stubbed save
        CatalogueUser user = new CatalogueUser("test", "test@ceh.ac.uk");
        GeminiDocument doc = new GeminiDocument();
        GeminiDocument existing = new GeminiDocument();
        existing.setMetadata(MetadataInfo.builder().catalogue("eidc").build());
        given(documentRepository.read("doc1")).willReturn(existing);
        given(documentRepository.save(eq(user), eq(doc), eq("doc1"), any(), eq("rev1"))).willReturn(doc);

        //When updating with an If-Match
        controller.updateGeminiDocument(user, "doc1", doc, "\"rev1\"");

        //Then the (unquoted) revision is passed to the repository
        verify(documentRepository).save(eq(user), eq(doc), eq("doc1"), any(), eq("rev1"));
    }

    @Test
    public void putEmitsETagOfNewRevisionAfterSave() throws Exception {
        //Given a matching read for the metadata graft, a stubbed save, and the post-save revision
        CatalogueUser user = new CatalogueUser("test", "test@ceh.ac.uk");
        GeminiDocument doc = new GeminiDocument();
        GeminiDocument existing = new GeminiDocument();
        existing.setMetadata(MetadataInfo.builder().catalogue("eidc").build());
        GeminiDocument saved = new GeminiDocument();
        given(documentRepository.read("doc1")).willReturn(existing);
        given(documentRepository.save(eq(user), eq(doc), eq("doc1"), any(), eq("rev1"))).willReturn(saved);
        given(cachedDataRepository.getDocumentRevisionToken("doc1")).willReturn("rev2");

        //When updating with an If-Match
        ResponseEntity<MetadataDocument> response = controller.updateGeminiDocument(user, "doc1", doc, "\"rev1\"");

        //Then the response carries the NEW per-document revision as its ETag (quoted per HTTP)
        assertThat(response.getHeaders().getETag(), is("\"rev2\""));
    }

    @Test
    @SneakyThrows
    public void roundTripGetThenTwoConsecutivePutsSucceedsWithoutSpuriousConflict() {
        //Given a document readable at revision "rev1"
        HttpServletRequest request = mock(HttpServletRequest.class);
        CatalogueUser user = new CatalogueUser("test", "test@ceh.ac.uk");
        GeminiDocument existing = new GeminiDocument();
        existing.setMetadata(MetadataInfo.builder().catalogue("eidc").state("public").build());
        given(documentRepository.read("doc1")).willReturn(existing);
        given(cachedDataRepository.getDocumentRevisionToken("doc1")).willReturn("rev1", "rev2", "rev3");

        //When: a GET reads the document and its current ETag
        ResponseEntity<MetadataDocument> getResponse = controller.readMetadata(user, "doc1", request);
        String etagFromGet = getResponse.getHeaders().getETag();
        assertThat(etagFromGet, is("\"rev1\""));

        //And: a first PUT is made using that ETag as If-Match, saving successfully at "rev1"
        GeminiDocument firstEdit = new GeminiDocument();
        GeminiDocument savedAfterFirstPut = new GeminiDocument();
        given(documentRepository.save(eq(user), eq(firstEdit), eq("doc1"), any(), eq("rev1")))
            .willReturn(savedAfterFirstPut);

        ResponseEntity<MetadataDocument> firstPutResponse =
            controller.updateGeminiDocument(user, "doc1", firstEdit, etagFromGet);
        String etagFromFirstPut = firstPutResponse.getHeaders().getETag();

        //Then: the first PUT's response carries the NEW revision as its ETag
        assertThat("the PUT response must carry the new ETag for the next save to reuse",
            etagFromFirstPut, is("\"rev2\""));

        //And: a second, consecutive PUT reusing that ETag as If-Match succeeds (does NOT conflict)
        GeminiDocument secondEdit = new GeminiDocument();
        GeminiDocument savedAfterSecondPut = new GeminiDocument();
        given(documentRepository.save(eq(user), eq(secondEdit), eq("doc1"), any(), eq("rev2")))
            .willReturn(savedAfterSecondPut);

        ResponseEntity<MetadataDocument> secondPutResponse =
            controller.updateGeminiDocument(user, "doc1", secondEdit, etagFromFirstPut);

        //Then the second PUT succeeds, having sent the revision the first PUT actually returned
        assertThat(secondPutResponse.getStatusCode(), equalTo(HttpStatus.OK));
        verify(documentRepository).save(eq(user), eq(secondEdit), eq("doc1"), any(), eq("rev2"));
    }

    @Test
    public void getEmitsETagOfCurrentRevision() throws Exception {
        //Given a readable document and a known per-document revision
        HttpServletRequest request = mock(HttpServletRequest.class);
        CatalogueUser user = new CatalogueUser("test", "test@ceh.ac.uk");
        GeminiDocument doc = new GeminiDocument();
        doc.setMetadata(MetadataInfo.builder().catalogue("eidc").build());
        given(documentRepository.read("doc1")).willReturn(doc);
        given(cachedDataRepository.getDocumentRevisionToken("doc1")).willReturn("rev1");

        //When reading the document
        ResponseEntity<MetadataDocument> response = controller.readMetadata(user, "doc1", request);

        //Then the ETag carries the current revision (quoted per HTTP)
        assertThat(response.getHeaders().getETag(), is("\"rev1\""));
    }

    /*
     * The tests above call the controller directly, which proves the logic but not the wiring. These drive
     * real requests through the full MVC stack — header binding, @ExceptionHandler dispatch, status
     * mapping and message conversion of the echoed body — because that wiring is where a mandatory
     * precondition can silently degrade to an optional one.
     */

    /*
     * The link editor asks for the record as application/link+json, which content negotiation routes to
     * a handler of its own rather than the general document read. Both are entry points to the same
     * edit-then-save cycle, so both have to hand back the revision that cycle's first save needs.
     */
    @Test
    @SneakyThrows
    @WithMockCatalogueUser
    @DisplayName("reading a record as link+json carries the same ETag as reading it as gemini+json")
    void linkDocumentReadEmitsETag() {
        givenUserIsPermittedToView();
        GeminiDocument existing = new GeminiDocument();
        existing.setMetadata(MetadataInfo.builder().catalogue("eidc").state("draft").build());
        given(documentRepository.read(id)).willReturn(existing);
        given(cachedDataRepository.getDocumentRevisionToken(id)).willReturn("metaRev1:rawRev1");

        mvc.perform(get("/documents/{id}", id).accept(LINKED_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ETAG, "\"metaRev1:rawRev1\""));
    }

    @Test
    @SneakyThrows
    @WithMockCatalogueUser
    @DisplayName("a PUT with no If-Match is rejected with 428 and never reaches the repository")
    void putWithoutPreconditionIsRejectedOverHttp() {
        given(permissionService.userCanEdit(id)).willReturn(true);

        mvc.perform(put("/documents/{id}", id)
                .contentType(GEMINI_JSON_VALUE)
                .content("{\"title\":\"Edited title\"}")
            )
            .andExpect(status().isPreconditionRequired());

        verify(documentRepository, never()).save(any(), any(), anyString(), anyString(), any());
    }

    @Test
    @SneakyThrows
    @WithMockCatalogueUser
    @DisplayName("a stale PUT is rejected with 409 whose body is the submission, so the edit is not lost")
    void stalePutConflictsOverHttp() {
        given(permissionService.userCanEdit(id)).willReturn(true);
        GeminiDocument existing = new GeminiDocument();
        existing.setMetadata(MetadataInfo.builder().catalogue("eidc").build());
        given(documentRepository.read(id)).willReturn(existing);

        GeminiDocument submitted = new GeminiDocument();
        submitted.setTitle("The edit the user would otherwise lose");
        given(documentRepository.save(any(), any(), eq(id), anyString(), eq("metaRev1:rawRev1")))
            .willThrow(new MetadataConflictException("stale", submitted));

        mvc.perform(put("/documents/{id}", id)
                .contentType(GEMINI_JSON_VALUE)
                .header(HttpHeaders.IF_MATCH, "\"metaRev1:rawRev1\"")
                .accept(GEMINI_JSON_VALUE)
                .content("{\"title\":\"The edit the user would otherwise lose\"}")
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.title").value("The edit the user would otherwise lose"));
    }

    /*
     * The same conflict, from a script rather than the editor. curl sends a wildcard Accept by default,
     * as does the worked example in docs/api.md. If the 409 body cannot be written for that Accept,
     * Spring rethrows the original exception and the caller gets a 500 with no echoed submission - the
     * documented contract broken precisely when the user's unsaved edit is on the line.
     */
    @Test
    @SneakyThrows
    @WithMockCatalogueUser
    @DisplayName("a stale PUT from a client sending no Accept header still gets the 409 and its body")
    void stalePutConflictsForClientsWithoutAnAcceptHeader() {
        given(permissionService.userCanEdit(id)).willReturn(true);
        GeminiDocument existing = new GeminiDocument();
        existing.setMetadata(MetadataInfo.builder().catalogue("eidc").build());
        given(documentRepository.read(id)).willReturn(existing);

        GeminiDocument submitted = new GeminiDocument();
        submitted.setTitle("The edit the user would otherwise lose");
        given(documentRepository.save(any(), any(), eq(id), anyString(), eq("metaRev1:rawRev1")))
            .willThrow(new MetadataConflictException("stale", submitted));

        mvc.perform(put("/documents/{id}", id)
                .contentType(GEMINI_JSON_VALUE)
                .header(HttpHeaders.IF_MATCH, "\"metaRev1:rawRev1\"")
                .content("{\"title\":\"The edit the user would otherwise lose\"}")
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.title").value("The edit the user would otherwise lose"));
    }

    @Test
    @SneakyThrows
    @WithMockCatalogueUser
    @DisplayName("a PUT with a current If-Match succeeds and returns the next revision as its ETag")
    void currentPutSucceedsOverHttp() {
        given(permissionService.userCanEdit(id)).willReturn(true);
        GeminiDocument existing = new GeminiDocument();
        givenMetadataDocument(existing);
        givenCatalogue();
        givenDefaultCatalogue();
        givenFreemarkerConfiguration();
        givenProfileNotActive();
        givenCodeLookup();
        givenMetadataQuality();
        existing.setMetadata(MetadataInfo.builder().catalogue("eidc").build());
        given(documentRepository.read(id)).willReturn(existing);
        given(documentRepository.save(any(), any(), eq(id), anyString(), eq("metaRev1:rawRev1")))
            .willReturn(existing);
        given(cachedDataRepository.getDocumentRevisionToken(id)).willReturn("metaRev1:rawRev2");

        mvc.perform(put("/documents/{id}", id)
                .contentType(GEMINI_JSON_VALUE)
                .header(HttpHeaders.IF_MATCH, "\"metaRev1:rawRev1\"")
                .content("{\"title\":\"Edited title\"}")
            )
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ETAG, "\"metaRev1:rawRev2\""));
    }

    @Test
    @SneakyThrows
    @WithMockCatalogueUser
    @DisplayName("If-Match: * satisfies the precondition without pinning a revision")
    void wildcardPreconditionIsAcceptedOverHttp() {
        given(permissionService.userCanEdit(id)).willReturn(true);
        GeminiDocument existing = new GeminiDocument();
        givenMetadataDocument(existing);
        givenCatalogue();
        givenDefaultCatalogue();
        givenFreemarkerConfiguration();
        givenProfileNotActive();
        givenCodeLookup();
        givenMetadataQuality();
        existing.setMetadata(MetadataInfo.builder().catalogue("eidc").build());
        given(documentRepository.read(id)).willReturn(existing);
        given(documentRepository.save(any(), any(), eq(id), anyString(), eq(null)))
            .willReturn(existing);

        mvc.perform(put("/documents/{id}", id)
                .contentType(GEMINI_JSON_VALUE)
                .header(HttpHeaders.IF_MATCH, "*")
                .content("{\"title\":\"Edited title\"}")
            )
            .andExpect(status().isOk());

        // null expected-revision is what tells the repository to skip the compare-then-commit check
        verify(documentRepository).save(any(), any(), eq(id), anyString(), eq(null));
    }
}
