package uk.ac.ceh.gateway.catalogue.services;

import freemarker.template.Template;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.Matchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.gateway.catalogue.gemini.DatasetReferenceDate;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import static uk.ac.ceh.gateway.catalogue.model.MetadataInfo.PUBLIC_GROUP;
import uk.ac.ceh.gateway.catalogue.model.Permission;

/**
 *
 * @author cjohn
 */
public class DataciteServiceTest {
    private DataciteService service;
    
    @Mock Template dataciteRequest;
    @Mock RestTemplate rest;
    @Mock DocumentIdentifierService identifierService;
    
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        service = new DataciteService("10.8268/", "Test publisher", "username", "password", identifierService, dataciteRequest, rest);
    }
    
    @Test
    public void checkValidAuthentication() {
        //Given
        //Nothing
        
        //When
        HttpHeaders headers = service.getBasicAuth();
        
        //Then
        assertTrue("Expected authorization header", headers.containsKey("Authorization"));
        assertThat("Expected authorization header", headers.get("Authorization"), contains("Basic dXNlcm5hbWU6cGFzc3dvcmQ="));
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
        assertTrue("Expected document to be updateable", dataciteUpdatable);        
    }
    
    @Test
    public void checkThatIsntDatacitableIfPublicationDateIsInFuture() {
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
        assertFalse("Expected document to not be updateable", dataciteUpdatable);        
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
        assertTrue("Expected document to be updateable", dataciteUpdatable);     
    }
    
    @Test
    public void checkThatIsNotDataciteUpdatableIfRequirementsArntMet() {
        //Given
        GeminiDocument document = new GeminiDocument();
        document.setMetadata(MetadataInfo.builder().build());
        
        //When
        boolean dataciteUpdatable = service.isDatacitable(document);
        
        //Then
        assertFalse("Expected document to not be updateable", dataciteUpdatable);        
    }
    
    @Test
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
        
        //When
        ResourceIdentifier dataciteUpdatable = service.updateDoiMetadata(document);
        
        //Then
        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(rest).postForEntity(eq("https://mds.datacite.org/metadata"), captor.capture(), eq(String.class));
        assertThat("correct content type", captor.getValue().getHeaders().getContentType(), equalTo(MediaType.valueOf("application/xml;charset=UTF-8")));
    }
    
    
    @Test
    public void checkThatPostsToDoiMintEndpointWhenValid() throws URISyntaxException {
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
        
        //When
        service.mintDoiRequest(document);
        
        //Then
        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(rest).postForEntity(eq("https://mds.datacite.org/doi"), captor.capture(), eq(String.class));
        assertThat("correct content type", captor.getValue().getHeaders().getContentType(), equalTo(MediaType.TEXT_PLAIN));
        assertThat("correct body", captor.getValue().getBody(), equalTo("doi=10.8268/MY_ID\nurl=http://ceh.com"));
    }
}
