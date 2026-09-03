package uk.ac.ceh.gateway.catalogue.exports;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;

/**
 * Asks each phase 4 authority about the things the catalogue's records cite, and
 * remembers what they said.
 *
 * <p>The pipeline the four {@link ReferenceSource}s share: the cache in front,
 * a per-source budget, the same failure classification the identity authorities
 * use, and the same rule that a copy of any age beats none.
 *
 * <p>It is deliberately the same machinery as {@link IdentityRetriever} rather
 * than a variation on it. That class learned three things the hard way, all of
 * which apply identically here: a budget must count attempts rather than
 * successes or it stops limiting a failing authority; a 429 must end the run's
 * dealings with that authority instead of continuing to the ceiling; and a
 * transient failure must be distinguishable from a definitive one, or a
 * mistyped identifier holds a graph back for ever.
 */
@Slf4j
@Profile("exports")
@Service
@ToString(exclude = {"restTemplate", "cache", "sources"})
public class ReferenceRetriever {

    /** @see IdentityRetriever.Descriptions for why both counts are reported. */
    public record Descriptions(Model model, int deferred, int transientFailures) {
        public boolean isEmpty() {
            return model.isEmpty();
        }

        public boolean isComplete() {
            return deferred == 0 && transientFailures == 0;
        }
    }

    private enum Outcome { OK, RATE_LIMITED, TRANSIENT, DEFINITIVE }

    private record HttpResponse(Outcome outcome, String body) {}

    /** The age limit for the fallback: any copy at all, however old. */
    private static final Duration FOREVER = ChronoUnit.FOREVER.getDuration();

    private final RestTemplate restTemplate;
    private final DescriptionCache cache;
    private final List<ReferenceSource> sources;

    public ReferenceRetriever(
        @Qualifier("authorities") RestTemplate restTemplate,
        DescriptionCache cache,
        List<ReferenceSource> sources
    ) {
        this.restTemplate = restTemplate;
        this.cache = cache;
        this.sources = sources;
        log.info("Creating with {} sources", sources.size());
    }

    /** The sources, so the graph service and the VoID description share one list. */
    public List<ReferenceSource> sources() {
        return List.copyOf(sources);
    }

    /**
     * @param iris   the IRIs to describe, all belonging to {@code source}
     * @param source the authority to ask
     */
    public Descriptions describe(Collection<String> iris, ReferenceSource source) {
        val combined = ModelFactory.createDefaultModel();
        var attempted = 0;
        var fetched = 0;
        var cached = 0;
        var transientFailures = 0;
        var definitive = 0;
        var deferred = 0;
        var stopped = false;

        for (val iri : iris) {
            val fresh = cache.get(iri, source.maxAge());
            if (fresh.isPresent()) {
                combined.add(fresh.get());
                cached++;
                continue;
            }
            if (stopped || attempted >= source.requestsPerRun()) {
                val held = cache.get(iri, FOREVER);
                if (held.isPresent()) {
                    combined.add(held.get());
                    cached++;
                } else {
                    deferred++;
                }
                continue;
            }

            attempted++;
            val response = get(source.requestUrl(iri), source.accept());
            if (response.outcome() == Outcome.OK) {
                val described = source.describe(iri, response.body());
                if (!described.isEmpty()) {
                    cache.put(iri, described);
                    combined.add(described);
                    fetched++;
                    continue;
                }
                // Reached, and had nothing usable to say about it. Recorded so
                // it is not asked again every run, exactly as for a vocabulary
                // concept the authority does not hold.
                log.debug("{} returned nothing usable about {}", source.graph(), iri);
                cache.put(iri, described);
                definitive++;
                continue;
            }
            if (response.outcome() == Outcome.RATE_LIMITED) {
                log.warn("{} is rate limiting us; asking it for nothing more this run", source.graph());
                stopped = true;
            }

            val held = cache.get(iri, FOREVER);
            if (held.isPresent()) {
                combined.add(held.get());
                cached++;
            } else if (response.outcome() == Outcome.DEFINITIVE) {
                definitive++;
            } else {
                transientFailures++;
            }
        }
        log.info("{}: {} of {} fetched, {} from cache, {} deferred, {} temporarily unavailable, "
                + "{} not held by the authority",
            source.graph(), fetched, iris.size(), cached, deferred, transientFailures, definitive);
        if (fetched > 0) {
            cache.save();
        }
        return new Descriptions(combined, deferred, transientFailures);
    }

    private HttpResponse get(String url, String accept) {
        try {
            val headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.valueOf(accept)));
            // URI.create, not the String overload. RestTemplate treats a String
            // url as a URI *template* and re-encodes it, which turns the %2F in
            // a GtR grant reference into %252F -- so the search matches nothing
            // and all 259 grants come back undescribed, with a 200 every time.
            // Verified against the live API: identical request, 16 triples as a
            // URI and 0 as a String.
            val response = restTemplate.exchange(
                URI.create(url), HttpMethod.GET, new HttpEntity<>(headers), String.class);
            val body = response.getBody();
            if (body == null || body.isBlank()) {
                log.warn("Empty body from {}", url);
                return new HttpResponse(Outcome.TRANSIENT, null);
            }
            return new HttpResponse(Outcome.OK, body);
        } catch (HttpClientErrorException ex) {
            return new HttpResponse(clientErrorOutcome(url, ex), null);
        } catch (HttpServerErrorException ex) {
            // What gtr.ukri.org was returning for every path, its own host
            // included, when this was written.
            log.warn("{} returned {}", url, ex.getStatusCode());
            return new HttpResponse(Outcome.TRANSIENT, null);
        } catch (Exception ex) {
            log.warn("Could not reach {}: {}", url, ex.getMessage());
            return new HttpResponse(Outcome.TRANSIENT, null);
        }
    }

    private static Outcome clientErrorOutcome(String url, HttpClientErrorException ex) {
        val status = ex.getStatusCode();
        if (status.value() == HttpStatus.TOO_MANY_REQUESTS.value()) {
            val retryAfter = ex.getResponseHeaders() == null
                ? null : ex.getResponseHeaders().getFirst("Retry-After");
            log.warn("{} rate limited us (429){}", url,
                retryAfter == null ? "" : ", Retry-After: " + retryAfter);
            return Outcome.RATE_LIMITED;
        }
        if (status.value() == HttpStatus.NOT_FOUND.value()
            || status.value() == HttpStatus.GONE.value()) {
            log.info("{} is not held by the authority ({})", url, status);
            return Outcome.DEFINITIVE;
        }
        log.warn("{} returned {}", url, status);
        return Outcome.TRANSIENT;
    }
}
