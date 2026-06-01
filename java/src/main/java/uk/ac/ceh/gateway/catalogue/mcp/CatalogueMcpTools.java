package uk.ac.ceh.gateway.catalogue.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.search.*;

import java.util.*;
import java.util.stream.Collectors;

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

    @Tool(description = "Full-text faceted search across UKCEH catalogue metadata records")
    @SneakyThrows
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

    @Tool(description = "Semantic similarity search using vector embeddings — finds conceptually related records even when keywords don't match")
    @SneakyThrows
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
                1, 20, catalogueKey
        );
        return objectMapper.writeValueAsString(toSummary(results));
    }

    @Tool(description = "Retrieve a single metadata record by its identifier")
    @SneakyThrows
    public String getDocument(
            @ToolParam(description = "Document identifier (UUID or short ID)") String id
    ) {
        var document = documentRepository.read(id);
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
    @SneakyThrows
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
