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
        document.setIncomingRelationships(findLinksWhere(id, eidcIncomingRelationships(), ANYREL));
    }
    
    private Set<Link> findLinksWhere(String identifier, ParameterizedSparqlString pss, Property rel) {
        pss.setIri("me", identifier);
        Set<Link> toReturn = new HashSet<>();
        try (QueryExecution qexec = QueryExecutionFactory.create(pss.asQuery(), jenaTdb)) {
            qexec.execSelect().forEachRemaining(s -> {
              toReturn.add(Link.builder()
                      .associationType(s.getLiteral("type").getString())
                      .href(s.getResource("node").getURI())
                      .rel(s.getResource("rel").getURI())
                      .title(s.getLiteral("title").getString())
                      .build());
            });
        }
        return toReturn;
    }
    
    /**
     * @return sparql query to find relationships defined by the EIDC ontology
     */
    private ParameterizedSparqlString eidcIncomingRelationships() {
        return new ParameterizedSparqlString(
            "SELECT ?node ?type ?title ?rel " +
            "WHERE { " +
            "  ?node ?rel ?me . " +
            "  ?node <http://purl.org/dc/terms/title> ?title ; " +
            "        <http://purl.org/dc/terms/type>  ?type . " +
            "FILTER(regex( str(?rel), '^https://vocabs.ceh.ac.uk/eidc#' ) )" +
            "}"
        );
    }
}
