package uk.ac.ceh.gateway.catalogue.catalogue;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Set;

@Slf4j
@Controller
public class RootRedirectController {
    private static final Set<String> LOCAL_HOSTS = Set.of("localhost", "127.0.0.1", "::1");

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

        // dri-one #71: the reverse proxy terminates TLS and forwards plain HTTP, so the
        // request scheme can be "http" even though the browser used HTTPS. Force HTTPS for
        // real deployments so the redirect chain never downgrades; leave local HTTP dev alone.
        if (!LOCAL_HOSTS.contains(request.getServerName())) {
            builder.scheme("https").port(-1);
        }

        return new ModelAndView("redirect:" + builder.toUriString());
    }
}