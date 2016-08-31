package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;

@ConvertUsing({
    @Template(called="html/catalogue.html.tpl", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
@Value
public class CatalogueResource {
    private final String id;
    private final List<String> catalogues;

    @JsonCreator
    @Builder
    private CatalogueResource(
        @JsonProperty("id") @NonNull String id,
        @JsonProperty("catalogues") @Singular @NonNull List<String> catalogues
    ) {
        this.id = id;
        this.catalogues = new ArrayList<>(catalogues);
    }

    public CatalogueResource(@NonNull MetadataDocument document) {
        MetadataInfo info = Objects.requireNonNull(document.getMetadata());
        this.id = document.getId();
        this.catalogues = new ArrayList<>(info.getCatalogues());
    }
    
    public MetadataInfo updateCatalogues(MetadataInfo original) {
        return original.setCatalogues(catalogues);
    }
       
}
