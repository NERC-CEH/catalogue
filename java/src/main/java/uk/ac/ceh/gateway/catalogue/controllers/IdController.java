package uk.ac.ceh.gateway.catalogue.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("id")
public class IdController {
    @Value("${documents.baseUri}") String baseUri;

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
