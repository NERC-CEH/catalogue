package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.components.datastore.DataDocument;
import uk.ac.ceh.components.datastore.DataOngoingCommit;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.gateway.catalogue.document.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.document.reading.DocumentTypeLookupService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ServiceAgreementServiceTest {

    private static final String FOLDER = "service-agreements/";
    private static final String ID = "test";

    @Mock
    private DataRepository<CatalogueUser> repo;
    @Mock
    private DocumentInfoMapper<MetadataInfo> documentInfoMapper;
    @Mock
    private DocumentTypeLookupService documentTypeLookupService;

    @InjectMocks
    private ServiceAgreementService serviceAgreementService;


    @Test
    @SneakyThrows
    public void canGet() {
        //Given
        CatalogueUser user = new CatalogueUser();
        user.setUsername("test");
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(ID);

        DataDocument dataDocument = mock(DataDocument.class);
        given(repo.getData(FOLDER + ID)).willReturn(dataDocument);

        //When
        DataDocument response = serviceAgreementService.get(ID);

        //Then
        assertThat(response, is(dataDocument));
    }

    @Test
    @SneakyThrows
    public void getThrowsException() {
        //Given
        CatalogueUser user = new CatalogueUser();
        user.setUsername("test");
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(ID);

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

        DataDocument dataDocument = mock(DataDocument.class);
        given(repo.getData(FOLDER + ID)).willThrow(new DataRepositoryException("failed"));

        //When
        assertThrows(DataRepositoryException.class, () ->
                serviceAgreementService.metadataRecordExists(ID));
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