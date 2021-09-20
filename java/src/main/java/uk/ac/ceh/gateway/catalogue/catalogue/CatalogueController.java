package uk.ac.ceh.gateway.catalogue.catalogue;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.web.bind.annotation.*;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@ToString
@RequestMapping("catalogues")
public class CatalogueController {
    private final DocumentRepository documentRepository;
    private final CatalogueService catalogueService;
    private final CatalogueModelAssembler catalogueModelAssembler;

    public CatalogueController(
        DocumentRepository documentRepository,
        CatalogueService catalogueService,
        CatalogueModelAssembler catalogueModelAssembler
    ) {
        this.documentRepository = documentRepository;
        this.catalogueService = catalogueService;
        this.catalogueModelAssembler = catalogueModelAssembler;
        log.info("Creating");
    }

    @GetMapping
    public List<CatalogueModel> catalogues(
        @RequestParam(value = "catalogue", required = false) String catalogue,
        @RequestParam(value = "identifier", required = false) String identifier
    ) {
        List<Catalogue> catalogues = new ArrayList<>(catalogueService.retrieveAll());

        try {
            if(catalogue != null) {
                catalogues.remove(catalogueService.retrieve(catalogue));
            } else if (identifier != null) {
                catalogues.remove(
                    catalogueService.retrieve(
                        documentRepository.read(
                            identifier
                        ).getCatalogue()
                    )
                );
            }
        } catch (CatalogueException | DocumentRepositoryException ex) {
            // If the catalogue or identifier does not exist just return all the catalogues
        }
        return catalogues.stream()
            .map(catalogueModelAssembler::toModel)
            .collect(Collectors.toList());
    }

    @GetMapping("{id}")
    public CatalogueModel get(
        @PathVariable("id") String id
    ) {
        val catalogue = catalogueService.retrieve(id);
        return catalogueModelAssembler.toModel(catalogue);
    }
}
