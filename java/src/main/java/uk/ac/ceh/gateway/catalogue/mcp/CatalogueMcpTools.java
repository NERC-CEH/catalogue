package uk.ac.ceh.gateway.catalogue.mcp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;
import uk.ac.ceh.gateway.catalogue.search.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Search tools exposed to external LLMs over MCP.
 * <p>
 * Tool methods declare no checked exceptions on purpose. Spring AI converts a
 * {@code RuntimeException} thrown by a tool into an error result the calling model can see, while a
 * checked exception bubbles out as a hard failure the model never learns about. So conditions a model
 * can provoke — an unknown identifier, an unconfigured searcher — are answered with an error payload,
 * and genuine faults are left to propagate unchecked. Serialisation needs no handling of its own:
 * Jackson 3's {@code JacksonException} is already unchecked.
 */
@Slf4j
@Component
@Profile("mcp-server")
public class CatalogueMcpTools {

    private final Searcher searcher;
    private final Optional<SemanticSearcher> semanticSearcher;
    private final DocumentRepository documentRepository;
    private final CatalogueService catalogueService;
    private final ObjectMapper objectMapper;

    public CatalogueMcpTools(
            Searcher searcher,
            Optional<SemanticSearcher> semanticSearcher,
            DocumentRepository documentRepository,
            CatalogueService catalogueService,
            ObjectMapper objectMapper
    ) {
        this.searcher = searcher;
        this.semanticSearcher = semanticSearcher;
        this.documentRepository = documentRepository;
        this.catalogueService = catalogueService;
        this.objectMapper = objectMapper;
        log.info("Creating MCP tools");
    }

    @Tool(description = "Keyword (BM25) full-text search across UKCEH catalogue metadata records. Best when the query contains specific terms that should appear in the record. For conceptual queries where the wording may not match, use semanticSearch; the two rank differently and can both be called and the results compared.")
    public String searchCatalogue(
            @ToolParam(description = "Search term, e.g. 'nitrogen deposition'") String term,
            @ToolParam(description = "Catalogue key to scope search, e.g. 'eidc'. Omit to search all catalogues.") String catalogue,
            @ToolParam(description = "Maximum number of results to return (default 20)") Integer rows
    ) {
        int resultRows = rows != null ? rows : 20;
        String catalogueKey = catalogue != null ? catalogue : CatalogueService.ALL_CATALOGUES_ID;
        SearchResults results = searcher.search(
                "mcp", CatalogueUser.PUBLIC_USER, term,
                null, SpatialOperation.ISWITHIN,
                1, resultRows,
                Collections.emptyList(), catalogueKey,
                null, org.apache.solr.client.solrj.request.SolrQuery.ORDER.asc
        );
        return objectMapper.writeValueAsString(toSummary(results));
    }

    @Tool(description = "Semantic similarity search using vector embeddings. Finds conceptually related records even when no keyword matches, so it suits natural language questions. It does not do exact term matching -- for that use searchCatalogue. Requires the vector-search profile.")
    public String semanticSearch(
            @ToolParam(description = "Natural language query, e.g. 'freshwater monitoring in upland areas'") String query,
            @ToolParam(description = "Catalogue key to scope search, e.g. 'eidc'. Omit to search all catalogues.") String catalogue
    ) {
        if (semanticSearcher.isEmpty()) {
            return "{\"error\": \"Semantic search is not configured on this server.\"}";
        }
        String catalogueKey = catalogue != null ? catalogue : CatalogueService.ALL_CATALOGUES_ID;
        SearchResults results = semanticSearcher.get().search(
                "mcp", CatalogueUser.PUBLIC_USER, query,
                null, SpatialOperation.ISWITHIN,
                1, 20, java.util.List.of(), catalogueKey
        );
        return objectMapper.writeValueAsString(toSummary(results));
    }

    @Tool(description = "Retrieve a single metadata record by its identifier")
    public String getDocument(
            @ToolParam(description = "Document identifier (UUID or short ID)") String id
    ) {
        MetadataDocument document;
        try {
            document = documentRepository.read(id);
        } catch (DocumentRepositoryException ex) {
            // A bad identifier is an ordinary outcome of a tool call, so answer the model rather than
            // failing the call — and log the cause, which previously propagated undeclared, unlogged.
            log.warn("MCP getDocument could not read '{}'", id, ex);
            return "{\"error\": \"cannot read document\"}";
        }
        if (document == null) {
            return "{\"error\": \"not found\"}";
        }
        MetadataInfo info = document.getMetadata();
        boolean published = info != null && "published".equalsIgnoreCase(info.getState());
        boolean publiclyVisible = info != null && info.getIdentities(Permission.VIEW).contains("public");
        if (!published || !publiclyVisible) {
            return "{\"error\": \"not found\"}";
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", document.getId());
        result.put("title", document.getTitle());
        result.put("description", document.getDescription());
        result.put("type", document.getType());
        return objectMapper.writeValueAsString(result);
    }

    @Tool(description = "List available catalogues and their identifiers")
    public String listCatalogues() {
        List<Map<String, String>> catalogues = catalogueService.retrieveAll().stream()
                .map(c -> {
                    Map<String, String> entry = new LinkedHashMap<>();
                    entry.put("id", c.getId());
                    entry.put("title", c.getTitle());
                    return entry;
                })
                .collect(Collectors.toList());
        return objectMapper.writeValueAsString(catalogues);
    }

    private Map<String, Object> toSummary(SearchResults results) {
        List<Map<String, String>> docs = results.getResults().stream()
                .map(r -> {
                    Map<String, String> doc = new LinkedHashMap<>();
                    doc.put("id", r.getIdentifier());
                    doc.put("title", r.getTitle());
                    doc.put("description", r.getShortenedDescription());
                    return doc;
                })
                .collect(Collectors.toList());
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("numFound", results.getNumFound());
        summary.put("results", docs);
        return summary;
    }
}
