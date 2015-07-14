package uk.ac.ceh.gateway.catalogue.controllers;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        }
    }
}
