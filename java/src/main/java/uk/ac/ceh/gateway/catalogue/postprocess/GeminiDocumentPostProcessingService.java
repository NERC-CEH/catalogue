package uk.ac.ceh.gateway.catalogue.postprocess;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.jena.query.*;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.citation.CitationService;
import uk.ac.ceh.gateway.catalogue.datacite.DataciteService;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Defines a post processing service which can be used adding additional
 * information to a Gemini Document
 */
@Slf4j
@ToString
public class GeminiDocumentPostProcessingService implements PostProcessingService<GeminiDocument> {
    private final CitationService citationService;
    private final DataciteService dataciteService;
    private final Dataset jenaTdb;
    private final DocumentIdentifierService documentIdentifierService;

    public GeminiDocumentPostProcessingService(
        CitationService citationService,
        DataciteService dataciteService,
        Dataset jenaTdb,
        DocumentIdentifierService documentIdentifierService) {
        this.citationService = citationService;
        this.dataciteService = dataciteService;
        this.jenaTdb = jenaTdb;
        this.documentIdentifierService = documentIdentifierService;
        log.info("Creating {}", this);
    }

    @Override
    public void postProcess(GeminiDocument document) {
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
                .ifPresent(document::setCitation);

        document.setDataciteMintable(dataciteService.isDataciteMintable(document));
        document.setDatacitable(dataciteService.isDatacitable(document, true));

    }

    private void process(GeminiDocument document, String id) {
        document.setIncomingRelationships(findLinksWhere(id, eidcIncomingRelationships()));
    }

    private Set<Link> findLinksWhere(String identifier, ParameterizedSparqlString pss) {
        pss.setIri("me", identifier);
        val toReturn = new HashSet<Link>();
        try (QueryExecution qexec = QueryExecutionFactory.create(pss.asQuery(), jenaTdb)) {
            qexec.execSelect().forEachRemaining(s -> toReturn.add(Link.builder()
                    .associationType(s.getLiteral("type").getString())
                    .href(s.getResource("node").getURI())
                    .rel(s.getResource("rel").getURI())
                    .title(s.getLiteral("title").getString())
                    .build()));
        }
        return toReturn;
    }

    /**
     * @return sparql query to find relationships defined by the EIDC ontology
     */
    @SuppressWarnings("HttpUrlsUsage")
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
