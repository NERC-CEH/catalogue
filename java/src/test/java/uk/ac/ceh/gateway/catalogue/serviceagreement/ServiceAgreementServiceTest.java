package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.sparql.function.library.leviathan.log;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import uk.ac.ceh.components.datastore.DataDocument;
import uk.ac.ceh.components.datastore.DataOngoingCommit;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.git.GitDataDocument;
import uk.ac.ceh.gateway.catalogue.document.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.document.reading.DocumentReadingService;
import uk.ac.ceh.gateway.catalogue.document.reading.DocumentTypeLookupService;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class ServiceAgreementServiceTest {

    private static final String FOLDER = "service-agreements/";
    private static final String ID = "test";

    @Mock
    private DataRepository<CatalogueUser> repo;
    @Mock
    private DocumentInfoMapper<MetadataInfo> documentMetadataInfoMapper;
    @Mock
    private DocumentInfoMapper<ServiceAgreement> documentServiceAgreementMapper;
    @Mock
    private DocumentTypeLookupService documentTypeLookupService;
    @Mock
    private DocumentReadingService documentReader;
    @Mock
    private PostProcessingService<ServiceAgreement> postProcessingService;

    @InjectMocks
    private ServiceAgreementService serviceAgreementService;


    @Test
    @SneakyThrows
    public void canGet() {
        //Given
        log.debug("testing");
        CatalogueUser user = new CatalogueUser();
        user.setUsername("test");
        ServiceAgreement serviceAgreement = mock(ServiceAgreement.class);
        serviceAgreement.setId(ID);

        GitDataDocument metadataInfoDocument = mock(GitDataDocument.class);
        GitDataDocument rawDocument = mock(GitDataDocument.class);

        ByteArrayInputStream metadataInfoInputStream = new ByteArrayInputStream("meta".getBytes());
        ByteArrayInputStream rawInputStream = new ByteArrayInputStream("file".getBytes());

        MetadataInfo metadata = MetadataInfo.builder().rawType(MediaType.TEXT_XML_VALUE).build();

        given(rawDocument.getInputStream()).willReturn(rawInputStream);

        given(repo.getData(FOLDER + ID + ".meta")).willReturn(metadataInfoDocument);
        given(repo.getData(FOLDER + ID + ".raw")).willReturn(rawDocument);

        given(metadataInfoDocument.getInputStream()).willReturn(metadataInfoInputStream);
        given(documentMetadataInfoMapper.readInfo(any(InputStream.class))).willReturn(metadata);
        given(documentReader.read(rawInputStream, MediaType.TEXT_XML, ServiceAgreement.class)).willReturn(serviceAgreement);

        //When
        ServiceAgreement response = serviceAgreementService.get(ID);

        //Then
    }

    @Test
    @SneakyThrows
    public void getThrowsException() {
        CatalogueUser user = new CatalogueUser();
        user.setUsername("test");
        ServiceAgreement serviceAgreement = mock(ServiceAgreement.class);
        serviceAgreement.setId(ID);

        GitDataDocument metadataInfoDocument = mock(GitDataDocument.class);
        GitDataDocument rawDocument = mock(GitDataDocument.class);

        ByteArrayInputStream metadataInfoInputStream = new ByteArrayInputStream("meta".getBytes());
        ByteArrayInputStream rawInputStream = new ByteArrayInputStream("file".getBytes());

        MetadataInfo metadata = MetadataInfo.builder().rawType(MediaType.TEXT_XML_VALUE).build();

        given(rawDocument.getInputStream()).willReturn(rawInputStream);

        given(repo.getData(FOLDER + ID + ".meta")).willReturn(metadataInfoDocument);
        given(repo.getData(FOLDER + ID + ".raw")).willReturn(rawDocument);

        given(metadataInfoDocument.getInputStream()).willReturn(metadataInfoInputStream);
        given(documentMetadataInfoMapper.readInfo(any(InputStream.class))).willReturn(metadata);
        given(documentReader.read(rawInputStream, MediaType.TEXT_XML, ServiceAgreement.class)).willReturn(serviceAgreement);
        //Given
        user.setUsername("test");

        DataDocument dataDocument = mock(DataDocument.class);
        given(repo.getData(FOLDER + ID)).willThrow(new DataRepositoryException("failed"));

        //When
        assertThrows(DataRepositoryException.class, () ->
                serviceAgreementService.get(ID));
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
        serviceAgreementService.delete(user, ID);

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
        given(repo.getData(FOLDER + ID)).willReturn(dataDocument);

        //When
        boolean result = serviceAgreementService.metadataRecordExists(ID);

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

        given(repo.getData(FOLDER + ID)).willThrow(new DataRepositoryException("failed"));

        //When
        boolean result = serviceAgreementService.metadataRecordExists(ID);

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

        given(repo.submitData(any(), any())).willReturn(dataOngoingCommit);
        given(dataOngoingCommit.submitData(any(), any())).willReturn(dataOngoingCommit);

        //When
        serviceAgreementService.save(user, ID, "catalogue", serviceAgreement);

        //Then
        verify(dataOngoingCommit).commit(user, "catalogue");
    }


}