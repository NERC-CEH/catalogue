package uk.ac.ceh.gateway.catalogue.quality;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.services.MetadataListingService;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class QualityControllerTest {

    @Mock
    private MetadataQualityService metadataQualityService;

    @Mock
    MetadataListingService metadataListingService;

    private QualityController qualityController;

    @Before
    public void setup() {
        qualityController = new QualityController(
            metadataQualityService,
            metadataListingService
        );
    }

    @Test
    public void checkIndividualFile() {
        //given
        CatalogueUser catalogueUser = CatalogueUser.PUBLIC_USER;

        //when
        qualityController.quality(catalogueUser, "eidc", "test");

        //then
        verify(metadataQualityService).check("test");
        verify(metadataListingService, never()).getPublicDocumentsOfCatalogue("eidc");
    }

    @Test
    public void checkWholeCatalogue() {
        //given
        CatalogueUser catalogueUser = CatalogueUser.PUBLIC_USER;
        given(metadataListingService.getPublicDocumentsOfCatalogue("eidc"))
            .willReturn(Arrays.asList(
                "test0",
                "test1",
                "test2"
            ));

        //when
        qualityController.quality(catalogueUser, "eidc");

        //then
        verify(metadataQualityService, times(3)).check(anyString());
        verify(metadataListingService).getPublicDocumentsOfCatalogue("eidc");
    }
}