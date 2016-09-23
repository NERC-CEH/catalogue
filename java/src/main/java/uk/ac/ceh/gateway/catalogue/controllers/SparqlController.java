package uk.ac.ceh.gateway.catalogue.controllers;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ceh.gateway.catalogue.model.SparqlResponse;

/**
 *
 * @author cjohn
 */
@Controller
@RequestMapping("maintenance/sparql")
@Secured(DocumentController.MAINTENANCE_ROLE)
public class SparqlController {
    private final Dataset jenaTdb;
    
    @Autowired
    public SparqlController(Dataset jenaTdb) {
        this.jenaTdb = jenaTdb;
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
