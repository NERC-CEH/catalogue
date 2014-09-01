package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.ukeof.UKEOFDocument;

/**
 * The following class represents the state at which a document is in along with 
 * the owning organisation
 * @author Christopher Johnson
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetadataInfo {
    private String rawType, state, documentType;
    
    @JsonIgnore
    public MediaType getRawMediaType() {
        return MediaType.parseMediaType(rawType);
    }
    
    @JsonIgnore
    public Class<? extends MetadataDocument> getDocumentClass() {
        switch(documentType) {
            case "GEMINI_DOCUMENT" : return GeminiDocument.class;
            case "UKEOF_DOCUMENT" :  return UKEOFDocument.class;
            default: throw new IllegalArgumentException(documentType + ": does not have a corresponding class");
        }
    }
    
    @JsonIgnore
    public void setDocumentClass(Class<? extends MetadataDocument> clazz) {
        if(GeminiDocument.class.isAssignableFrom(clazz)) {
            setDocumentType("GEMINI_DOCUMENT");
        }
        else if(UKEOFDocument.class.isAssignableFrom(clazz)) {
            setDocumentType("UKEOF_DOCUMENT");
        }
        else {
            throw new IllegalArgumentException(clazz + " cannot be mapped to a known metadata document");
        }
    }
    
    public void hideMediaType() {
        setRawType(null);
    }
}