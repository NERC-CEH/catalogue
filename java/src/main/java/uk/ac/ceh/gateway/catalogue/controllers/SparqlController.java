package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ceh.gateway.catalogue.model.SparqlResponse;

@Slf4j
@ToString
@Controller
@RequestMapping("maintenance/sparql")
@Secured(DocumentController.MAINTENANCE_ROLE)
public class SparqlController {
    private final Dataset jenaTdb;

    public SparqlController(Dataset jenaTdb) {
        this.jenaTdb = jenaTdb;
        log.info("Creating {}", this);
    }
    
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public SparqlResponse getSparqlPage() {
        return new SparqlResponse();
    }
    
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public SparqlResponse executeSparqlQuery(@RequestParam(value = "query") String queryStr) {
        SparqlResponse response = new SparqlResponse();
        response.setQuery(queryStr);
        try {
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
        try ( QueryExecution qExec = QueryExecutionFactory.create(query, jenaTdb)) {
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
