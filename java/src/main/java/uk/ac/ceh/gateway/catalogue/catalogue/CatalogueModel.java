package uk.ac.ceh.gateway.catalogue.catalogue;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;
import uk.ac.ceh.gateway.catalogue.vocabularies.KeywordVocabulary;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class CatalogueModel extends RepresentationModel<CatalogueModel> {
    private String id;
    private String title;
    private List<KeywordVocabulary> vocabularies;

    public CatalogueModel(Catalogue catalogue) {
        this.id = catalogue.getId();
        this.title = catalogue.getTitle();
        this.vocabularies = catalogue.getVocabularies();
    }
}
