package uk.ac.ceh.gateway.catalogue.templateHelpers;

import lombok.SneakyThrows;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.HubbubResponse;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.UploadService;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import org.junit.jupiter.api.Test;

@ExtendWith(MockitoExtension.class)
class RoCrateServiceTest {
    @Mock
    private UploadService uploadService;

    private final String fileId = "164ef14f-95a5-45c7-8f36-d2000ba45516";
    private final String path = "subfolder/dataset.csv";
    private final LocalDateTime lastModified = LocalDateTime.of(2022, 3, 12, 14, 26, 52);
    private final LocalDateTime lastValidated = LocalDateTime.of(2022, 3, 26, 23, 9, 12);
    private final String baseUri = "https://mocktest.com";
    private final String datastore = "eidchub";
    private final int lastPage=2;
    private final int pageSize=5;
    private final int totalFiles=8;

    private HubbubResponse getHubbubResponse(int currentPage, int lastPage, int pageSize, int totalFiles, int numFilesOnPage){
        return new HubbubResponse(
            Collections.nCopies(
                numFilesOnPage,
                new HubbubResponse.FileInfo(456L,fileId,datastore,"csv","fc3facd3122cb0250f4bf82746d4bd13",0.32,lastModified,lastValidated,path,"VALID","text/csv")
            ),
            null,
            new HubbubResponse.Meta(currentPage, lastPage, pageSize, totalFiles)
        );
    }

    @Test
    @SneakyThrows
    /**
     * This ensures that the RoCrateService correctly handles the paging of the UploadService responses and produces
     * the expected list of Parts ready for a downstream Freemarker template (see templates/schema.org/schema.org.ftlh).
     * It also checks that the Path has a contentUrl based on the 'detached' ro-crate definition.
     */
    void from_detached() {
        //given
        boolean isAttached = false;
        String expectedContentUrl = baseUri + "/datastore/" + datastore + "/" + fileId + "/" + path;
        given(uploadService.get(fileId, datastore, 1, pageSize))
            .willReturn(getHubbubResponse(1, lastPage, pageSize, totalFiles, 5));
        given(uploadService.get(fileId, datastore, 2, pageSize))
            .willReturn(getHubbubResponse(2, lastPage, pageSize, totalFiles, 3));
        RoCrateService roCrateService = new RoCrateService(uploadService, datastore, baseUri, pageSize);

        //when
        var actual = roCrateService.from(fileId, isAttached);

        //then
        verify(uploadService).get(fileId, datastore, 1, 5);
        verify(uploadService).get(fileId, datastore, 2, 5);
        assertEquals(totalFiles, actual.size());
        assertEquals(expectedContentUrl, actual.get(0).getContentUrl());
    }

    @Test
    @SneakyThrows
    /**
     * This ensures that the RoCrateService correctly handles the paging of the UploadService responses and produces
     * the expected list of Parts ready for a downstream Freemarker template (see templates/schema.org/schema.org.ftlh).
     * It also checks that the Path has a contentUrl based on the 'attached' ro-crate definition.
     */
    void from_attached() {
        //given
        boolean isAttached = true;
        String expectedContentUrl = "data/" + path;
        given(uploadService.get(fileId, datastore, 1, pageSize))
            .willReturn(getHubbubResponse(1, lastPage, pageSize, totalFiles, 5));
        given(uploadService.get(fileId, datastore, 2, pageSize))
            .willReturn(getHubbubResponse(2, lastPage, pageSize, totalFiles, 3));
        RoCrateService roCrateService = new RoCrateService(uploadService, datastore, baseUri, pageSize);

        //when
        var actual = roCrateService.from(fileId, isAttached);

        //then
        verify(uploadService).get(fileId, datastore, 1, 5);
        verify(uploadService).get(fileId, datastore, 2, 5);
        assertEquals(totalFiles, actual.size());
        assertEquals(expectedContentUrl, actual.get(0).getContentUrl());
    }
}
