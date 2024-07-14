package uk.ac.ceh.gateway.catalogue.controllers;

import jakarta.servlet.http.HttpServletResponse;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.model.ResourceNotFoundException;
import uk.ac.ceh.gateway.catalogue.services.MetadataListingService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@ToString
@Controller
public class SitemapController {
    private final CatalogueService catalogueService;
    private final DocumentIdentifierService identifierService;
    private final MetadataListingService listingService;

    public SitemapController(
            CatalogueService catalogueService,
            DocumentIdentifierService identifierService,
            MetadataListingService listingService
    ) {
        this.catalogueService = catalogueService;
        this.identifierService = identifierService;
        this.listingService = listingService;
        log.info("Creating");
    }

    @GetMapping("robots.txt")
    public ModelAndView robots(
        HttpServletResponse response
    ) {
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        List<String> catalogues = catalogueService.retrieveAll()
            .stream()
            .map(Catalogue::getId)
            .filter(id -> !"inlicensed".equals(id))
            .filter(id -> !"ceh".equals(id))
            .collect(Collectors.toList());
        Map<String, Object> model = new HashMap<>();
        model.put("catalogues", catalogues);
        model.put("baseUri", identifierService.getBaseUri());
        return new ModelAndView("sitemap/robots.txt", model);
    }

    @GetMapping("{catalogue}/sitemap.txt")
    public ModelAndView sitemap(
        @PathVariable("catalogue") String catalogue,
        HttpServletResponse response
    ) {
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        if (catalogueService.retrieve(catalogue) == null) {
            throw new ResourceNotFoundException(catalogue + " is not a catalogue");
        }
        List<String> urls = listingService.getPublicDocumentsOfCatalogue(catalogue)
            .stream()
            .map(id -> identifierService.generateUri(id))
            .collect(Collectors.toList());
        return new ModelAndView("sitemap/sitemap.txt", "urls", urls);
    }

}
