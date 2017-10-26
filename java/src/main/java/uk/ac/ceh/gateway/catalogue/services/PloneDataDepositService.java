package uk.ac.ceh.gateway.catalogue.services;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import lombok.AllArgsConstructor;
import uk.ac.ceh.gateway.catalogue.model.DocumentUpload;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

@AllArgsConstructor
public class PloneDataDepositService {
    private final WebResource ploneWebResource;

    public String addOrUpdate(DocumentUpload du) throws IOException, DocumentRepositoryException{
        List<String> files = du.getData().entrySet().stream()
                .map(f -> f.getKey() + ";" + f.getValue().getHash())
                .collect(Collectors.toList());

        MultivaluedMap formData = new MultivaluedMapImpl();
        formData.add("fileIdentifier", du.getGuid());
        formData.add("title", du.getTitle());
        formData.add("location", String.format("\\\\nerclactdb.nerc-lancaster.ac.uk\\appdev\\appdev\\datastore\\dropbox\\%s\\", du.getGuid()));
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
