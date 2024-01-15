package uk.ac.ceh.gateway.catalogue.datacite;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.gateway.catalogue.gemini.DatasetReferenceDate;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static uk.ac.ceh.gateway.catalogue.model.MetadataInfo.PUBLIC_GROUP;

@ExtendWith(MockitoExtension.class)
public class DataciteServiceTest {
    private static final String ID = "d4bdc836-5b89-44c5-aca2-2880a5d5a5be";

    private DataciteService service;
    private MockRestServiceServer mockServer;
    @Mock
    DocumentIdentifierService identifierService;
    Configuration configuration;
    String doiPrefix = "10.8268";

    @BeforeEach
    @SneakyThrows
    public void init() {
        val restTemplate = new RestTemplate();
        configuration = new Configuration(Configuration.VERSION_2_3_23);
        configuration.setDirectoryForTemplateLoading(new File("../templates"));
        service = new DataciteService(
                "https://example.com/doi",
                doiPrefix,
                "Test publisher",
                "Test legacy publisher",
                "username",
                "password",
                "datacite/datacite.ftlx",
                identifierService,
                configuration,
                restTemplate
        );
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void checkThatIsDataciteUpdatableIfEverythingIsPresent() {
        //Given
        ResponsibleParty author = ResponsibleParty.builder().role("author").build();
        ResponsibleParty publisher = ResponsibleParty.builder().role("publisher").organisationName("Test publisher").build();
        MetadataInfo metadata = MetadataInfo.builder().state("published").build();
        metadata.addPermission(Permission.VIEW, PUBLIC_GROUP);
        GeminiDocument document = new GeminiDocument();
        document.setResponsibleParties(Arrays.asList(author, publisher));
        document.setDatasetReferenceDate(DatasetReferenceDate.builder().publicationDate(LocalDate.of(2010, Month.MARCH, 2)).build());
        document.setTitle("Title");
        document.setMetadata(metadata);

        //When
        boolean dataciteUpdatable = service.isDatacitable(document, false);

        //Then
        assertTrue(dataciteUpdatable);
        verifyNoInteractions(identifierService);
    }

    @Test
    public void checkThatIsNotDatacitableIfPublicationDateIsInFuture() {
        //Given
        ResponsibleParty author = ResponsibleParty.builder().role("author").build();
        ResponsibleParty publisher = ResponsibleParty.builder().role("publisher").organisationName("Test publisher").build();
        MetadataInfo metadata = MetadataInfo.builder().state("published").build();
        metadata.addPermission(Permission.VIEW, PUBLIC_GROUP);
        GeminiDocument document = new GeminiDocument();
        document.setResponsibleParties(Arrays.asList(author, publisher));
        document.setDatasetReferenceDate(DatasetReferenceDate.builder().publicationDate(LocalDate.of(2110, Month.MARCH, 2)).build());
        document.setTitle("Title");
        document.setMetadata(metadata);

        //When
        boolean dataciteUpdatable = service.isDatacitable(document, false);

        //Then
        assertFalse(dataciteUpdatable);
        verifyNoInteractions(identifierService);
    }

    @Test
    public void checkThatIsDatacitableIfPublicationDateIsToday() {
        //Given
        ResponsibleParty author = ResponsibleParty.builder().role("author").build();
        ResponsibleParty publisher = ResponsibleParty.builder().role("publisher").organisationName("Test publisher").build();
        MetadataInfo metadata = MetadataInfo.builder().state("published").build();
        metadata.addPermission(Permission.VIEW, PUBLIC_GROUP);
        GeminiDocument document = new GeminiDocument();
        document.setResponsibleParties(Arrays.asList(author, publisher));
        document.setDatasetReferenceDate(DatasetReferenceDate.builder().publicationDate(LocalDate.now()).build());
        document.setTitle("Title");
        document.setMetadata(metadata);

        //When
        boolean dataciteUpdatable = service.isDatacitable(document, false);

        //Then
        assertTrue(dataciteUpdatable);
        verifyNoInteractions(identifierService);
    }

    @Test
    public void checkThatIsDatacitableIfPublisherIsLegacy() {
        //Given
        ResponsibleParty author = ResponsibleParty.builder().role("author").build();
        ResponsibleParty publisher = ResponsibleParty.builder().role("publisher").organisationName("Test legacy publisher").build();
        MetadataInfo metadata = MetadataInfo.builder().state("published").build();
        metadata.addPermission(Permission.VIEW, PUBLIC_GROUP);
        GeminiDocument document = new GeminiDocument();
        document.setResponsibleParties(Arrays.asList(author, publisher));
        document.setDatasetReferenceDate(DatasetReferenceDate.builder().publicationDate(LocalDate.now()).build());
        document.setTitle("Title");
        document.setMetadata(metadata);

        //When
        boolean dataciteUpdatable = service.isDatacitable(document, true);

        //Then
        assertTrue(dataciteUpdatable);
        verifyNoInteractions(identifierService);
    }

    @Test
    public void checkThatIsDatacitableIfPublisherIsNormal() {
        //Given
        ResponsibleParty author = ResponsibleParty.builder().role("author").build();
        ResponsibleParty publisher = ResponsibleParty.builder().role("publisher").organisationName("Test publisher").build();
        MetadataInfo metadata = MetadataInfo.builder().state("published").build();
        metadata.addPermission(Permission.VIEW, PUBLIC_GROUP);
        GeminiDocument document = new GeminiDocument();
        document.setResponsibleParties(Arrays.asList(author, publisher));
        document.setDatasetReferenceDate(DatasetReferenceDate.builder().publicationDate(LocalDate.now()).build());
        document.setTitle("Title");
        document.setMetadata(metadata);

        //When
        boolean dataciteUpdatable = service.isDatacitable(document, true);

        //Then
        assertTrue(dataciteUpdatable);
        verifyNoInteractions(identifierService);
    }

    @Test
    public void checkThatIsNotDatacitableIfPublisherIsLegacy() {
        //Given
        ResponsibleParty author = ResponsibleParty.builder().role("author").build();
        ResponsibleParty publisher = ResponsibleParty.builder().role("publisher").organisationName("Test legacy publisher").build();
        MetadataInfo metadata = MetadataInfo.builder().state("published").build();
        metadata.addPermission(Permission.VIEW, PUBLIC_GROUP);
        GeminiDocument document = new GeminiDocument();
        document.setResponsibleParties(Arrays.asList(author, publisher));
        document.setDatasetReferenceDate(DatasetReferenceDate.builder().publicationDate(LocalDate.now()).build());
        document.setTitle("Title");
        document.setMetadata(metadata);

        //When
        boolean dataciteUpdatable = service.isDatacitable(document, false);

        //Then
        assertFalse(dataciteUpdatable);
        verifyNoInteractions(identifierService);
    }

    @Test
    public void checkThatIsNotDataciteUpdatableIfRequirementsAreNotMet() {
        //Given
        GeminiDocument document = new GeminiDocument();
        document.setMetadata(MetadataInfo.builder().build());

        //When
        boolean dataciteUpdatable = service.isDatacitable(document, false);

        //Then
        assertFalse(dataciteUpdatable);
        verifyNoInteractions(identifierService);
    }

    @Test
    @SneakyThrows
    public void updatesDoiMetadata() {
        //Given
        val document = getGeminiDocument();
        when(identifierService.generateUri(ID)).thenReturn("https://catalogue.ceh.ac.uk/id/" + ID);
        val encoded = IOUtils.toString(Objects.requireNonNull(getClass().getResourceAsStream("encoded.txt")), StandardCharsets.UTF_8);

        // TODO: in future could look at the xml content sent to Datacite
        mockServer
                .expect(requestTo("https://example.com/doi/10.8268/d4bdc836-5b89-44c5-aca2-2880a5d5a5be"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(content().contentTypeCompatibleWith(MediaType.valueOf("application/vnd.api+json")))
                .andExpect(jsonPath("$.data.id").value(doiPrefix + "/" + ID))
                .andExpect(jsonPath("$.data.attributes.doi").value(doiPrefix + "/" + ID))
                .andExpect(jsonPath("$.data.attributes.url").value("https://catalogue.ceh.ac.uk/id/" + ID))
                .andExpect(jsonPath("$.data.attributes.xml").value(encoded))
                .andRespond(withSuccess());

        //When
        service.updateDoiMetadata(document);

        //Then
        mockServer.verify();
        verify(identifierService).generateUri(ID);
    }

    @Test
    @SneakyThrows
    public void updatesDoiMetadataLegacy() {
        //Given
        val document = getGeminiDocumentWithLegacyPublisher();
        when(identifierService.generateUri(ID)).thenReturn("https://catalogue.ceh.ac.uk/id/" + ID);
        val encoded = IOUtils.toString(Objects.requireNonNull(getClass().getResourceAsStream("encoded.txt")), StandardCharsets.UTF_8);

        // TODO: in future could look at the xml content sent to Datacite
        mockServer
                .expect(requestTo("https://example.com/doi/10.8268/d4bdc836-5b89-44c5-aca2-2880a5d5a5be"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(content().contentTypeCompatibleWith(MediaType.valueOf("application/vnd.api+json")))
                .andExpect(jsonPath("$.data.id").value(doiPrefix + "/" + ID))
                .andExpect(jsonPath("$.data.attributes.doi").value(doiPrefix + "/" + ID))
                .andExpect(jsonPath("$.data.attributes.url").value("https://catalogue.ceh.ac.uk/id/" + ID))
                .andExpect(jsonPath("$.data.attributes.xml").value(encoded))
                .andRespond(withSuccess());

        //When
        service.updateDoiMetadata(document);

        //Then
        mockServer.verify();
        verify(identifierService).generateUri(ID);
    }


    @Test
    @SneakyThrows
    public void generateDoi() {
        //Given
        val document = getGeminiDocument();
        when(identifierService.generateUri(ID)).thenReturn("https://catalogue.ceh.ac.uk/id/" + ID);
        val encoded = IOUtils.toString(Objects.requireNonNull(getClass().getResourceAsStream("encoded.txt")), StandardCharsets.UTF_8);

        mockServer
                .expect(requestTo("https://example.com/doi"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.valueOf("application/vnd.api+json")))
                .andExpect(jsonPath("$.data.id").value(doiPrefix + "/" + ID))
                .andExpect(jsonPath("$.data.attributes.doi").value(doiPrefix + "/" + ID))
                .andExpect(jsonPath("$.data.attributes.url").value("https://catalogue.ceh.ac.uk/id/" + ID))
                .andExpect(jsonPath("$.data.attributes.xml").value(encoded))
                .andRespond(withSuccess());

        //When
        service.generateDoi(document);

        //Then
        mockServer.verify();
        verify(identifierService).generateUri(ID);
    }

    private GeminiDocument getGeminiDocument(){
        val author = ResponsibleParty.builder().role("author").build();
        val publisher = ResponsibleParty.builder().role("publisher").organisationName("Test publisher").build();
        val metadata = MetadataInfo.builder().state("published").build();
        metadata.addPermission(Permission.VIEW, PUBLIC_GROUP);
        val document = new GeminiDocument();
        document.setDescription("This is the description");
        document.setResponsibleParties(Arrays.asList(author, publisher));
        document.setDatasetReferenceDate(DatasetReferenceDate.builder().publicationDate(LocalDate.of(2010, Month.MARCH, 2)).build());
        document.setTitle("Title");
        document.setMetadata(metadata);
        document.setId(ID);

        return document;
    }

    private GeminiDocument getGeminiDocumentWithLegacyPublisher(){
        val author = ResponsibleParty.builder().role("author").build();
        val publisher = ResponsibleParty.builder().role("publisher").organisationName("Test legacy publisher").build();
        val metadata = MetadataInfo.builder().state("published").build();
        metadata.addPermission(Permission.VIEW, PUBLIC_GROUP);
        val document = new GeminiDocument();
        document.setDescription("This is the description");
        document.setResponsibleParties(Arrays.asList(author, publisher));
        document.setDatasetReferenceDate(DatasetReferenceDate.builder().publicationDate(LocalDate.of(2010, Month.MARCH, 2)).build());
        document.setTitle("Title");
        document.setMetadata(metadata);
        document.setId(ID);

        return document;
    }

    @Test
    public void canGetDoiMetadata() {
        //given
        val document = getGeminiDocument();
        document.setResourceIdentifiers(Collections.singletonList(
            ResourceIdentifier.builder().codeSpace("doi:").code(doiPrefix + "/" + ID).build()
        ));
        mockServer.expect(requestTo("https://example.com/doi/10.8268/d4bdc836-5b89-44c5-aca2-2880a5d5a5be"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("accept", "application/vnd.datacite.datacite+xml"))
            .andRespond(withSuccess());

        //when
        service.getDoiMetadata(document);

        //then
        mockServer.verify();
    }
}
