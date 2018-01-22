package uk.ac.ceh.gateway.catalogue.postprocess;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Property;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.services.CitationService;
import uk.ac.ceh.gateway.catalogue.services.DataciteService;
import uk.ac.ceh.gateway.catalogue.services.DocumentIdentifierService;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static uk.ac.ceh.gateway.catalogue.indexing.Ontology.*;

/**
 * Defines a post processing service which can be used adding additional 
 * information to a Gemini Document
 * @author cjohn
 */
public class GeminiDocumentPostProcessingService implements PostProcessingService<GeminiDocument> {
    private final CitationService citationService;
    private final DataciteService dataciteService;
    private final Dataset jenaTdb;
    private final DocumentIdentifierService documentIdentifierService;

    @Autowired
    public GeminiDocumentPostProcessingService(
        CitationService citationService,
        DataciteService dataciteService,
        Dataset jenaTdb,
        DocumentIdentifierService documentIdentifierService) {
        this.citationService = citationService;
        this.dataciteService = dataciteService;
        this.jenaTdb = jenaTdb;
        this.documentIdentifierService = documentIdentifierService;
    }
        
    @Override
    public void postProcess(GeminiDocument document) throws PostProcessingException {
        Optional.ofNullable(document.getId()).ifPresent(u -> {
            String uri = documentIdentifierService.generateUri(u);
            if (jenaTdb.isInTransaction()) {
                process(document, uri);
            } else {
                jenaTdb.begin(ReadWrite.READ);  
                try {
                    process(document, uri);
                } finally {
                    jenaTdb.end();
                }
            }
        });
                
        citationService.getCitation(document)
                .ifPresent(c -> document.setCitation(c));
        
        document.setDataciteMintable(dataciteService.isDataciteMintable(document));
        document.setDatacitable(dataciteService.isDatacitable(document));
        
    }
    
    private void process(GeminiDocument document, String id) {
        findLinksWhere(id, parent(), IS_PART_OF).stream()
                .findFirst().ifPresent( p -> document.setParent(p));

        document.setChildren(findLinksWhere(id, children(), IS_PART_OF));  
        document.setDocumentLinks(findLinksWhere(id, connectedBy(), RELATION));
        document.setComposedOf(findLinksWhere(id, isComposedOf(), IS_PART_OF));
        document.setModelLinks(findLinksWhere(id, models(), REFERENCES));

        findLinksWhere(id, has(), REPLACES).stream()
                .findFirst().ifPresent( r -> document.setRevisionOf(r));

        findLinksWhere(id, isHadBy(), REPLACES).stream()
                .findFirst().ifPresent( r -> document.setRevised(r));
    }
    
    private Set<Link> findLinksWhere(String identifier, ParameterizedSparqlString pss, Property rel) {
        pss.setIri("me", identifier);
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
     * @return a sparql query which finds gemini links which are tightly coupled
     * from the document represented by ?id by ?rel e.g.
     * 
     *    <http://document1> <IDENTIFIER> "doc1ID"
     *    <http://document1> <CONNECTED_TO> <http://document2>
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
            "  ?me ?rel ?node . " +
            "  ?node <http://purl.org/dc/terms/title> ?title ; " +
            "        <http://purl.org/dc/terms/type>  ?type . " +
            "}"
        );
    }
    
    /**
     * @return a sparql query which finds gemini links which are tightly linked 
     * to the concept specified by ?id with relationship ?rel. e.g.
     * 
     *    <http://document1> <IDENTIFIER> "doc1ID"
     *    <http://document1> <CONNECTED_TO> <http://document2>
     *    <http://document2> <IDENTIFIER> "doc2ID"
     *    ...
     * 
     * Looking up document with id "doc1ID" and relationship <CONNECTED_TO> 
     * will locate <http://document2>
     */
    private ParameterizedSparqlString isHadBy() {
        return new ParameterizedSparqlString(
            "SELECT ?node ?type ?title " +
            "WHERE { " +
            "  ?node ?rel ?me . " +
            "  ?node <http://purl.org/dc/terms/title> ?title ; " +
            "        <http://purl.org/dc/terms/type>  ?type . " +
            "}"
        );
    }
    
    /**
     * @return sparql query to find resources linked to a repository
     */
    private ParameterizedSparqlString isComposedOf() {
        return new ParameterizedSparqlString(
            "SELECT ?node ?type ?title " +
            "WHERE { " +
            "  ?node ?rel ?me ; " +
            "        <http://purl.org/dc/terms/title> ?title ; " +
            "        <http://purl.org/dc/terms/type>  ?type . " +
            "  ?me <http://purl.org/dc/terms/type> 'repository' " +
            "}"
        );
    }
    
    /**
     * @return sparql query to find parent resource
     */
    private ParameterizedSparqlString parent() {
        return new ParameterizedSparqlString(
            "SELECT ?node ?type ?title " +
            "WHERE { " +
            "  ?me ?rel ?node . " +
            "  ?node <http://purl.org/dc/terms/title> ?title ; " +
            "        <http://purl.org/dc/terms/type>  ?type . " +
            "   FILTER ( ?type != 'repository' )" +
            "}"
        );
    }
    
    /**
     * @return sparql query to find child resources
     */
    private ParameterizedSparqlString children() {
        return new ParameterizedSparqlString(
            "SELECT ?node ?type ?title " +
            "WHERE { " +
            "  ?node ?rel ?me ; " +
            "        <http://purl.org/dc/terms/title> ?title ; " +
            "        <http://purl.org/dc/terms/type>  ?type . " +
            "   ?me <http://purl.org/dc/terms/type> ?myType" +
            "   FILTER ( ?myType != 'repository' )" +
            "}"
        );
    }
    
    /**
     * @return sparql query to find model resources
     */
    private ParameterizedSparqlString models() {
        return new ParameterizedSparqlString(
            "SELECT ?node ?type ?title " +
            "WHERE { " +
            "  ?node ?rel ?me ; " +
            "        <http://purl.org/dc/terms/title> ?title ; " +
            "        <http://purl.org/dc/terms/type>  ?type . " +
            "   FILTER ( ?type = 'model' )" +
            "}"
        );
    }
    
    /**
     * @return returns the union query of #isHadBy and #has. Essentially this 
     * finds links which are linked from either source or target
     */
    private ParameterizedSparqlString connectedBy() {
        return new ParameterizedSparqlString(
            "SELECT ?node ?type ?title " +
            "WHERE { " +
            "  { ?me ?rel ?node } UNION { ?node ?rel ?me } . " +
            "  ?node <http://purl.org/dc/terms/title> ?title ; " +
            "        <http://purl.org/dc/terms/type>  ?type . " +
            "}"
        );
    }
}
