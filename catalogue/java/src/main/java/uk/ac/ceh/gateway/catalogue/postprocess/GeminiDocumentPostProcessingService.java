package uk.ac.ceh.gateway.catalogue.postprocess;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.Link;
import uk.ac.ceh.gateway.catalogue.services.CitationService;

/**
 * Defines a post processing service which can be used adding additional 
 * information to a Gemini Document
 * @author cjohn
 */
@Data
public class GeminiDocumentPostProcessingService implements PostProcessingService<GeminiDocument> {
    private final CitationService citationService;
    private final Dataset jenaTdb;
    
    @Override
    public void postProcess(GeminiDocument document) {
        Optional.ofNullable(document.getParentIdentifier())
                .ifPresent(i -> document.setParent(getLink(i)));
        
        Optional.ofNullable(document.getId())
                .ifPresent(i -> document.setChildren(getReverseLinks(i)));
        
        Optional.ofNullable(document.getCoupledResources())
                .ifPresent(r -> document.setDocumentLinks(r.stream().map(c -> getLink(c)).collect(Collectors.toSet())));
        
        citationService.getCitation(document)
                .ifPresent(c -> document.setCitation(c));
    }
    
    protected Link getLink(String identifier) {
        ParameterizedSparqlString pss = new ParameterizedSparqlString("SELECT ?node ?title ?type WHERE {"
                + "?node <http://purl.org/dc/terms/identifier> ?id . "
                + "?node <http://purl.org/dc/terms/title> ?title . "
                + "?node <http://purl.org/dc/terms/type> ?type .}");
        
        pss.setLiteral("id", identifier);

        try (QueryExecution qexec = QueryExecutionFactory.create(pss.asQuery(), jenaTdb)) {
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
    
    protected Set<Link> getReverseLinks(String identifier) {
        ParameterizedSparqlString pss = new ParameterizedSparqlString("SELECT ?node ?title ?type WHERE {"
                + "?parent <http://purl.org/dc/terms/identifier> ?id . "
                + "?node <http://purl.org/dc/terms/isPartOf> ?parent . "
                + "?node <http://purl.org/dc/terms/title> ?title . "
                + "?node <http://purl.org/dc/terms/type> ?type . }");
        
        
        pss.setLiteral("id", identifier);
        
        Set<Link> toReturn = new HashSet<>();
        try (QueryExecution qexec = QueryExecutionFactory.create(pss.asQuery(), jenaTdb)) {
            qexec.execSelect().forEachRemaining(s -> {
              toReturn.add(Link.builder()
                      .associationType(s.getLiteral("type").getString())
                      .href(s.getResource("node").getURI())
                      .title(s.getLiteral("title").getString())
                      .build());
            });
        }
        return toReturn;
    }
}
