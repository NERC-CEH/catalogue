package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;

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
        val requestParm = request.getQueryString() == null ? "" : "?" + request.getQueryString();
        val redirectView = new RedirectView(String.format("/documents/%s%s", path, requestParm));
        redirectView.setStatusCode(HttpStatus.SEE_OTHER);
        return redirectView;
    }
}
