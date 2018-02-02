package uk.ac.ceh.gateway.catalogue.services;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import lombok.AllArgsConstructor;
import lombok.val;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.ac.ceh.gateway.catalogue.upload.UploadDocument;
import uk.ac.ceh.gateway.catalogue.upload.UploadFiles;

@AllArgsConstructor
public class PloneDataDepositService {
    private final WebResource ploneWebResource;

    private static String DOCUMENTS_LOCATION = "\\\\nerclactdb.nerc-lancaster.ac.uk\\appdev\\appdev\\datastore\\dropbox\\";
    private static String DATASTORE_LOCATION = "\\\\nerclactdb.nerc-lancaster.ac.uk\\appdev\\appdev\\datastore\\dropbox\\";

    public String addOrUpdate(UploadDocument document) {
        val parentId = document.getParentId();
        val documents = document.getUploadFiles().get("documents");
        val datastore = document.getUploadFiles().get("datastore");

        val validFiles = getValidFiles(parentId, documents, DOCUMENTS_LOCATION);
        validFiles.addAll(getValidFiles(parentId, datastore, DATASTORE_LOCATION));

        val invalidFiles = getInvalidFiles(documents);
        invalidFiles.addAll(getInvalidFiles(datastore));

        return addOrUpdate(validFiles, invalidFiles, document.getParentId(), document.getTitle());
    }

    private List<String> getValidFiles(String id, UploadFiles uploadFiles, String location) {
        return uploadFiles.getDocuments().values().stream()
            .map(f -> String.format(
                "%s;%s;%s%s%s",
                f.getName(),
                f.getHash(),
                location,
                id,
                uploadFiles.isZipped() ? String.format("\\%s.zip", id) : "")
            ).collect(Collectors.toList());
    }

    private List<String> getInvalidFiles(UploadFiles files) {
        return files
            .getInvalid()
            .values()
            .stream()
            .map(f -> f.getName())
            .collect(Collectors.toList());
    }

    private String addOrUpdate(List<String> validFiles, List<String> invalidFiles, String guid, String title) {
        MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
        formData.add("fileIdentifier", guid);
        formData.add("title", title);
        formData.add("files", String.join(",", validFiles));
        formData.add("invalidFiles", String.join(",", invalidFiles));

        ClientResponse response = ploneWebResource
            .type(MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.TEXT_PLAIN)
            .post(ClientResponse.class, formData);

        if (response.getStatus() != 200){
            throw new PloneUpdateFailure();
        }
        return response.getEntity(String.class);
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR , reason = "Can not finish your file upload or file move, there was a problem updating a backend service (Plone).")
    class PloneUpdateFailure extends RuntimeException {
        static final long serialVersionUID = 1L;
    }
}