package uk.ac.ceh.gateway.catalogue.imp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.experimental.Accessors;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@JsonTypeInfo(use=Id.NAME, include=As.EXISTING_PROPERTY, property="type", visible=true)
@JsonSubTypes({
    @Type(name="model",            value = Model.class),
    @Type(name="modelApplication", value = ModelApplication.class)
})
public class ImpDocument extends AbstractMetadataDocument {
    private String type;
    private List<String> identifiers;
    private List<Link> links;
    private List<Keyword> keywords;

    @Override
    public void addAdditionalKeywords(@NonNull List<Keyword> additionalKeywords) {
        keywords = Optional.ofNullable(keywords)
            .orElse(new ArrayList<>());
        keywords.addAll(additionalKeywords);
    }

    @Override
    @JsonIgnore
    public List<Keyword> getAllKeywords() {
        return keywords;
    }

}
