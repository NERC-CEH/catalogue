package uk.ac.ceh.gateway.catalogue.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import uk.ac.ceh.gateway.catalogue.CatalogueWebTest;
import uk.ac.ceh.gateway.catalogue.indexing.solr.PendingEmbeddingService;
import uk.ac.ceh.gateway.catalogue.search.SemanticSearcher;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Every consumer of these services injects them as {@code Optional}, so an absent bean is
 * indistinguishable from "vector search is switched off" — semantic search would simply report
 * itself unconfigured, and documents would silently never be embedded. Nothing else asserts they
 * are present when the profile that exists to provide them is active.
 */
@ActiveProfiles({"auth-crowd", "server-eidc", "search-basic", "vector-search"})
@CatalogueWebTest
@DisplayName("Vector search context")
class VectorSearchContextTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("The vector-search profile provides an embedding model")
    void embeddingModelIsWired() {
        assertThat(applicationContext.getBeansOfType(EmbeddingModel.class)).isNotEmpty();
    }

    @Test
    @DisplayName("The semantic searcher and the embedding pipeline are wired alongside it")
    void embeddingConsumersAreWired() {
        assertThat(applicationContext.getBeansOfType(SemanticSearcher.class)).isNotEmpty();
        assertThat(applicationContext.getBeansOfType(PendingEmbeddingService.class)).isNotEmpty();
    }
}
