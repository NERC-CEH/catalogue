package uk.ac.ceh.gateway.catalogue.sparql;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "SPARQL", description = "SPARQL query interface for the catalogue knowledge graph")
public class SparqlUiController {

    private final String sparqlEndpoint;
    private final String catalogueGraph;

    public SparqlUiController(
        @Value("${fuseki.sparqlEndpoint}") String sparqlEndpoint,
        @Value("${documents.baseUri}") String catalogueGraph
    ) {
        this.sparqlEndpoint = sparqlEndpoint;
        // FusekiExportService PUTs the catalogue's Turtle to ?graph=<baseUri>, so
        // this is the graph name the example queries have to use. Passed in rather
        // than written into the page, which would break on dev and staging.
        this.catalogueGraph = catalogueGraph;
        log.info("Creating");
    }

    @Operation(
        summary = "SPARQL query UI",
        description = "Interactive SPARQL query interface for the catalogue knowledge graph. " +
            "Supports SELECT, CONSTRUCT, DESCRIBE, and ASK queries."
    )
    @GetMapping("sparql")
    public String sparqlUiPage(Model model) {
        model.addAttribute("sparqlEndpoint", sparqlEndpoint);
        model.addAttribute("catalogueGraph", catalogueGraph);
        return "html/sparql-ui";
    }
}
