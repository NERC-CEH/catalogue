package uk.ac.ceh.gateway.catalogue.exports;

import org.springframework.http.HttpMethod;

import java.net.URI;
import java.util.Map;

/**
 * One HTTP request an authority wants made on its behalf.
 *
 * <p>A source describes the request and {@link AuthorityRetriever} performs it.
 * That split is the point of the whole arrangement: the invariants worth
 * protecting are in the response handling — a 429 must end the run, a transient
 * failure must be distinguishable from a definitive one — so if each source made
 * its own call, those would be reimplemented once per authority.
 *
 * <p>The URI is a {@link URI} and never a {@code String}, and that is not
 * incidental. {@code RestTemplate} treats a String url as a URI <em>template</em>
 * and re-encodes it, which turned the {@code %2F} in a GtR grant reference into
 * {@code %252F}: the search matched nothing and all 259 grants came back
 * undescribed, with a 200 every time. Sources encode their own query parameters,
 * as {@code GtrSource} already did, and the trap is now unrepresentable rather
 * than warned about in a comment.
 */
public record Request(
    HttpMethod method,
    URI uri,
    String accept,
    String contentType,
    String body,
    Map<String, String> headers
) {
    public Request {
        headers = Map.copyOf(headers);
    }

    /** Ask for an entity, saying what representation is wanted. */
    public static Request get(String uri, String accept) {
        return get(uri, accept, Map.of());
    }

    /**
     * As {@link #get(String, String)}, with headers the authority requires —
     * ROR's {@code Client-Id}, and the {@code User-Agent} the Wikidata Query
     * Service blocks clients for omitting.
     */
    public static Request get(String uri, String accept, Map<String, String> headers) {
        return new Request(HttpMethod.GET, URI.create(uri), accept, null, null, headers);
    }

    /** Send a query. Wikidata's CONSTRUCT is too long for a query string. */
    public static Request post(
        String uri, String accept, String contentType, String body, Map<String, String> headers
    ) {
        return new Request(HttpMethod.POST, URI.create(uri), accept, contentType, body, headers);
    }
}
