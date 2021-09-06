package uk.ac.ceh.gateway.catalogue.serviceagreement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.components.datastore.DataDocument;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ServiceAgreementControllerTest {

    private static final String ID = "test";

    @Mock
    private ServiceAgreementSearch search;
    @Mock
    private ServiceAgreementService serviceAgreementService;

    @InjectMocks
    private ServiceAgreementController serviceAgreementController;

    @Test
    public void canCreate() {
        //Given
        CatalogueUser user = new CatalogueUser();

        given(serviceAgreementService.metadataRecordExists(ID)).willReturn(true);

        //When
        MetadataDocument response = serviceAgreementController.create(user,"catalogue", ID);

        //Then
        assertThat(response.getTitle(), is("test"));
    }

    @Test
    public void canGet() {
        //Given
        CatalogueUser user = new CatalogueUser();

        DataDocument dataDocument = mock(DataDocument.class);
        given(serviceAgreementService.get(ID)).willReturn(dataDocument);

        //When
        DataDocument response = serviceAgreementController.get(user, ID);

        //Then
        assertThat(response, is(dataDocument));
    }

    @Test
    public void canDelete() {
        //Given
        CatalogueUser user = new CatalogueUser();

        DataRevision<CatalogueUser> dataRevision = mock(DataRevision.class);
        given(serviceAgreementService.delete(user, ID)).willReturn(dataRevision);

        //When
        DataRevision response = serviceAgreementController.delete(user, ID);

        //Then
        assertThat(response, is(dataRevision));
    }

}