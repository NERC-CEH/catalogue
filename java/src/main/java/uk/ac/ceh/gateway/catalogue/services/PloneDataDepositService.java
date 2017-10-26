package uk.ac.ceh.gateway.catalogue.services;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import net.lingala.zip4j.exception.ZipException;
import uk.ac.ceh.gateway.catalogue.model.DocumentUpload;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

@AllArgsConstructor
public class PloneDataDepositService {
    private final WebResource ploneWebResource;

    public String addOrUpdate(DocumentUpload du) throws ZipException, IOException, DocumentRepositoryException{
        List<String> files = du.getDocuments().entrySet().stream()
                 .map(f -> URLEncoder.encode(f.getKey() + ";" + f.getValue().getHash()))
                 .collect(Collectors.toList());

        ClientResponse response = ploneWebResource
                .queryParam("fileIdentifier", URLEncoder.encode(du.getGuid()))
                .queryParam("title", du.getTitle())
                .queryParam("location", URLEncoder.encode(String.format("\\\\nerclactdb.nerc-lancaster.ac.uk\\appdev\\appdev\\datastore\\dropbox\\%s\\", du.getGuid())))
                .queryParam("files", String.join(",", files))
                .accept(MediaType.TEXT_PLAIN)
                .get(ClientResponse.class);

        if (response.getStatus() == 400){
            String reason = response.getEntity(String.class);
            throw new RuntimeException(String.format("Failed to update Plone: HTTP error code: %s : Error message: %s", response.getStatus(), reason));
        }else if (response.getStatus() != 200){
            throw new RuntimeException(String.format("Failed to update Plone: HTTP error code: %s", response.getStatus()));
        }
        return response.getEntity(String.class);
    }

}
