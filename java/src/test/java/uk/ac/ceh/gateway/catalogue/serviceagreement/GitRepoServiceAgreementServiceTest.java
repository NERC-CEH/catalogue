package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.components.datastore.DataDocument;
import uk.ac.ceh.components.datastore.DataOngoingCommit;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.gateway.catalogue.document.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.document.reading.DocumentTypeLookupService;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceConstraint;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.JiraService;

import java.io.ByteArrayInputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SuppressWarnings({"unchecked", "rawtypes"})
@Slf4j
@ExtendWith(MockitoExtension.class)
public class GitRepoServiceAgreementServiceTest {

    private static final String FOLDER = "service-agreements/";
    private static final String ID = "test";

    @Mock
    DocumentTypeLookupService documentTypeLookupService;
    @Mock
    private DataRepository<CatalogueUser> repo;
    @Mock
    private DocumentInfoMapper<MetadataInfo> metadataInfoMapper;
    @Mock
    private DocumentInfoMapper<ServiceAgreement> serviceAgreementMapper;
    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private JiraService jiraService;

    private ServiceAgreementService service;

    @BeforeEach
    void setup() {
        service = new GitRepoServiceAgreementService(
            documentTypeLookupService,
            repo,
            metadataInfoMapper,
            serviceAgreementMapper,
            documentRepository,
            jiraService

        );
    }

    @Test
    @SneakyThrows
    public void canGet() {
        //Given
        val serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(ID);

        val metadataInfoDocument = mock(DataDocument.class);
        given(repo.getData(FOLDER + ID + ".meta"))
            .willReturn(metadataInfoDocument);
        given(metadataInfoDocument.getInputStream())
            .willReturn(new ByteArrayInputStream("meta".getBytes()));
        val metadata = MetadataInfo.builder()
            .rawType(APPLICATION_JSON_VALUE)
            .build();
        given(metadataInfoMapper.readInfo(any()))
            .willReturn(metadata);

        val rawDocument = mock(DataDocument.class);
        given(repo.getData(FOLDER + ID + ".raw"))
            .willReturn(rawDocument);
        given(rawDocument.getInputStream())
            .willReturn(new ByteArrayInputStream("file".getBytes()));
        given(serviceAgreementMapper.readInfo(any()))
            .willReturn(serviceAgreement);

        //When
        service.get(ID);

        //Then
    }

    @Test
    @SneakyThrows
    public void canNotGetRaw() {
        //Given
        val serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(ID);

        val metadataInfoDocument = mock(DataDocument.class);
        given(repo.getData(FOLDER + ID + ".meta"))
                .willReturn(metadataInfoDocument);

        given(repo.getData(FOLDER + ID + ".raw"))
                .willThrow(new DataRepositoryException("Fail"));

        //When
        assertThrows(DataRepositoryException.class, () ->
                service.get(ID));
    }

    @Test
    @SneakyThrows
    public void canNotGetMeta() {
        //Given
        val serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(ID);

        given(repo.getData(FOLDER + ID + ".meta"))
                .willThrow(new DataRepositoryException("Fail"));

        //When
        assertThrows(DataRepositoryException.class, () ->
                service.get(ID));
    }

    @Test
    @SneakyThrows
    public void canDelete() {
        //Given
        CatalogueUser user = new CatalogueUser();
        user.setUsername("test");
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(ID);


        DataOngoingCommit<CatalogueUser> dataOngoingCommit = mock(DataOngoingCommit.class);
        given(repo.deleteData(FOLDER + ID + ".meta")).willReturn(dataOngoingCommit);
        given(dataOngoingCommit.deleteData(any())).willReturn(dataOngoingCommit);

        //When
        service.delete(user, ID);

        //Then
        verify(dataOngoingCommit).commit(user, "delete document: test");
    }

    @Test
    @SneakyThrows
    public void metadataRecordExists() {
        //Given
        CatalogueUser user = new CatalogueUser();
        user.setUsername("test");
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(ID);

        DataDocument dataDocument = mock(DataDocument.class);
        given(repo.getData(ID + ".meta")).willReturn(dataDocument);

        //When
        boolean result = service.metadataRecordExists(ID);

        //Then
        assertThat(result, is(equalTo(true)));
    }

