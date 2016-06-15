package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.NonNull;
import lombok.Value;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;

@ConvertUsing({
    @Template(called="html/catalogue.html.tpl", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
@Value
public class CatalogueResource {
    private final String id, title;
    private final List<String> catalogues;

    @JsonCreator
    private CatalogueResource(
        @JsonProperty("id") String id,
        @JsonProperty("title") String title,
        @JsonProperty("catalogues") List<String> catalogues
    ) {
        this.id = id;
        this.title = title;
        this.catalogues = catalogues;
    }

    public CatalogueResource(@NonNull MetadataDocument document) {
        MetadataInfo info = Objects.requireNonNull(document.getMetadata());
        this.id = document.getId();
        this.title = document.getTitle();
        this.catalogues = new ArrayList<>(info.getCatalogues());
    }
    
    public MetadataInfo updateCatalogues(MetadataInfo original) {
        return original.setCatalogues(catalogues);
    }
       
}
