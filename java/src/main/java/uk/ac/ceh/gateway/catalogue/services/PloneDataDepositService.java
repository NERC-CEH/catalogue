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
import uk.ac.ceh.gateway.catalogue.model.DocumentUpload;

@AllArgsConstructor
public class PloneDataDepositService {
    private final WebResource ploneWebResource;

    private static final Map<String, String> locations = new HashMap<>();
    static {
        locations.put("documents", "\\\\nerclactdb.nerc-lancaster.ac.uk\\appdev\\appdev\\datastore\\dropbox\\");
        locations.put("data", "\\\\nerclactdb.nerc-lancaster.ac.uk\\appdev\\appdev\\datastore\\eidchub\\");
    }
    
    public String addOrUpdate(DocumentUpload documentsUpload, DocumentUpload datastoreUpload) {
        List<String> files = toList(documentsUpload, locations.get("documents"));
        files.addAll(toList(datastoreUpload, locations.get("data")));
        return addOrUpdate(files, documentsUpload.getGuid(), documentsUpload.getTitle());
    }

    private List<String> toList(DocumentUpload upload, String location) {
        return upload.getDocuments().entrySet().stream()
                .map(f -> String.format("%s;%s;%s%s", f.getKey(), f.getValue().getHash(), location, upload.getGuid()))
                .collect(Collectors.toList());
    }

    private String addOrUpdate(List<String> files, String guid, String title){
        MultivaluedMap formData = new MultivaluedMapImpl();
        formData.add("fileIdentifier", guid);
        formData.add("title", title);
        formData.add("files", String.join(",", files));

        ClientResponse response = ploneWebResource
            .type(MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.TEXT_PLAIN)
            .post(ClientResponse.class, formData);

        if (response.getStatus() == 400){
            String reason = response.getEntity(String.class);
            throw new RuntimeException(String.format("Failed to update Plone: HTTP error code: %s : Error message: %s", response.getStatus(), reason));
        }else if (response.getStatus() != 200){
            throw new RuntimeException(String.format("Failed to update Plone: HTTP error code: %s", response.getStatus()));
        }
        return response.getEntity(String.class);
    }

}
