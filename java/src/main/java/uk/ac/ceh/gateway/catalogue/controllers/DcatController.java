package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.model.ResourceNotFoundException;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.services.MetadataListingService;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@ToString
@Controller
public class DcatController {
    //
    private final CatalogueService catalogueService;
    private final DocumentIdentifierService identifierService;
    private final MetadataListingService listingService;

    public DcatController(
            CatalogueService catalogueService,
            DocumentIdentifierService identifierService,
            MetadataListingService listingService
    ) {
        this.catalogueService = catalogueService;
        this.identifierService = identifierService;
        this.listingService = listingService;
        log.info("Creating");
    }

    @GetMapping("{catalogue}/catalogue.ttl")
    public ModelAndView dcat(
        @PathVariable("catalogue") String catalogue,
        HttpServletResponse response
    ) {
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        if (catalogueService.retrieve(catalogue) == null) {
            throw new ResourceNotFoundException(catalogue + " is not a catalogue");
        }
        List<String> records = listingService.getPublicDocumentsOfCatalogue(catalogue)
            .stream()
            .collect(Collectors.toList());
        return new ModelAndView("rdf/catalogue.ttl", "records", records);
    }

}
