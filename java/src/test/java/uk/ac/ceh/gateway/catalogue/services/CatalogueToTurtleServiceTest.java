package uk.ac.ceh.gateway.catalogue.services;

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
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CatalogueToTurtleServiceTest {
    private CatalogueToTurtleService service;

    @Mock
    CatalogueService catalogueService;
    @Mock
    DocumentRepository documentRepository;
    @Mock
    MetadataListingService listing;
    private static final String baseUri = "https://example.com";
    private static final String catalogueKey = "eidc";
    private final Catalogue catalogue = Catalogue.builder()
        .id(catalogueKey)
        .title("Env Data Centre")
        .url(baseUri)
        .contactUrl("")
        .logo("eidc.png")
        .build();
    private final Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);

    @SneakyThrows
    @BeforeEach
    public void setup() {
        given(catalogueService.retrieve(catalogueKey))
            .willReturn(catalogue);

        configuration.setDirectoryForTemplateLoading(new File("../templates"));
        service = new CatalogueToTurtleService(
            catalogueService,
            documentRepository,
            configuration,
            listing,
            baseUri
        );
    }

    @Test
    void getBigTtl() {
        //When
        val result = service.getBigTtl(catalogueKey);

        //Then
        assertTrue(result.map(
            ttl -> ttl.contains("<%s/documents>".formatted(catalogueKey))
        ).orElse(false));
    }
}
