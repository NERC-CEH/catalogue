package uk.ac.ceh.gateway.catalogue.templateHelpers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.HubbubResponse;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.UploadService;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * This class uses the Hubbub endpoint to get dataset information for the ro-crate metadata.
 */

@Service
@Slf4j
public class RoCrateService {
    private final UploadService uploadService;
    private final String datastore;
    private final String baseUri;
    private final int pageSize;

    public RoCrateService(
        UploadService uploadService,
        @Value("${hubbub.datastore:eidchub}") String datastore,
        @Value("${documents.baseUri}") String baseUri,
        @Value("${hubbub.request.pagesize}") int pageSize
    ) {
        this.uploadService = uploadService;
        this.datastore = datastore;
        this.baseUri = baseUri;
        this.pageSize = pageSize;
        log.info("Creating {}", this);
    }

    public ArrayList<Part> from(String fileId, boolean isAttached){
        int currentPage = 0;
        HubbubResponse resp;
        ArrayList<RoCrateService.Part> parts = new ArrayList<>();
        try {
            do {
                currentPage++;
                resp = uploadService.get(fileId, datastore, currentPage, pageSize);
                updateParts(resp, parts, fileId, isAttached);
            } while (currentPage != resp.getMeta().getLastPage());
        }catch(Exception e){
            log.info(e.toString());
            log.info(e.getMessage());
        }
        return parts;
    }

    private void updateParts(HubbubResponse resp, ArrayList<RoCrateService.Part> parts, String fileId, boolean isAttached) {
        resp.getData().forEach(file -> {
            String[] pathBits = file.getPath().split("/");
            String id = pathBits[pathBits.length - 1];
            String path = path(isAttached, fileId, file.getPath());
            parts.add(
                new Part(
                    id,
                    "File",
                    id,
                    file.getMimeType(),
                    path,
                    file.getBytes(),
                    file.getLastModified()
                )
            );
        });
    }

    /**
     * The key difference between 'attached' and 'detached' in ro-crate is whether the path is relative or not.
     * 'attached' is relative.  'detached' is absolute.  'attached' is used when data is bundled in a zip file,
     * such as our data-packager.  'detached' is just the absolute call to the data.
     * This method creates the appropriate path to the data dependent on whether it is 'attached' or 'detached'.
     * Example of an absolute path: https://catalogue.ceh.ac.uk/datastore/eidchub/fd8151e9-0ee2-4dfa-a254-470c9bb9bc1e/sub-folder/CBED-deposition-forest-2013-2015.csv
     * Example of a relative path as in the data-packager: data/sub-folder/CBED-deposition-forest-2013-2015.csv
     * @param isAttached boolean identifying whether 'attached' (true) or 'detached' (false)
     * @param fileId String the identifier of the dataset
     * @param path the path to the file as already taken from a HubbubResponse
     * @return the full path (relative or absolute) to the data
     */
    private String path(boolean isAttached, String fileId, String path){
        return isAttached ?
                "data/" + path
            :
                baseUri + "/datastore/" + datastore + "/" + fileId + "/" + path
            ;
    }

    @lombok.Value
    public class Part {
        String id, type, name, encodingFormat, contentUrl;
        Long bytes;
        LocalDateTime lastModified;
    }
}
