package uk.ac.ceh.gateway.catalogue.controllers;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.imp.Model;
import uk.ac.ceh.gateway.catalogue.model.*;
import uk.ac.ceh.gateway.catalogue.permission.PermissionService;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.services.CatalogueService;
import uk.ac.ceh.gateway.catalogue.services.CodeLookupService;
import uk.ac.ceh.gateway.catalogue.services.JenaLookupService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uk.ac.ceh.gateway.catalogue.config.CatalogueMediaTypes.*;
import static uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig.EIDC_PUBLISHER_USERNAME;

@ActiveProfiles("test")
@DisplayName("DocumentController")
@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})
@WebMvcTest(
    controllers=DocumentController.class,
    properties="spring.freemarker.template-loader-path=file:../templates"
)
class DocumentControllerTest {
    @MockBean private CatalogueService catalogueService;
    @MockBean private CodeLookupService codeLookupService;
    @MockBean private DocumentRepository documentRepository;
    @MockBean private JenaLookupService jenaLookupService;
    @MockBean(name="permission") private PermissionService permissionService;

    @Autowired private MockMvc mockMvc;
    @Autowired private Configuration configuration;

    private DocumentController controller;
    private final String linkedDocumentId = "0a6c7c4c-0515-40a8-b84e-7ffe622b2579";
    private final String id = "M3tADATA_ID";
    private final String catalogueKey = "eidc";

    @BeforeEach
    void setup() {
        controller = new DocumentController(documentRepository);
    }

    private void givenUserIsPermittedToView() {
        given(permissionService.toAccess(any(CatalogueUser.class), eq(id), eq("VIEW")))
            .willReturn(true);
    }

    @SneakyThrows
    private void givenGeminiDocument() {
        val gemini = new GeminiDocument();
        gemini.setId(id);
        val metadata = MetadataInfo.builder()
            .catalogue(catalogueKey)
            .state("public")
            .build();
        gemini.setMetadata(metadata);
        gemini.setUri("https://example.com/" + id);
        gemini.setTitle("Example Gemini Document");
        gemini.setType("dataset");
        val bbox = BoundingBox.builder()
            .northBoundLatitude("59.456")
            .eastBoundLongitude("2.574")
            .southBoundLatitude("31.109")
            .westBoundLongitude("-1.091")
            .build();
        gemini.setBoundingBoxes(Collections.singletonList(bbox));
        given(documentRepository.read(id)).willReturn(gemini);
    }

    @SneakyThrows
    private void givenFreemarkerConfiguration() {
        configuration.setSharedVariable("catalogues", catalogueService);
        configuration.setSharedVariable("codes", codeLookupService);
        configuration.setSharedVariable("jena", jenaLookupService);
        configuration.setSharedVariable("permission", permissionService);
    }

