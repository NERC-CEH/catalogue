package uk.ac.ceh.gateway.catalogue.catalogue;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import static java.lang.String.format;

@Slf4j
@Controller
public class RootRedirectController {
    CatalogueService catalogueService;

    public RootRedirectController(CatalogueService catalogueService) {
        this.catalogueService = catalogueService;
    }

    @GetMapping
    public ModelAndView redirectRootToDefaultCatalogue(HttpServletRequest request) {
        val catalogueId = catalogueService.defaultCatalogue().getId();
        val serverName = request.getServerName();
        val serverPort = request.getServerPort();

        String url;
        if (serverPort == 443) {
            url = format("https://%s/%s/documents", serverName, catalogueId);
        } else if (serverPort == 80) {
            url = format("https://%s/%s/documents", serverName, catalogueId);
        } else {
            url = format("https://%s:%d/%s/documents", serverName, serverPort, catalogueId);
        }

        RedirectView redirectView = new RedirectView(url, true, true, false);
        return new ModelAndView(redirectView);
    }
}
