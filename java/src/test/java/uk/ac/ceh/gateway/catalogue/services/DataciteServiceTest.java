package uk.ac.ceh.gateway.catalogue.services;

import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.gateway.catalogue.gemini.DatasetReferenceDate;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static uk.ac.ceh.gateway.catalogue.model.MetadataInfo.PUBLIC_GROUP;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class DataciteServiceTest {
    private DataciteService service;
    private MockRestServiceServer mockServer;
    @Mock
    DocumentIdentifierService identifierService;
    @Mock
    Configuration configuration;

    @Before
    @SneakyThrows
    public void init() {
        val restTemplate = new RestTemplate();
        service = new DataciteService(
                "https://example.com/dois",
                "10.8268/",
                "Test publisher",
                "username",
                "password",
                "datacite/datacite.ftl",
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
        boolean dataciteUpdatable = service.isDatacitable(document);

        //Then
        assertTrue("Expected document to be updatable", dataciteUpdatable);
        verifyZeroInteractions(configuration, identifierService);
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
        boolean dataciteUpdatable = service.isDatacitable(document);

        //Then
        assertFalse("Expected document to not be updatable", dataciteUpdatable);
        verifyZeroInteractions(configuration, identifierService);
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
        boolean dataciteUpdatable = service.isDatacitable(document);

        //Then
        assertTrue("Expected document to be updatable", dataciteUpdatable);
        verifyZeroInteractions(configuration, identifierService);
    }

    @Test
    public void checkThatIsNotDataciteUpdatableIfRequirementsAreNotMet() {
        //Given
        GeminiDocument document = new GeminiDocument();
        document.setMetadata(MetadataInfo.builder().build());

        //When
        boolean dataciteUpdatable = service.isDatacitable(document);

        //Then
        assertFalse("Expected document to not be updatable", dataciteUpdatable);
        verifyZeroInteractions(configuration, identifierService);
    }

    @Test
    @SneakyThrows
    public void checkThatPostsToRestEndpointWhenValid() {
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
        given(configuration.getTemplate("datacite/datacite.ftl")).willReturn(mock(Template.class));

        // TODO: in future could look at the xml content sent to Datacite
        mockServer
                .expect(requestTo("https://example.com/dois"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(content().contentTypeCompatibleWith(MediaType.valueOf("application/vnd.api+json")))
                .andRespond(withSuccess());

        //When
        service.updateDoiMetadata(document);

        //Then
        mockServer.verify();
    }


    @Test
    @SneakyThrows
    public void checkThatPostsToDoiMintEndpointWhenValid() {
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
        when(identifierService.generateUri("MY_ID")).thenReturn("http://ceh.com");
        document.setId("MY_ID");
        given(configuration.getTemplate("datacite/datacite.ftl")).willReturn(mock(Template.class));

        mockServer
                .expect(requestTo("https://example.com/dois"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.valueOf("application/vnd.api+json")))
                .andExpect(content().string("{\"id\":\"10.8268/MY_ID\",\"type\":\"dois\",\"atttributes\":{\"event\":\"publish\",\"url\":\"https://schema.datacite.org/meta/kernel-4.0/index.html\",\"xml\":\"\"}}"))
                .andRespond(withSuccess());

        //When
        service.mintDoiRequest(document);

        //Then
        mockServer.verify();
    }
}