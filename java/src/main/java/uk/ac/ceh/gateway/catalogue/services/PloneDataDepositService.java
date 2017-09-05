package uk.ac.ceh.gateway.catalogue.services;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ceh.gateway.catalogue.model.DocumentUpload;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

@Slf4j
@AllArgsConstructor
public class PloneDataDepositService {
    private final WebResource ploneWebResource;
    private final DocumentUploadService documentUploadService;
            
    public void processDataDeposit(String guid) throws IOException, DocumentRepositoryException{
        DocumentUpload du = documentUploadService.get(guid);
        List<String> files = du.getData().entrySet().stream()
                 .map(f -> URLEncoder.encode(f.getKey() + ";" + f.getValue().getHash()))
                 .collect(Collectors.toList());

        ClientResponse response = ploneWebResource
                .queryParam("fileIdentifier", URLEncoder.encode(guid))
                .queryParam("title", URLEncoder.encode(du.getTitle()))
                .queryParam("location", URLEncoder.encode(du.getPath()))
                .queryParam("files", String.join(",", files))
                .accept(MediaType.TEXT_PLAIN)
                .get(ClientResponse.class);
        
        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed to update Plone: HTTP error code: " + response.getStatus());
        }
        
        System.out.println(response.getEntity(String.class));
        System.out.println(response.toString());
    }

}
