package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import lombok.experimental.Accessors;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;

@Data
@Accessors(chain = true)
public abstract class MetadataDocumentImpl implements MetadataDocument {

    private URI uri;
    private String id, title, description;
    protected Keyword resourceType; 
    private List<String> partOfRepository;
    protected LocalDateTime metadataDate;
    private List<ResourceIdentifier> resourceIdentifiers;

    @JsonIgnore
    private MetadataInfo metadata;
    
    @Override
    public String getType() {
        return Optional.ofNullable(resourceType)
            .map(Keyword::getValue)
            .orElse("");
    }

    @Override
    public void attachPartOfRepository(List<String> repositories) {
        setPartOfRepository(repositories);
    }

    @Override
    public void attachMetadata(MetadataInfo metadata) {
        setMetadata(metadata);
    }
      
    @Override
    public void attachUri(URI uri) {
        setUri(uri);
    }    
}
