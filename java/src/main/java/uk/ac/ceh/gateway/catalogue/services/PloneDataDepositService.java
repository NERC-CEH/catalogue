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

        String toReturn = "default";

        // List<String> files = du.getData().entrySet().stream()
        List<String> files = null;
        if(du != null) {
            files = du.getData().entrySet().stream()
                 .map(f -> URLEncoder.encode(f.getKey() + ";" + f.getValue().getHash()))
                 .collect(Collectors.toList());
        }

        // ClientResponse response = ploneWebResource
        //         .queryParam("fileIdentifier", URLEncoder.encode(du.getGuid()))
        //         .queryParam("title", du.getTitle())
        //         .queryParam("location", URLEncoder.encode(String.format("\\\\nerclactdb.nerc-lancaster.ac.uk\\appdev\\appdev\\datastore\\dropbox\\%s\\", du.getGuid())))
        //         .queryParam("files", String.join(",", files))
        //         .accept(MediaType.TEXT_PLAIN)
        //         .get(ClientResponse.class);

        MultivaluedMap formData = new MultivaluedMapImpl();
        formData.add("fileIdentifier", du.getGuid());
        formData.add("title", du.getTitle());
        formData.add("location", String.format("\\\\nerclactdb.nerc-lancaster.ac.uk\\appdev\\appdev\\datastore\\dropbox\\%s\\", du.getGuid()));
        formData.add("files", String.join(",", files));
        ClientResponse response = ploneWebResource
            .type(MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.TEXT_PLAIN)
            .post(ClientResponse.class, formData);

        // MultivaluedMap formData = new MultivaluedMapImpl();
        // formData.add("fileIdentifier", "5b338064-b09b-4846-a0b3-b7ec358eabcd");
        // formData.add("title", "my data stuff");
        // formData.add("location", "onthesan");
        // formData.add("files", String.join(",", "file1;123438064-b09b-4846-a0b3-b7ec358eabcd,file2;abcd8064-b09b-4846-a0b3-b7ec358eabcd"));
        // ClientResponse response = ploneWebResource
        //     .type(MediaType.APPLICATION_FORM_URLENCODED)
        //     .accept(MediaType.TEXT_PLAIN)
        //     .post(ClientResponse.class, formData);

        if (response.getStatus() == 400){
            String reason = response.getEntity(String.class);
            throw new RuntimeException(String.format("Failed to update Plone: HTTP error code: %s : Error message: %s", response.getStatus(), reason));
        }else if (response.getStatus() == 404){
            throw new RuntimeException("Got 404: " + String.format(ploneWebResource.getURI().toString(), response.getStatus()));
        }else if (response.getStatus() != 200){
            throw new RuntimeException(String.format("Failed to update Plone: HTTP error code: %s", response.getStatus()));
        }
        return response.getEntity(String.class);
        // return "any old crap";
    }

}
