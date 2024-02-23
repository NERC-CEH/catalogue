package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@SuppressWarnings("SpringMVCViewInspection")
@Slf4j
@ToString
@Controller
public class SparqlUiController {

    private final String sparqlEndpoint;

    public SparqlUiController(@Value("${fuseki.url}") String hostname) {
        this.sparqlEndpoint = hostname + "/ds/sparql";
        log.info("creating {}", this);
    }

    @GetMapping("sparql")
    public String sparqlUiPage(Model model) {
        model.addAttribute("sparqlEndpoint", sparqlEndpoint);
        return "html/sparql-ui";
    }
}
