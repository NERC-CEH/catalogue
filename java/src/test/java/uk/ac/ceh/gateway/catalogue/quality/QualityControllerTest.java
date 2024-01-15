package uk.ac.ceh.gateway.catalogue.quality;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.services.MetadataListingService;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QualityControllerTest {

    @Mock
    private MetadataQualityService metadataQualityService;

    @Mock
    MetadataListingService metadataListingService;

    private QualityController qualityController;

    @BeforeEach
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
