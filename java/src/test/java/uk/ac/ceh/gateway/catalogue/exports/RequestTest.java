package uk.ac.ceh.gateway.catalogue.exports;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("One request an authority wants made")
class RequestTest {

    @Test
    @DisplayName("a GET carries the URI and what to accept, and nothing else")
    void get() {
        val request = Request.get("https://orcid.org/0000-0002-0394-2998", "text/turtle");

        assertThat(request.method(), is(HttpMethod.GET));
        assertThat(request.uri().toString(), is("https://orcid.org/0000-0002-0394-2998"));
        assertThat(request.accept(), is("text/turtle"));
        assertThat(request.contentType(), is(nullValue()));
        assertThat(request.body(), is(nullValue()));
        assertThat(request.headers(), is(anEmptyMap()));
    }

    @Test
    @DisplayName("a GET can carry the headers an authority identifies its clients by")
    void getWithHeaders() {
        val request = Request.get("https://api.ror.org/v2/organizations/00pggkr55",
            "application/json", Map.of("Client-Id", "an-id"));

        assertThat(request.headers(), is(Map.of("Client-Id", "an-id")));
    }

    @Test
    @DisplayName("a POST carries a body and its content type")
    void post() {
        val request = Request.post("https://query.wikidata.org/sparql", "text/turtle",
            "application/sparql-query", "CONSTRUCT {}", Map.of("User-Agent", "ukceh"));

        assertThat(request.method(), is(HttpMethod.POST));
        assertThat(request.contentType(), is("application/sparql-query"));
        assertThat(request.body(), is("CONSTRUCT {}"));
    }

    @Test
    @DisplayName("the headers cannot be changed after the fact")
    void headersAreCopied() {
        val mutable = new HashMap<String, String>();
        mutable.put("Client-Id", "an-id");
        val request = Request.get("https://ror.org/", "application/json", mutable);

        mutable.put("Client-Id", "something-else");

        assertThat("a source must not be able to mutate a request after handing it over",
            request.headers(), is(Map.of("Client-Id", "an-id")));
    }

    @Test
    @DisplayName("a string that is not a URI is refused here, not at the socket")
    void badUriIsRefused() {
        // A URI is required rather than a String precisely so that a source
        // cannot hand over something RestTemplate would treat as a template and
        // re-encode -- the %2F in a GtR grant reference became %252F, and all
        // 259 grants came back undescribed with a 200 every time.
        assertThrows(IllegalArgumentException.class,
            () -> Request.get("https://gtr.ukri.org/a space", "application/json"));
    }
}
