package uk.ac.ceh.gateway.catalogue.search;

import lombok.extern.slf4j.Slf4j;
import uk.ac.ceh.gateway.catalogue.model.ExternalResourceFailureException;

/**
 * Package-private utility — turns a failure of a search backend into the exception the caller should
 * see. Shared by all four searchers so the guarantee below exists in one place rather than four.
 * <p>
 * The returned message is a fixed string, never the cause's. {@link ExternalResourceFailureException}
 * is rendered into the response body and the search endpoints are reachable anonymously, while both
 * SolrJ and the AWS SDK put internal detail in their own messages — SolrJ prefixes the Solr base URL,
 * the AWS SDK includes request ids. The cause is logged instead, with the catalogue and endpoint
 * needed to find it; before this, these failures surfaced as a bare 500 with a stack trace and no
 * indication of which search had failed.
 * <p>
 * {@code ExternalResourceFailureException} maps to 502, which is what an upstream search backend
 * failing actually is.
 */
@Slf4j
final class SearchBackendFailure {

    private SearchBackendFailure() {}

    static ExternalResourceFailureException unavailable(
            String what,
            String endpoint,
            String catalogueKey,
            Exception cause
    ) {
        log.error("{} failed for catalogue {} on endpoint {}", what, catalogueKey, endpoint, cause);
        return new ExternalResourceFailureException(
                "Search is temporarily unavailable, please try again", cause);
    }
}
