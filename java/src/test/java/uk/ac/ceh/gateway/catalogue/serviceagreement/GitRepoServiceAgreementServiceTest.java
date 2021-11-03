package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.components.datastore.*;
import uk.ac.ceh.gateway.catalogue.document.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceConstraint;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SuppressWarnings({"unchecked", "rawtypes"})
@Slf4j
@ExtendWith(MockitoExtension.class)
public class GitRepoServiceAgreementServiceTest {

    private static final String FOLDER = "service-agreements/";
    private static final String ID = "test";

    @Mock private DataRepository<CatalogueUser> repo;
    @Mock private DocumentInfoMapper<MetadataInfo> metadataInfoMapper;
    @Mock private DocumentInfoMapper<ServiceAgreement> serviceAgreementMapper;
    @Mock private DocumentRepository documentRepository;

    private ServiceAgreementService service;
    private ServiceAgreement serviceAgreement;

    private static CatalogueUser user;

    @BeforeAll
    static void init() {
        user = new CatalogueUser();
        user.setUsername("test");
    }

    @BeforeEach
    void setup() {
        service = new GitRepoServiceAgreementService(
            repo,
            metadataInfoMapper,
            serviceAgreementMapper,
            documentRepository
        );
        serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(ID);
    }

    @Test
    @SneakyThrows
    public void getServiceAgreement() {
        //Given
        givenPublishedServiceAgreement();

        //When
        service.get(ID);

        //Then
    }

    @Test
    @SneakyThrows
    public void canNotGetRaw() {
        //Given
        val metadataInfoDocument = mock(DataDocument.class);
        given(repo.getData(FOLDER + ID + ".meta"))
                .willReturn(metadataInfoDocument);

        given(repo.getData(FOLDER + ID + ".raw"))
                .willThrow(new DataRepositoryException("Fail"));

        //When
        assertThrows(DataRepositoryException.class, () ->
                service.get(ID)
        );
    }

    @Test
    @SneakyThrows
    public void canNotGetMeta() {
        //Given
        given(repo.getData(FOLDER + ID + ".meta"))
                .willThrow(new DataRepositoryException("Fail"));

        //When
        assertThrows(DataRepositoryException.class, () ->
                service.get(ID)
        );
    }

    @Test
    @SneakyThrows
    public void canDelete() {
        //Given
        val dataOngoingCommit = mock(DataOngoingCommit.class);
        given(repo.deleteData(FOLDER + ID + ".meta"))
            .willReturn(dataOngoingCommit);
        given(dataOngoingCommit.deleteData(any()))
            .willReturn(dataOngoingCommit);

        //When
        service.delete(user, ID);

        //Then
        verify(dataOngoingCommit).commit(user, "delete document: test");
    }

    @Test
    @SneakyThrows
    public void metadataRecordExists() {
        //Given
        val dataDocument = mock(DataDocument.class);
        given(repo.getData(ID + ".meta"))
            .willReturn(dataDocument);

        //When
        boolean result = service.metadataRecordExists(ID);

        //Then
        assertThat(result, is(equalTo(true)));
    }

    @Test
    @SneakyThrows
    public void metadataRecordDoesNotExist() {
        //Given
        given(repo.getData(ID + ".meta"))
            .willThrow(new DataRepositoryException("failed"));

        //When
        boolean result = service.metadataRecordExists(ID);

        assertThat(result, is(false));
    }

    @Test
    @SneakyThrows
    public void updateServiceAgreement() {
        //Given
        val dataOngoingCommit = mock(DataOngoingCommit.class);

        given(repo.submitData(
            eq("service-agreements/" + ID + ".raw"),
            any(DataWriter.class)
        ))
            .willReturn(dataOngoingCommit);

        given(dataOngoingCommit.commit(
            any(CatalogueUser.class), eq("updating service agreement test")
        ))
            .willReturn(mock(DataRevision.class));

        givenPublishedServiceAgreement();

        //When
        service.update(user, ID, serviceAgreement);

        //Then
        verify(dataOngoingCommit).commit(user, "updating service agreement test");
    }

    @Test
    @SneakyThrows
    public void createServiceAgreement() {
        //Given
        DataOngoingCommit dataOngoingCommit = mock(DataOngoingCommit.class);

        given(repo.submitData(any(), any()))
            .willReturn(dataOngoingCommit);
        given(dataOngoingCommit.submitData(any(), any()))
            .willReturn(dataOngoingCommit);

        givenPublishedServiceAgreement();

        //When
        service.create(user, ID, "eidc", serviceAgreement);

        //Then
        verify(dataOngoingCommit).commit(user, "creating service agreement test");
    }

    @Test
    @SneakyThrows
    void canPopulateDraftGeminiDocument() {
        //Given
        givenDraftGeminiDocument();
        givenPublishedServiceAgreement();
        val expected = new GeminiDocument();
        expected.setId(ID);
        expected.setTitle("this is a test");
        expected.setMetadata(MetadataInfo.builder().state("draft").build());
        expected.setUseConstraints(List.of(serviceAgreement.getEndUserLicence()));

        //When
        service.populateGeminiDocument(user, ID);

        //then
        verify(documentRepository).save(user, expected, "populated from service agreement");
    }

    @Test
    @SneakyThrows
    void cannotPopulatePublishedGeminiDocument() {
        //given
        givenPublishedGeminiDocument();
        givenDraftServiceAgreement();

        //when
        assertThrows(ServiceAgreementException.class, () ->
            service.populateGeminiDocument(user, ID)
        );

        //then
        verify(documentRepository, never()).save(eq(user), any(MetadataDocument.class), eq("populated from service agreement"));
    }

    @SneakyThrows
    private void givenPublishedServiceAgreement() {
        val metadataInfoDocument = mock(DataDocument.class);
        given(repo.getData(FOLDER + ID + ".meta"))
            .willReturn(metadataInfoDocument);
        given(metadataInfoDocument.getInputStream())
            .willReturn(new ByteArrayInputStream("meta".getBytes()));

        val metadata = MetadataInfo.builder()
            .state("published")
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
        serviceAgreement.setMetadata(metadata);
        serviceAgreement.setTitle("this is a test");
        serviceAgreement.setEndUserLicence(new ResourceConstraint("test", "test", "test"));
    }

    @SneakyThrows
    private void givenDraftServiceAgreement() {
        val metadataInfoDocument = mock(DataDocument.class);
        given(repo.getData(FOLDER + ID + ".meta"))
            .willReturn(metadataInfoDocument);
        given(metadataInfoDocument.getInputStream())
            .willReturn(new ByteArrayInputStream("meta".getBytes()));

        val metadata = MetadataInfo.builder()
            .state("draft")
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
        serviceAgreement.setMetadata(metadata);
        serviceAgreement.setId(ID);
    }

    @SneakyThrows
    private void givenDraftGeminiDocument() {
        val metadataInfo = MetadataInfo.builder().state("draft").build();
        val geminiDocument = new GeminiDocument();
        geminiDocument.setId(ID);
        geminiDocument.setMetadata(metadataInfo);
        given(documentRepository.read(ID))
            .willReturn(geminiDocument);
    }

    @SneakyThrows
    private void givenPublishedGeminiDocument() {
        val metadataInfo = MetadataInfo.builder().state("published").build();
        val geminiDocument = new GeminiDocument();
        geminiDocument.setId(ID);
        geminiDocument.setMetadata(metadataInfo);
        given(documentRepository.read(ID))
            .willReturn(geminiDocument);
    }

}
