package uk.ac.ceh.gateway.catalogue.services;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.model.DocumentUpload;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PloneDataDepositService {
    @Qualifier("plone")
    private final WebResource ploneWebResource;

    public String addOrUpdate(DocumentUpload du) throws IOException, DocumentRepositoryException{
        List<String> files = du.getData().entrySet().stream()
                 .map(f -> {
                     try {
                         return URLEncoder.encode(f.getKey() + ";" + f.getValue().getHash(), "utf-8");
                     } catch (UnsupportedEncodingException e) {
                         throw new RuntimeException(e);
                     }
                 })
                 .collect(Collectors.toList());

        ClientResponse response = ploneWebResource
                .queryParam("fileIdentifier", URLEncoder.encode(du.getGuid(), "utf-8"))
                .queryParam("title", du.getTitle())
                .queryParam("location", URLEncoder.encode("\\\\nerclactdb.nerc-lancaster.ac.uk\\appdev\\appdev\\datastore\\eidchub\\", "utf-8"))
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
