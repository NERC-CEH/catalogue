package uk.ac.ceh.gateway.catalogue.mcp;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import tools.jackson.databind.ObjectMapper;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;
import uk.ac.ceh.gateway.catalogue.search.Searcher;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * The tool methods used to carry {@code @SneakyThrows}, which threw the checked exceptions of
 * {@code DocumentRepository} and Jackson 2 undeclared. Spring AI treats a checked exception from a
 * tool as a hard failure the calling model never sees, so these tests pin down that a model-provoked
 * condition comes back as an answer instead.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Catalogue MCP tools")
class CatalogueMcpToolsTest {

    @Mock private Searcher searcher;
    @Mock private DocumentRepository documentRepository;
    @Mock private CatalogueService catalogueService;

    private CatalogueMcpTools tools;

    @BeforeEach
    void setUp() {
        tools = new CatalogueMcpTools(
            searcher,
            Optional.empty(),
            documentRepository,
            catalogueService,
            new ObjectMapper()
        );
    }

    @Test
    @DisplayName("A failed read answers the model rather than throwing")
    void readFailureIsReportedToTheModel() throws Exception {
        given(documentRepository.read("broken"))
            .willThrow(new DocumentRepositoryException("Cannot read file: broken", new RuntimeException()));

        assertThat(tools.getDocument("broken")).contains("cannot read document");
    }

    @Test
    @DisplayName("A missing record answers not found rather than throwing NullPointerException")
    void missingRecordIsNotFound() throws Exception {
        given(documentRepository.read("absent")).willReturn(null);

        assertThat(tools.getDocument("absent")).contains("not found");
    }

    @Test
    @DisplayName("An unpublished record is withheld")
    void unpublishedRecordIsWithheld() throws Exception {
        var document = new GeminiDocument();
        document.setMetadata(MetadataInfo.builder().catalogue("eidc").state("draft").build());
        given(documentRepository.read("draft-record")).willReturn(document);

        assertThat(tools.getDocument("draft-record")).contains("not found");
    }

    @Test
    @DisplayName("A published, publicly viewable record is serialised")
    void publishedRecordIsReturned() throws Exception {
        var document = new GeminiDocument();
        document.setId("abc-123");
        document.setTitle("Nitrogen deposition");
        var info = MetadataInfo.builder().catalogue("eidc").state("published").build();
        info.addPermission(Permission.VIEW, MetadataInfo.PUBLIC_GROUP);
        document.setMetadata(info);
        given(documentRepository.read("abc-123")).willReturn(document);

        assertThat(tools.getDocument("abc-123"))
            .contains("abc-123")
            .contains("Nitrogen deposition")
            .doesNotContain("error");
    }

    /**
     * The tool inventory is this server's external contract — a model discovers what it can call
     * from exactly this list, so a tool appearing or disappearing changes behaviour for every
     * client. Enumerated through {@link MethodToolCallbackProvider}, the same way
     * {@code McpServerConfig} builds it, so this reflects what is really exposed.
     * <p>
     * Notably there is no hybrid tool. {@code HybridSearcher} was removed because Solr cannot do
     * the rank fusion it asked for until 10.1; BM25 and KNN are offered as the two separate paths
     * below, and a model wanting both ranks calls both. Restoring a hybrid tool should be a
     * deliberate act that updates this list.
     */
    @Test
    @DisplayName("Exposes exactly the intended tools, and no hybrid search")
    void exposesTheIntendedTools() {
        val names = Arrays.stream(
                MethodToolCallbackProvider.builder().toolObjects(tools).build().getToolCallbacks())
            .map(callback -> callback.getToolDefinition().name())
            .sorted()
            .toList();

        assertThat(names).containsExactly(
            "getDocument", "listCatalogues", "searchCatalogue", "semanticSearch");
    }

    /**
     * Semantic search is the one tool that depends on an optional bean, so without the
     * vector-search profile it has to answer the model rather than throw — the same contract the
     * removed hybrid tool had, and now the only place it applies.
     */
    @Test
    @DisplayName("Semantic search reports itself unconfigured rather than failing")
    void semanticSearchWithoutTheProfileAnswersTheModel() {
        assertThat(tools.semanticSearch("upland river water quality", "eidc"))
            .contains("not configured");
    }

    @Test
    @DisplayName("Catalogues serialise through the auto-configured Jackson 3 mapper")
    void cataloguesAreSerialised() {
        given(catalogueService.retrieveAll()).willReturn(List.of(
            Catalogue.builder().id("eidc").title("EIDC").url("").contactUrl("").logo("").build()
        ));

        assertThat(tools.listCatalogues()).contains("eidc").contains("EIDC");
    }
}
