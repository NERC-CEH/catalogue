package uk.ac.ceh.gateway.catalogue.quality;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.quality.MetadataQualityService.Results;
import uk.ac.ceh.gateway.catalogue.services.DocumentReader;

//@Controller
@SuppressWarnings("unused")
public class QualityController {
    private final MetadataQualityService metadataQualityService;

    @Autowired
    public QualityController(DocumentReader documentReader, ObjectMapper objectMapper) {
        this.metadataQualityService = new MetadataQualityService(documentReader, objectMapper);
    }

    @PreAuthorize("@permission.toAccess(#user, #file, 'VIEW')")
    @GetMapping(value = "documents/{file}/quality", produces = "application/json")
    @ResponseBody
    public HttpEntity<Results> readMetadata(
        @ActiveUser CatalogueUser user,
        @PathVariable("file") String file
    ) {
        return ResponseEntity.ok(metadataQualityService.check(file));
    }
}
