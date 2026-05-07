package uk.ac.ceh.gateway.catalogue.waf;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.permission.CrowdPermissionServiceTest;
import uk.ac.ceh.gateway.catalogue.services.MetadataListingService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GeminiWafServiceTest {
    private GeminiWafService service;

    @Mock
    DataRepository<CatalogueUser> repo;
    @Mock
    MetadataListingService listing;

    @SneakyThrows
    @BeforeEach
    public void setup() {
        List<String> files = Arrays.asList("test1", "test2");
        List<String> resourceTypes = new ArrayList<>(Arrays.asList("dataset", "service"));
        given(repo.getLatestRevision()).willReturn(new CrowdPermissionServiceTest.DummyRevision("latest"));
        given(listing.getPublicDocuments("latest", GeminiDocument.class, resourceTypes))
            .willReturn(files);

        service = new GeminiWafService(
            repo,
            listing
        );
    }

    @Test
    @SneakyThrows
    void getWafFilesWithoutPrefetch() {
        //Given
        val expect = Arrays.asList("test1.xml", "test2.xml");

        //When
        val result = service.getWafFiles();

        //Then
        verify(listing, times(1)).getPublicDocuments(eq("latest"), eq(GeminiDocument.class), any());
        assertEquals(expect, result);
    }

    @Test
    @SneakyThrows
    void getWafFilesWithPrefetch() {
        //Given
        val expect = Arrays.asList("test1.xml", "test2.xml");

        //When
        service.fetchFiles();
        val result = service.getWafFiles();

        //Then
        verify(listing, times(1)).getPublicDocuments(eq("latest"), eq(GeminiDocument.class), any());
        assertEquals(expect, result);
    }
}
