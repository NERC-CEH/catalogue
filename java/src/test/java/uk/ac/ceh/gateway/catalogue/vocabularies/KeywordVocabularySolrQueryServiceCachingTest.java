package uk.ac.ceh.gateway.catalogue.vocabularies;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.request.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Optional;

import static org.apache.solr.client.solrj.SolrRequest.METHOD.POST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.ac.ceh.gateway.catalogue.vocabularies.KeywordVocabularySolrQueryService.EXACT_LABEL_CACHE;

/**
 * Exercises {@code resolveExactLabel} through a real caching proxy. The annotation's
 * explicit key and its condition are Spring-evaluated, so neither is covered by a plain
 * unit test: an argument-less or mis-typed key on a String-keyed cache fails only at
 * runtime, and a record renders one lookup per URI-less keyword, so a proxy that quietly
 * failed to cache would cost a Solr round trip apiece.
 */
@SpringJUnitConfig
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Caching of keyword label resolution (dri-one #321)")
class KeywordVocabularySolrQueryServiceCachingTest {

    private static final String CONCEPT = "https://www.eionet.europa.eu/gemet/concept/7842";

    @Configuration
    @EnableCaching
    static class CachingTestConfig {

        @Bean
        CacheManager cacheManager() {
            val cacheManager = new CaffeineCacheManager();
            cacheManager.registerCustomCache(EXACT_LABEL_CACHE, Caffeine.newBuilder().build());
            return cacheManager;
        }

        @Bean
        SolrClient solrClient() {
            return mock(SolrClient.class);
        }

        @Bean
        KeywordVocabularySolrQueryService keywordVocabularySolrQueryService(SolrClient solrClient) {
            return new KeywordVocabularySolrQueryService(solrClient);
        }
    }

    @Autowired private SolrClient solrClient;
    @Autowired private KeywordVocabularySolrQueryService service;

    @SneakyThrows
    private void givenSolrReturns(List<Keyword> beans) {
        val response = mock(QueryResponse.class);
        given(solrClient.query(anyString(), any(SolrQuery.class), eq(POST))).willReturn(response);
        given(response.getBeans(Keyword.class)).willReturn(beans);
    }

    @Test
    @SneakyThrows
    @DisplayName("the same label is resolved against Solr only once")
    void repeatedLookupsOfOneLabelReachSolrOnce() {
        givenSolrReturns(List.of(new Keyword("Soil moisture", "GEMET", CONCEPT)));

        val first = service.resolveExactLabel("Soil moisture");
        val second = service.resolveExactLabel("Soil moisture");

        assertThat(first.map(Keyword::getUrl), equalTo(Optional.of(CONCEPT)));
        assertThat(second, equalTo(first));
        verify(solrClient, times(1)).query(anyString(), any(SolrQuery.class), eq(POST));
    }

    @Test
    @SneakyThrows
    @DisplayName("the cache is keyed on the label, so different labels are resolved separately")
    void differentLabelsAreKeyedSeparately() {
        givenSolrReturns(List.of(new Keyword("Soil moisture", "GEMET", CONCEPT)));

        service.resolveExactLabel("Soil moisture");
        service.resolveExactLabel("Rainfall rate");

        verify(solrClient, times(2)).query(anyString(), any(SolrQuery.class), eq(POST));
    }

    @Test
    @SneakyThrows
    @DisplayName("text that cannot identify anything is neither cached nor sent to Solr")
    void blankLabelNeverReachesSolr() {
        assertThat(service.resolveExactLabel("   "), is(Optional.empty()));

        verifyNoInteractions(solrClient);
    }
}
