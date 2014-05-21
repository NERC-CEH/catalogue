package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String organisation;
    private State state;
    
    public enum State { 
        PRIVATE, 
        PUBLIC, 
        SENSITIVE 
    }
}