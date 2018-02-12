package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import uk.ac.ceh.gateway.catalogue.model.Catalogue;
import uk.ac.ceh.gateway.catalogue.model.ResourceNotFoundException;
import uk.ac.ceh.gateway.catalogue.services.CatalogueService;
import uk.ac.ceh.gateway.catalogue.services.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.services.MetadataListingService;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
@AllArgsConstructor
public class SitemapController {
    private final CatalogueService catalogueService;
    private final DocumentIdentifierService identifierService;
    private final MetadataListingService listingService;

    @RequestMapping(value = "robots.txt", method = RequestMethod.GET)
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
        return new ModelAndView("/sitemap/robots.txt.tpl", model);
    }

    @RequestMapping(value = "{catalogue}/sitemap.txt", method = RequestMethod.GET)
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
        return new ModelAndView("/sitemap/sitemap.txt.tpl", "urls", urls);
    }

}