package uk.ac.ceh.gateway.catalogue.catalogue;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Slf4j
@Controller
public class RootRedirectController {
    private final CatalogueService catalogueService;

    public RootRedirectController(CatalogueService catalogueService) {
        this.catalogueService = catalogueService;
    }

    @GetMapping
    public ModelAndView redirectRootToDefaultCatalogue(HttpServletRequest request) {
        val catalogueId = catalogueService.defaultCatalogue().getId();

        val builder = ServletUriComponentsBuilder.fromRequest(request)
            .replacePath("/" + catalogueId + "/documents")
            .replaceQuery(null);

        return new ModelAndView("redirect:" + builder.toUriString());
    }
}