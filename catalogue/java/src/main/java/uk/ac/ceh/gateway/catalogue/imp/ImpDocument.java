package uk.ac.ceh.gateway.catalogue.imp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import java.net.URI;
import java.util.List;
import lombok.Data;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;

@Data
@JsonTypeInfo(use=Id.NAME, include=As.EXISTING_PROPERTY, property="type", visible=true)
@JsonSubTypes({
    @Type(name="model",            value = Model.class),
    @Type(name="modelApplication", value = ModelApplication.class)
})
public class ImpDocument implements MetadataDocument {
    private URI uri;
    private String id, title, description, type;
    private List<String> identifiers;
    private List<Link> links;
    
    @JsonIgnore
    private MetadataInfo metadata;
    
    @Override
    public void attachMetadata(MetadataInfo metadata) {
        setMetadata(metadata);
    }

    @Override
    public void attachUri(URI uri) {
        setUri(uri);
    }
}
