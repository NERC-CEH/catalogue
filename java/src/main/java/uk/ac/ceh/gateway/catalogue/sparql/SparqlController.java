package uk.ac.ceh.gateway.catalogue.sparql;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.function.FunctionFactory;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import uk.ac.ceh.gateway.catalogue.controllers.DocumentController;

@Slf4j
@ToString
@RestController
@RequestMapping("maintenance/sparql")
@Secured(DocumentController.MAINTENANCE_ROLE)
public class SparqlController {
    private final Dataset jenaTdb;

    public SparqlController(Dataset jenaTdb) {
        this.jenaTdb = jenaTdb;
        log.info("Creating");
    }

    @GetMapping
    public SparqlResponse getSparqlPage() {
        return new SparqlResponse();
    }

    @PostMapping
    public SparqlResponse executeSparqlQuery(@RequestParam(value = "query") String queryStr) {
        SparqlResponse response = new SparqlResponse();
        response.setQuery(queryStr);
        try {
            log.info("Running query: \n{}", queryStr);
            Query query = QueryFactory.create(queryStr, Syntax.syntaxARQ);
            executeQuery(query, response);
        }
        catch (QueryException ex) {
            response.setError(ex.getMessage());
        }
        return response;
    }

    private void executeQuery(Query query, SparqlResponse response) {
        jenaTdb.begin(ReadWrite.READ);
        ARQ.getContext().set(ARQConstants.registryFunctions, FunctionRegistry.get());
        try ( QueryExecution qExec = QueryExecutionFactory.create(query, jenaTdb)) {
            qExec.getContext().set(ARQConstants.registryFunctions, FunctionRegistry.get());
            FunctionFactory f = FunctionRegistry.get().getFunctionFactory("http://www.opengis.net/def/function/geosparql/distance");
            log.info("Function instance: {}", f.getClass().getName());

            if(query.isSelectType()) {
                response.setResult(ResultSetFormatter.asText(qExec.execSelect()));
            }
            else if(query.isConstructType()) {
                Model construct = qExec.execConstruct();
                response.setResult(construct.toString());
            }
            else if(query.isDescribeType()) {
                Model describe = qExec.execDescribe();
                response.setResult(describe.toString());
            }
            else if(query.isAskType()) {
                boolean ask = qExec.execAsk();
                response.setResult(Boolean.toString(ask));
            }
        } finally {
            jenaTdb.end();
        }
    }
}
