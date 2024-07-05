package uk.ac.ceh.gateway.catalogue.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
@RequestMapping("id")
public class IdController {

    @GetMapping("{id}.xml")
    public RedirectView redirectXmlToResource(@PathVariable String id, HttpServletRequest request) {
        return redirect(id + ".xml", request);
    }

    @GetMapping("{id}")
    public RedirectView redirectToResource(@PathVariable String id, HttpServletRequest request) {
        return redirect(id, request);
    }

    private RedirectView redirect(String path, HttpServletRequest request) {
        UriComponentsBuilder url = ServletUriComponentsBuilder
            .fromRequest(request)
            .replacePath("documents/{path}");
        val redirectView = new RedirectView(url.buildAndExpand(path).toUriString());
        redirectView.setStatusCode(HttpStatus.SEE_OTHER);
        return redirectView;
    }
}
