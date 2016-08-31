package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import lombok.experimental.Accessors;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.gemini.adapters.LocalDateTimeDeserializer;
import uk.ac.ceh.gateway.catalogue.gemini.adapters.LocalDateTimeSerializer;

@Data
@Accessors(chain = true)
public abstract class AbstractMetadataDocument implements MetadataDocument {
    private URI uri;
    private String id, title, description;
    protected Keyword resourceType; 
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
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
    public void attachMetadata(MetadataInfo metadata) {
        setMetadata(metadata);
    }
      
    @Override
    public void attachUri(URI uri) {
        setUri(uri);
    }
}
