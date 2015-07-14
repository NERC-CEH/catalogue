package uk.ac.ceh.gateway.catalogue.postprocess;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.rdf.model.Property;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.Data;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.Link;
import static uk.ac.ceh.gateway.catalogue.indexing.Ontology.*;
import uk.ac.ceh.gateway.catalogue.services.CitationService;

/**
 * Defines a post processing service which can be used adding additional 
 * information to a Gemini Document
 * @author cjohn
 */
@Data
public class GeminiDocumentPostProcessingService implements PostProcessingService<GeminiDocument> {
    private final CitationService citationService;
    private final ObjectMapper mapper;
    private final Dataset jenaTdb;
    
    @Override
    public void postProcess(GeminiDocument document) throws PostProcessingException {
        Optional.ofNullable(document.getId()).ifPresent(i -> {
            findLinksWhere(i, has(), IS_PART_OF).stream()
                    .findFirst().ifPresent( p -> document.setParent(p));
            
            document.setChildren(findLinksWhere(i, isHadBy(), IS_PART_OF));  
            document.setDocumentLinks(findLinksWhere(i, connectedBy(), RELATION));
            
            findLinksWhere(i, has(), REPLACES).stream()
                    .findFirst().ifPresent( r -> document.setRevisionOf(r));
            
            findLinksWhere(i, isHadBy(), REPLACES).stream()
                    .findFirst().ifPresent( r -> document.setRevised(r));
        });
                
        citationService.getCitation(document)
                .ifPresent(c -> document.setCitation(c));
        
        try {
            document.setJsonString(mapper.writeValueAsString(document));
        }
        catch(JsonProcessingException ex) {
            throw new PostProcessingException(ex);
        }
    }
    
    private Set<Link> findLinksWhere(String identifier, ParameterizedSparqlString pss, Property rel) {
        pss.setLiteral("id", identifier);
        pss.setParam("rel", rel);
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
    
    /**
     * @return a sparql query which finds gemini links which are loosely coupled
     * (based upon the identifier) from the document represented by ?id by ?rel e.g.
     * 
     *    <http://document1> <IDENTIFIER> "doc1ID"
     *    <http://document1> <CONNECTED_TO> "doc2ID"
     *    <http://document2> <IDENTIFIER> "doc2ID"
     *    ...
     * 
     * Looking up document with id "doc1ID" and relationship <CONNECTED_TO> 
     * will locate <http://document2>
     */
    private ParameterizedSparqlString has() {
        return new ParameterizedSparqlString(
            "SELECT ?node ?type ?title " +
            "WHERE { " +
            "  ?me <http://purl.org/dc/terms/identifier> ?id . " +
            "  ?me ?rel ?altid . " +
            "  ?node <http://purl.org/dc/terms/identifier> ?altid ." +
            "  ?node <http://purl.org/dc/terms/title> ?title . " +
            "  ?node <http://purl.org/dc/terms/type> ?type . " +
            "}"
        );
    }
    
    /**
     * @return a sparql query which finds gemini links which are loosely linked 
     * to the concept specified by ?id with relationship ?rel. e.g.
     * 
     *    <http://document1> <IDENTIFIER> "doc1ID"
     *    <http://document1> <CONNECTED_TO> "doc2ID"
     *    <http://document2> <IDENTIFIER> "doc2ID"
     *    ...
     * 
     * Looking up document with id "doc2ID" and relationship <CONNECTED_TO> 
     * will locate <http://document1>
     */
    private ParameterizedSparqlString isHadBy() {
        return new ParameterizedSparqlString(
            "SELECT ?node ?type ?title " +
            "WHERE { " +
            "  ?me <http://purl.org/dc/terms/identifier> ?id . " +
            "  ?me <http://purl.org/dc/terms/identifier> ?altid . " +
            "  ?node ?rel ?altid ." +
            "  ?node <http://purl.org/dc/terms/title> ?title . " +
            "  ?node <http://purl.org/dc/terms/type> ?type . " +
            "}"
        );
    }
    
    /**
     * @return returns the union query of #isHadBy and #has. Essentially this 
     * finds links which are directly or indirectly linked to the given doc (with
     * identifier ?id)
     */
    private ParameterizedSparqlString connectedBy() {
        return new ParameterizedSparqlString(
            "SELECT ?node ?type ?title " +
            "WHERE { " +
            "  ?me <http://purl.org/dc/terms/identifier> ?id . " +
            "  { { ?me ?rel ?altid . ?node <http://purl.org/dc/terms/identifier> ?altid } " +
            "    UNION " +
            "    { ?me <http://purl.org/dc/terms/identifier> ?altid . ?node ?rel ?altid } " +
            "  } . " +
            "  ?node <http://purl.org/dc/terms/title> ?title . " +
            "  ?node <http://purl.org/dc/terms/type> ?type . " +
            "}"
        );
    }
}
