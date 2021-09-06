package uk.ac.ceh.gateway.catalogue.serviceagreement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.components.datastore.*;
import uk.ac.ceh.gateway.catalogue.document.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.document.reading.DocumentTypeLookupService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ServiceAgreementServiceTest {

    @Mock
    private DataRepository<CatalogueUser> repo;
    @Mock
    private DocumentInfoMapper<MetadataInfo> documentInfoMapper;
    @Mock
    private DocumentTypeLookupService documentTypeLookupService;

    @InjectMocks
    private ServiceAgreementService serviceAgreementService;


    @Test
    public void canGet() throws DataRepositoryException {
        //Given
        CatalogueUser user = new CatalogueUser();
        user.setUsername("test");
        String id = "test";
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId("id");

        DataDocument dataDocument = mock(DataDocument.class);


        given(repo.getData("service-agreements/" + id)).willReturn(dataDocument);

        //When
        serviceAgreementService.get(id);

        //Then
        // verify(dataDocument).commit(user, "catalogue");
    }

    @Test
    public void canDelete() throws DataRepositoryException {
        //Given
        CatalogueUser user = new CatalogueUser();
        user.setUsername("test");
        String id = "test";
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId("id");


        DataOngoingCommit<CatalogueUser> dataOngoingCommit = mock(DataOngoingCommit.class);


        given(repo.deleteData("service-agreements/" + id + ".meta")).willReturn(dataOngoingCommit);
        given(dataOngoingCommit.deleteData(any())).willReturn(dataOngoingCommit);

        //When
        serviceAgreementService.delete(user, id);

        //Then
        verify(dataOngoingCommit).commit(user, "delete document: test");
    }

    @Test
    public void metadataRecordExists() throws DataRepositoryException {
        //Given
        CatalogueUser user = new CatalogueUser();
        user.setUsername("test");
        String id = "test";
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId("id");

        DataDocument dataDocument = mock(DataDocument.class);


        given(repo.getData("service-agreements/" + id)).willReturn(dataDocument);

        //When
        boolean result = serviceAgreementService.metadataRecordExists(id);

        //Then
        assertThat(result, is(equalTo(true)));
    }

    @Test
    public void canSave() throws DataRepositoryException {
        //Given
        CatalogueUser user = new CatalogueUser();
        user.setUsername("test");
        String id = "test";
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId("id");
        DataOngoingCommit dataOngoingCommit = mock(DataOngoingCommit.class);

        given(repo.submitData(any(), any())).willReturn(dataOngoingCommit);
        given(dataOngoingCommit.submitData(any(), any())).willReturn(dataOngoingCommit);

        //When
        serviceAgreementService.save(user, id, "catalogue", serviceAgreement);

        //Then
        verify(dataOngoingCommit).commit(user, "catalogue");
    }


}