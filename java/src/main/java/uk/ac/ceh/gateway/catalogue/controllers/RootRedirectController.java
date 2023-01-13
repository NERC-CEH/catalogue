package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;

import static java.lang.String.format;

@Slf4j
@Controller
public class RootRedirectController {
    CatalogueService catalogueService;

    public RootRedirectController(CatalogueService catalogueService) {
        this.catalogueService = catalogueService;
    }

    @GetMapping
    public ModelAndView redirectRootToDefaultCatalogue() {
        val catalogueId = catalogueService.defaultCatalogue().getId();
        val url = format("redirect:/%s/documents", catalogueId);
        return new ModelAndView(url);
    }
}