    @Test
    @SneakyThrows
    public void metadataRecordDoesNotExist() {
        //Given
        CatalogueUser user = new CatalogueUser();
        user.setUsername("test");
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(ID);

        given(repo.getData(ID + ".meta")).willThrow(new DataRepositoryException("failed"));

        //When
        boolean result = service.metadataRecordExists(ID);

        assertThat(result, is(false));
    }

    @Test
    @SneakyThrows
    public void canSave() {
        //Given
        CatalogueUser user = new CatalogueUser();
        user.setUsername("test");
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(ID);
        DataOngoingCommit dataOngoingCommit = mock(DataOngoingCommit.class);
        MetadataInfo metadataInfo = MetadataInfo.builder().build();

        given(repo.submitData(any(), any())).willReturn(dataOngoingCommit);
        given(dataOngoingCommit.submitData(any(), any())).willReturn(dataOngoingCommit);

        //When
        service.save(user, ID, "catalogue", serviceAgreement, metadataInfo);

        //Then
        verify(dataOngoingCommit).commit(user, "catalogue");
    }

    @Test
    @SneakyThrows
    public void canPopulateGeminiDocument() {
        //Given
        CatalogueUser user = new CatalogueUser();
        user.setUsername("test");
        val serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(ID);
        serviceAgreement.setEndUserLicence(new ResourceConstraint("test", "test", "test"));
        GeminiDocument geminiDocument = new GeminiDocument(serviceAgreement);

        val metadataInfoDocument = mock(DataDocument.class);
        given(repo.getData(FOLDER + ID + ".meta"))
                .willReturn(metadataInfoDocument);
        given(metadataInfoDocument.getInputStream())
                .willReturn(new ByteArrayInputStream("meta".getBytes()));
        val metadata = MetadataInfo.builder()
                .rawType(APPLICATION_JSON_VALUE)
                .build();
        given(metadataInfoMapper.readInfo(any()))
                .willReturn(metadata);

        val rawDocument = mock(DataDocument.class);
        given(repo.getData(FOLDER + ID + ".raw"))
                .willReturn(rawDocument);
        given(rawDocument.getInputStream())
                .willReturn(new ByteArrayInputStream("file".getBytes()));
        given(serviceAgreementMapper.readInfo(any()))
                .willReturn(serviceAgreement);

        val metadataDocument = mock(MetadataDocument.class);
        given(documentRepository.save(user, geminiDocument, "catalogue"))
                .willReturn(metadataDocument);


        //When
        service.populateGeminiDocument(user, ID);

        //Then
        verify(documentRepository).save(user,geminiDocument, "catalogue");
    }

    @Test
    @SneakyThrows
    public void canPublishServiceAgreement() {
        //Given
        CatalogueUser user = new CatalogueUser();
        user.setUsername("test");
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(ID);


        DataOngoingCommit<CatalogueUser> dataOngoingCommit = mock(DataOngoingCommit.class);
        given(repo.deleteData(FOLDER + ID + ".meta")).willReturn(dataOngoingCommit);
        given(dataOngoingCommit.deleteData(any())).willReturn(dataOngoingCommit);

        val metadataInfoDocument = mock(DataDocument.class);
        given(repo.getData(FOLDER + ID + ".meta"))
                .willReturn(metadataInfoDocument);
        given(metadataInfoDocument.getInputStream())
                .willReturn(new ByteArrayInputStream("meta".getBytes()));
        val metadata = MetadataInfo.builder()
                .rawType(APPLICATION_JSON_VALUE)
                .build();
        given(metadataInfoMapper.readInfo(any()))
                .willReturn(metadata);

        val rawDocument = mock(DataDocument.class);
        given(repo.getData(FOLDER + ID + ".raw"))
                .willReturn(rawDocument);
        given(rawDocument.getInputStream())
                .willReturn(new ByteArrayInputStream("file".getBytes()));
        given(serviceAgreementMapper.readInfo(any()))
                .willReturn(serviceAgreement);

        //When
        service.publishServiceAgreement(user, ID);

        //Then
        verify(dataOngoingCommit).commit(user, "delete document: test");
    }

}