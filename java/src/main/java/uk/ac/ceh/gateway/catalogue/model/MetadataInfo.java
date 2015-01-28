package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;

/**
 * The following class represents the state at which a document is in
 * @author Christopher Johnson
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Accessors(chain = true)
public class MetadataInfo {
    private String rawType, state, documentType;
    private List<String> canView, canEdit, canDelete;
    
    @JsonIgnore
    public MediaType getRawMediaType() {
        return MediaType.parseMediaType(rawType);
    }
    
    public void hideMediaType() {
        setRawType(null);
    }
    
    public String getState() {
        return Optional.ofNullable(state).orElse("draft");
    }
    
    public String getDocumentType() {
        return Optional.ofNullable(documentType).orElse("");
    }
    
    public List<String> getCanView() {
        return Optional.ofNullable(canView).orElse(Collections.emptyList());
    }
    
    public List<String> getCanEdit() {
        return Optional.ofNullable(canEdit).orElse(Collections.emptyList());
    }
    
    public List<String> getCanDelete() {
        return Optional.ofNullable(canDelete).orElse(Collections.emptyList());
    }
}