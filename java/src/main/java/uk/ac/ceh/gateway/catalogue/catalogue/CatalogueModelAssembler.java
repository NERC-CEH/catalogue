package uk.ac.ceh.gateway.catalogue.catalogue;

import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CatalogueModelAssembler extends RepresentationModelAssemblerSupport<Catalogue, CatalogueModel> {

    public CatalogueModelAssembler() {
        super(CatalogueController.class, CatalogueModel.class);
    }

    @Override
    public CatalogueModel toModel(Catalogue catalogue) {
        return createModelWithId(catalogue.getId(), catalogue);
    }

    @Override
    protected CatalogueModel instantiateModel(Catalogue catalogue) {
        log.debug(catalogue.toString());
        return new CatalogueModel(catalogue);
    }
}
