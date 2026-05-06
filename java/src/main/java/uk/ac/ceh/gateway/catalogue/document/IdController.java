package uk.ac.ceh.gateway.catalogue.document;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;
import uk.ac.ceh.gateway.catalogue.services.ResourceIdentifierLookupService;

@Controller
@RequestMapping("id")
@RequiredArgsConstructor
public class IdController {
    @Value("${documents.baseUri}") String baseUri;
    private final ResourceIdentifierLookupService resolver;

    @GetMapping("{codespace}/{code}")
    public RedirectView resolveRi(
        @PathVariable String codespace,
        @PathVariable String code,
        HttpServletRequest request
    ) {
        String combined = codespace + ":" + code;

        return resolver.resolveToUuid(combined)
            .map(uuid -> redirect(uuid, request))
            .orElseGet(() -> redirect(combined, request));
    }

    @GetMapping("{id}.xml")
    public RedirectView redirectXmlToResource(@PathVariable String id, HttpServletRequest request) {
        return redirect(id + ".xml", request);
    }

    @GetMapping("{id}")
    public RedirectView redirectToResource(@PathVariable String id, HttpServletRequest request) {
        return redirect(id, request);
    }

    private RedirectView redirect(String path, HttpServletRequest request) {
        val requestParm = request.getQueryString() == null ? "" : "?" + request.getQueryString();
        val redirectView = new RedirectView(String.format("%s/documents/%s%s", baseUri, path, requestParm));
        redirectView.setStatusCode(HttpStatus.SEE_OTHER);
        return redirectView;
    }
}
