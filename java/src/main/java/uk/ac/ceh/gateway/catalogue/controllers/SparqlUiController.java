package uk.ac.ceh.gateway.catalogue.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@SuppressWarnings("SpringMVCViewInspection")
@Controller
public class SparqlUiController {

    @GetMapping("sparql")
    public String sparqlUiPage() {
        return "html/sparql-ui";
    }
}
