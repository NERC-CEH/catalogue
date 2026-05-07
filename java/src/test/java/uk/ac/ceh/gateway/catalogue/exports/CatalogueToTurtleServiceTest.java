package uk.ac.ceh.gateway.catalogue.exports;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.services.MetadataListingService;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogueToTurtleServiceTest {
    private CatalogueToTurtleService service;

    @Mock
    CatalogueService catalogueService;
    @Mock
    MetadataListingService listing;
    private static final String baseUri = "https://example.com";
    private static final String catalogueKey = "eidc";
    private static final String otherCatalogueKey = "other";

    private final Catalogue catalogue = Catalogue.builder()
        .id(catalogueKey)
        .title("Env Data Centre")
        .url(baseUri)
        .contactUrl("")
        .logo("eidc.png")
        .build();

    private final Catalogue otherCatalogue = Catalogue.builder()
        .id(otherCatalogueKey)
        .title("Env Data Centre")
        .url(baseUri)
        .contactUrl("")
        .logo("eidc.png")
        .build();

    private final Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);

    @SneakyThrows
    @BeforeEach
    public void setup() {
        configuration.setDirectoryForTemplateLoading(new File("../templates"));
        service = new CatalogueToTurtleService(
            catalogueService,
            configuration,
            listing,
            baseUri
        );
    }

    @Test
    void getBigTtlWithoutPrefetch() {
        //Given
        given(catalogueService.retrieve(catalogueKey))
            .willReturn(catalogue);

        //When
        val result = service.getBigTtl(catalogueKey);

        //Then
        verify(catalogueService, times(1)).retrieve(catalogueKey);
        assertTrue(result.map(
            ttl -> ttl.contains("<%s/documents>".formatted(catalogueKey))
        ).orElse(false));
    }

    @Test
    void getBigTtlWithPrefetch() {
        //Given
        given(catalogueService.retrieve(catalogueKey))
            .willReturn(catalogue);

        //When
        service.fetchCatalogues();
        val result = service.getBigTtl(catalogueKey);

        //Then
        verify(catalogueService, times(1)).retrieve(catalogueKey);
        assertTrue(result.map(
            ttl -> ttl.contains("<%s/documents>".formatted(catalogueKey))
        ).orElse(false));
    }

    @Test
    void getOtherBigTtlWithoutPrefetch() {
        //Given
        given(catalogueService.retrieve(otherCatalogueKey))
            .willReturn(otherCatalogue);

        //When
        val result = service.getBigTtl(otherCatalogueKey);

        //Then
        verify(catalogueService, times(1)).retrieve(otherCatalogueKey);
        assertTrue(result.map(
            ttl -> ttl.contains("<%s/documents>".formatted(otherCatalogueKey))
        ).orElse(false));
    }

    @Test
    void getOtherBigTtlWithPrefetch() {
        //Given
        given(catalogueService.retrieve(anyString()))
            .willReturn(otherCatalogue);

        //When
        service.fetchCatalogues();
        val result = service.getBigTtl(otherCatalogueKey);

        //Then
        verify(catalogueService, times(2)).retrieve(anyString());
        assertTrue(result.map(
            ttl -> ttl.contains("<%s/documents>".formatted(otherCatalogueKey))
        ).orElse(false));
    }
}
