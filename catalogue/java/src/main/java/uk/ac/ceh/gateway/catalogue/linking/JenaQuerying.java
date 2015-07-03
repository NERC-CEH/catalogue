package uk.ac.ceh.gateway.catalogue.linking;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import uk.ac.ceh.gateway.catalogue.gemini.Link;
/**
 *
 * @author cjohn
 */
@Data
public class JenaQuerying {
    private final Model model;
    
    public Link getLink(String identifier) {
        ParameterizedSparqlString pss = new ParameterizedSparqlString("SELECT ?node ?title ?type WHERE {"
                + "?node <http://purl.org/dc/terms/identifier> ?id . "
                + "?node <http://purl.org/dc/terms/title> ?title . "
                + "?node <http://purl.org/dc/terms/type> ?type ."
                + "}");
        
        pss.setLiteral("id", identifier);

        try (QueryExecution qexec = QueryExecutionFactory.create(pss.asQuery(), model)) {
            ResultSet results = qexec.execSelect();
            if(results.hasNext()) {
              QuerySolution soln = results.nextSolution();
              return Link.builder().associationType(soln.getLiteral("type").getString())
                      .href(soln.getResource("node").getURI())
                      .title(soln.getLiteral("title").getString())
                      .build();
            }
        }
        return null;
    }
    
    public List<Link> getReverseLinks(String identifier) {
        ParameterizedSparqlString pss = new ParameterizedSparqlString("SELECT ?node ?title ?type WHERE {"
                + "?parent <http://purl.org/dc/terms/identifier> ?id . "
                + "?node <http://purl.org/dc/terms/isPartOf> ?parent . "
                + "?node <http://purl.org/dc/terms/title> ?title . "
                + "?node <http://purl.org/dc/terms/type> ?type ."
                + "}");
        
        
        pss.setLiteral("id", identifier);
        
        List<Link> toReturn = new ArrayList<>();
        try (QueryExecution qexec = QueryExecutionFactory.create(pss.asQuery(), model)) {
            ResultSet results = qexec.execSelect();
            while(results.hasNext()) {
              QuerySolution soln = results.nextSolution();
              toReturn.add(Link.builder().associationType(soln.getLiteral("type").getString())
                      .href(soln.getResource("node").getURI())
                      .title(soln.getLiteral("title").getString())
                      .build());
            }
        }
        return toReturn;
    }
}
