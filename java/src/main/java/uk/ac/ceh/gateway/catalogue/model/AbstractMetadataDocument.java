package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.experimental.Accessors;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceIdentifier;
import uk.ac.ceh.gateway.catalogue.gemini.adapters.LocalDateTimeDeserializer;
import uk.ac.ceh.gateway.catalogue.gemini.adapters.LocalDateTimeSerializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Data
@Accessors(chain = true)
public abstract class AbstractMetadataDocument implements MetadataDocument {
    private String id, uri, type, title, description;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime metadataDate;
    private List<ResourceIdentifier> resourceIdentifiers;
    @JsonIgnore
    private MetadataInfo metadata;
    private Set<Relationship> relationships;
    private List<Keyword> keywords;

    public List<ResourceIdentifier> getResourceIdentifiers() {
        return Optional.ofNullable(resourceIdentifiers)
            .orElse(Collections.emptyList());
    }

    @Override
    @JsonIgnore
    public String getMetadataDateTime() {
        return Optional.ofNullable(metadataDate)
            .map(md -> md.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .orElse("");
    }

    @Override
    @JsonIgnore
    public List<Keyword> getAllKeywords() {
        return keywords;
    }

    @Override
    public MetadataDocument addAdditionalKeywords(List<Keyword> additionalKeywords) {
        keywords = Optional.ofNullable(keywords)
                .orElse(new ArrayList<>());

        keywords.addAll(additionalKeywords);
        return this;
    }

}