    private void givenCatalogue() {
        given(catalogueService.retrieve(catalogueKey))
            .willReturn(
                Catalogue.builder()
                    .id(catalogueKey)
                    .title("Env Data Centre")
                    .url("https://example.com")
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
    public String expectedResponse(String filename) {
        return StreamUtils.copyToString(
            getClass().getResourceAsStream(filename),
            StandardCharsets.UTF_8
        );
    }

    @Test
    @SneakyThrows
    void getGeminiDocumentAsHtml() {
        //given
        givenUserIsPermittedToView();
        givenGeminiDocument();
        givenFreemarkerConfiguration();
        givenCatalogue();
        givenCodeLookup();

        //when
        mockMvc.perform(
            get("/documents/{id}", id)
            .accept(MediaType.TEXT_HTML)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.TEXT_HTML));
    }

    @Test
    @SneakyThrows
    void getGeminiDocumentAsJson() {
        //given
        givenUserIsPermittedToView();
        givenGeminiDocument();

        //when
        mockMvc.perform(
            get("/documents/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedResponse("gemini.json")));
    }

    @Test
    @SneakyThrows
    void getGeminiDocumentAsJsonUsingQueryParam() {
        //given
        givenUserIsPermittedToView();
        givenGeminiDocument();

        //when
        mockMvc.perform(
            get("/documents/{id}", id)
                .queryParam("format", "json")
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedResponse("gemini.json")));
    }

    @Test
    @SneakyThrows
    void getGeminiDocumentAsSchemaOrgUsingAccept() {
        //given
        givenUserIsPermittedToView();
        givenGeminiDocument();

        //when
        mockMvc.perform(
            get("/documents/{id}", id)
                .accept(RDF_SCHEMAORG_VALUE)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(RDF_SCHEMAORG_VALUE))
            .andExpect(content().json(expectedResponse("gemini-schema-org.json")));
    }

    @Test
    @SneakyThrows
    void getGeminiDocumentAsSchemaOrgUsingQueryParam() {
        //given
        givenUserIsPermittedToView();
        givenGeminiDocument();

        //when
        mockMvc.perform(
            get("/documents/{id}", id)
                .queryParam("format", RDF_SCHEMAORG_SHORT)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(RDF_SCHEMAORG_VALUE))
            .andExpect(content().json(expectedResponse("gemini-schema-org.json")));
    }

    @Test
    @SneakyThrows
    void getGeminiDocumentAsRdfTurtleUsingQueryParam() {
        //given
        givenUserIsPermittedToView();
        givenGeminiDocument();
        givenFreemarkerConfiguration();

        //when
        mockMvc.perform(
            get("/documents/{id}", id)
                .queryParam("format", RDF_TTL_SHORT)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(RDF_TTL_VALUE))
            .andExpect(content().string(expectedResponse("gemini.ttl")));
    }

    @Test
    @SneakyThrows
    void getGeminiDocumentAsInspireXmlUsingQueryParam() {
        //given
        givenUserIsPermittedToView();
        givenGeminiDocument();
        givenFreemarkerConfiguration();

        //when
        mockMvc.perform(
            get("/documents/{id}", id)
                .queryParam("format", GEMINI_SHORT)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(GEMINI_XML_VALUE));
    }

    @Test
    @SneakyThrows
    void checkItCanRewriteIdToDocumentWithFileExtension() {
        //When
        mockMvc.perform(
            get("/id/{id}.xml", id)
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(header().string("location", "https://localhost/documents/M3tADATA_ID.xml"));
    }

    @Test
    @DisplayName("Redirect URL has query string parameters")
    @SneakyThrows
    public void redirectWithQueryString() {
        //When
        mockMvc.perform(
            get("/id/{id}", id)
            .queryParam("query", "string")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(header().string("location", "https://localhost/documents/M3tADATA_ID?query=string"));
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
        mockMvc.perform(
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
        CatalogueUser user = new CatalogueUser();
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
        CatalogueUser user = new CatalogueUser();
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
        CatalogueUser user = new CatalogueUser();
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
        CatalogueUser user = new CatalogueUser();
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
        CatalogueUser user = new CatalogueUser();
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
        CatalogueUser user = new CatalogueUser();
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
        MetadataDocument master = new GeminiDocument().setMetadata(
            MetadataInfo.builder().state("draft").build()
        );
        LinkDocument linkDocument = LinkDocument.builder().linkedDocumentId("master").original(master).build();
        given(documentRepository.read("test")).willReturn(linkDocument);

        //when
        MetadataDocument actual = controller.readMetadata(CatalogueUser.PUBLIC_USER, "test");

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
        CatalogueUser user = CatalogueUser.PUBLIC_USER;
        String file = "myFile";
        MetadataInfo info = MetadataInfo.builder().build();
        MetadataDocument document = new GeminiDocument();
        document.setMetadata(info);
        given(documentRepository.read(file))
            .willReturn(document);

        //When
        controller.readMetadata(user, file);

        //Then
        verify(documentRepository).read(file);
    }
}
