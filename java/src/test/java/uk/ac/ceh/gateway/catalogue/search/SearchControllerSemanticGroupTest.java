package uk.ac.ceh.gateway.catalogue.search;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import uk.ac.ceh.components.userstore.GroupStore;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.model.CatalogueGroup;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Guards the anonymous-user path through the semantic access check.
 * <p>
 * {@code CrowdGroupStore.getGroups} is annotated {@code @Cacheable(key="#user.username")}, and
 * {@link CatalogueUser#PUBLIC_USER} has a null username, so calling it for an anonymous visitor
 * makes Spring's cache interceptor throw {@code IllegalArgumentException: Null key returned for
 * cache operation}. Every other caller in the codebase — {@code SolrVisibilityFilter} and
 * {@code SearchQuery.setRecordVisibility} — guards with {@code if (user.isPublic())} and never
 * consults the group store for the public user. {@code userCanUseSemantic} did not, so once
 * {@code catalogue.semantic.group} was set on staging EVERY anonymous search returned 500 —
 * keyword searches included, because the flag is computed eagerly to populate
 * {@code semanticEnabled} whether or not {@code semantic=true} was asked for.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SearchController semantic group gating")
class SearchControllerSemanticGroupTest {

    private static final String SEMANTIC_GROUP = "semantic-search";

    @Mock private Searcher searcher;
    @Mock private SemanticSearcher semanticSearcher;
    @Mock private GroupStore<CatalogueUser> groupStore;

    private SearchController controller() {
        return new SearchController(searcher, Optional.of(semanticSearcher), groupStore, SEMANTIC_GROUP);
    }

    private SearchResults searchResults() {
        return new SearchResults(
            2, "nitrogen deposition", 1, 20, "http://localhost/eidc/documents",
            "without", "intersecting", "within", "prev", "next",
            Collections.emptyList(), Collections.emptyList(),
            Catalogue.builder().id("eidc").title("Env Data Centre").url("").contactUrl("").logo("eidc.png").build(),
            Collections.emptyList(), null, "asc", false
        );
    }

    private SearchResults search(CatalogueUser user, boolean semantic) {
        return controller().search(
            user, "eidc", "nitrogen deposition", null, "IsWithin", 1, 20,
            Collections.emptyList(), null, "asc", semantic,
            new MockHttpServletRequest("GET", "/eidc/documents")
        );
    }

    @Test
    @DisplayName("An anonymous search never consults the group store")
    void publicUserNeverReachesTheGroupStore() {
        given(searcher.search(any(), any(), any(), any(), any(), anyInt(), anyInt(), any(), any(), any(), any()))
            .willReturn(searchResults());

        val results = search(CatalogueUser.PUBLIC_USER, false);

        verify(groupStore, never()).getGroups(any());
        assertThat(results.isSemanticEnabled()).isFalse();
    }

    @Test
    @DisplayName("An anonymous request asking for semantic search falls back to keyword search")
    void publicUserAskingForSemanticFallsBackToKeyword() {
        given(searcher.search(any(), any(), any(), any(), any(), anyInt(), anyInt(), any(), any(), any(), any()))
            .willReturn(searchResults());

        search(CatalogueUser.PUBLIC_USER, true);

        verify(groupStore, never()).getGroups(any());
        verify(semanticSearcher, never()).search(any(), any(), any(), any(), any(), anyInt(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("A signed-in member of the semantic group still gets semantic search")
    void memberOfTheGroupStillGetsSemanticSearch() {
        val user = new CatalogueUser("rjsc", "rjsc@ceh.ac.uk");
        given(groupStore.getGroups(user)).willReturn(List.of(new CatalogueGroup(SEMANTIC_GROUP)));
        given(semanticSearcher.search(any(), any(), any(), any(), any(), anyInt(), anyInt(), any(), any()))
            .willReturn(searchResults());

        val results = search(user, true);

        verify(semanticSearcher).search(any(), any(), any(), any(), any(), anyInt(), anyInt(), any(), any());
        assertThat(results.isSemanticEnabled()).isTrue();
    }
}
