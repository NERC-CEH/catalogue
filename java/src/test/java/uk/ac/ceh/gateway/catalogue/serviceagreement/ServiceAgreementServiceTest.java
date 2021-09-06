package uk.ac.ceh.gateway.catalogue.serviceagreement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.components.datastore.DataOngoingCommit;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataWriter;
import uk.ac.ceh.gateway.catalogue.document.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.document.reading.DocumentTypeLookupService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

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
    public void canSave() throws DataRepositoryException {
        //Given
        CatalogueUser user = new CatalogueUser();
        user.setUsername("test");
        String id = "test";
        String message = "template: test";
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId("id");
        DataOngoingCommit dataOngoingCommit = mock(DataOngoingCommit.class);

        when(repo.submitData(any(), any())).thenReturn(dataOngoingCommit);
        given(dataOngoingCommit.submitData(any(), any())).willReturn(dataOngoingCommit);

        //When
        serviceAgreementService.save(user, id, "catalogue", serviceAgreement);

        //Then
        verify(dataOngoingCommit).commit(user, "catalogue");
    }


}