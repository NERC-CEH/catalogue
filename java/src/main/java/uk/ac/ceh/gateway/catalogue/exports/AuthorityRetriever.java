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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Asks each authority about the entities the catalogue's records cite, and
 * remembers what it said.
 *
 * <p>One loop for all of them. It used to be four, and the duplication was
 * knowing rather than careless — {@code ReferenceRetriever}'s javadoc said it was
 * "deliberately the same machinery as IdentityRetriever rather than a variation
 * on it" — but three invariants then had to be maintained in four places:
 *
 * <ul>
 *   <li>a budget must count <b>attempts</b> rather than successes, or it stops
 *       limiting a failing authority, which is the one condition it exists for;
 *   <li>a <b>429 must end the run's dealings</b> with that authority instead of
 *       continuing to the ceiling;
 *   <li>a <b>transient failure must be distinguishable from a definitive
 *       one</b>, or a mistyped identifier holds a graph back for ever.
 * </ul>
 *
 * <p>Each authority contributes only the two things it alone knows: where to ask
 * ({@link AuthoritySource#request}) and how to read the answer
 * ({@link AuthoritySource#describe}).
 *
 * <p>Nothing here writes the cache snapshot. That is one file covering every
 * authority, so {@code FusekiExportService} writes it once when every provider
 * has finished rather than once per authority.
 */
@Slf4j
@Profile("exports")
@Service
@ToString(exclude = {"restTemplate", "cache"})
public class AuthorityRetriever {

    /**
     * What one run managed to obtain.
     *
     * <p>Two counts, because two different things make a run's model thinner
     * than the authority could have made it, and both mean a later run will do
     * better: {@code deferred} entities were never asked about, because the
     * budget ran out or the authority told us to stop; {@code transientFailures}
     * were asked about and met a failure that may not recur. An entity the
     * authority <em>definitively</em> does not have — a 404 for a mistyped
     * identifier — is counted in neither, because no later run can help it and
     * blocking on it would block the graph for good.
     */
    public record Descriptions(Model model, int deferred, int transientFailures) {
        public boolean isEmpty() {
            return model.isEmpty();
        }

        /**
         * Whether this is the best the authority could currently give. A run
         * that is not complete must not be published: the export writes each
         * graph with a single PUT, which replaces it.
         */
        public boolean isComplete() {
            return deferred == 0 && transientFailures == 0;
        }
    }

    /**
     * How an attempt turned out. The distinction that matters is not success
     * versus failure but whether a later run could do better, because that is
     * what decides whether a graph may be published.
     */
    private enum Outcome { OK, RATE_LIMITED, TRANSIENT, DEFINITIVE }

    private record HttpResponse(Outcome outcome, String body) {}

    /** The age limit for the fallback: any copy at all, however old. */
    private static final Duration FOREVER = ChronoUnit.FOREVER.getDuration();

    private final RestTemplate restTemplate;
    private final DescriptionCache cache;

    public AuthorityRetriever(
        @Qualifier("authorities") RestTemplate restTemplate,
        DescriptionCache cache
    ) {
        this.restTemplate = restTemplate;
        this.cache = cache;
        log.info("Creating");
    }

    /**
     * @param iris   the IRIs to describe, all belonging to {@code source}
     * @param source the authority to ask
     */
    public Descriptions describe(Collection<String> iris, AuthoritySource source) {
        val combined = ModelFactory.createDefaultModel();
        val wanted = new ArrayList<String>();
        var cached = 0;
        var fetched = 0;
        var deferred = 0;
        var transientFailures = 0;
        var definitive = 0;
        var requests = 0;
        var stopped = false;

        // Dropped before anything else touches them. A concept URI is
        // interpolated into a SPARQL VALUES clause, so one containing a brace, a
        // pipe or a backslash makes the whole query a syntax error -- which
        // returns nothing, which the caller's publish-whole-or-not-at-all guard
        // turns into a permanently frozen graph. It is also not a usable cache
        // key, since the cache stores a description in a named graph keyed by it.
        val usable = iris.stream().filter(Iris::isPublishable).sorted().toList();
        if (usable.size() < iris.size()) {
            log.warn("{}: ignoring {} IRIs that are not usable: {}", source.graph(),
                iris.size() - usable.size(),
                iris.stream().filter(iri -> !Iris.isPublishable(iri)).toList());
        }

        for (val iri : usable) {
            val fresh = cache.get(iri, source.maxAge());
            if (fresh.isPresent()) {
                combined.add(fresh.get());
                cached++;
            } else {
                wanted.add(iri);
            }
        }

        val batchSize = Math.max(1, source.batchSize());
        for (var start = 0; start < wanted.size(); start += batchSize) {
            val batch = wanted.subList(start, Math.min(start + batchSize, wanted.size()));

            // Out of budget, or told to stop. Either way these entities are not
            // asked about: any copy we hold is still better than nothing, and
            // the rest are picked up by the next run.
            if (stopped || requests >= source.requestsPerRun()) {
                for (val iri : batch) {
                    if (addHeld(combined, iri)) {
                        cached++;
                    } else {
                        deferred++;
                    }
                }
                continue;
            }

            requests++;
            val response = perform(source, batch);

            if (response.outcome() == Outcome.OK) {
                val described = source.describe(batch, response.body());
                for (val iri : batch) {
                    val description = described.getOrDefault(iri, ModelFactory.createDefaultModel());
                    // Stored even when empty, and deliberately: it says the
                    // authority was reached and had nothing to say, which is
                    // worth remembering rather than retrying every run.
                    cache.put(iri, description);
                    combined.add(description);
                    if (description.isEmpty()) {
                        definitive++;
                    } else {
                        fetched++;
                    }
                }
                continue;
            }

            if (response.outcome() == Outcome.RATE_LIMITED) {
                // The authority has asked us to stop. Continuing to the budget
                // ceiling would be both rude and pointless, so nothing more is
                // asked of it and the remainder is left for tomorrow.
                log.warn("{} is rate limiting us; asking it for nothing more this run",
                    source.graph());
                stopped = true;
            }

            for (val iri : batch) {
                if (addHeld(combined, iri)) {
                    cached++;
                } else if (response.outcome() == Outcome.DEFINITIVE) {
                    definitive++;
                } else {
                    transientFailures++;
                }
            }
        }

        log.info("{}: {} of {} fetched in {} requests, {} from cache, {} deferred, "
                + "{} temporarily unavailable, {} not held by the authority",
            source.graph(), fetched, usable.size(), requests, cached, deferred,
            transientFailures, definitive);
        return new Descriptions(combined, deferred, transientFailures);
    }

    /**
     * Adds any copy held, of any age. A name from a fortnight ago is still that
     * person's name, and it beats dropping them from the graph.
     *
     * @return whether one was held
     */
    private boolean addHeld(Model combined, String iri) {
        val held = cache.get(iri, FOREVER);
        held.ifPresent(combined::add);
        return held.isPresent();
    }

    private HttpResponse perform(AuthoritySource source, List<String> batch) {
        Request request;
        try {
            request = source.request(batch);
        } catch (Exception ex) {
            // A URI built from an authority's data, or from ours. Transient
            // rather than definitive: the likeliest cause is a bug we fix, and a
            // definitive verdict would excuse it from holding the graph back.
            log.warn("Could not build a request for {}: {}", source.graph(), ex.getMessage());
            return new HttpResponse(Outcome.TRANSIENT, null);
        }
        try {
            val headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.valueOf(request.accept())));
            if (request.contentType() != null) {
                headers.setContentType(MediaType.valueOf(request.contentType()));
            }
            request.headers().forEach(headers::set);
            val response = restTemplate.exchange(request.uri(), request.method(),
                new HttpEntity<>(request.body(), headers), String.class);
            val body = response.getBody();
            if (body == null || body.isBlank()) {
                // A 200 with nothing in it. Not an entity that does not exist --
                // more likely a proxy or an error page -- so worth trying again.
                log.warn("Empty body from {}", request.uri());
                return new HttpResponse(Outcome.TRANSIENT, null);
            }
            return new HttpResponse(Outcome.OK, body);
        } catch (HttpClientErrorException ex) {
            return new HttpResponse(clientErrorOutcome(request, ex), null);
        } catch (HttpServerErrorException ex) {
            log.warn("{} returned {}", request.uri(), ex.getStatusCode());
            return new HttpResponse(Outcome.TRANSIENT, null);
        } catch (Exception ex) {
            // Deliberately wide: a timeout, a DNS failure, a reset connection, a
            // response that is not what we asked for. None of it should stop the
            // other entities or the export, and all of it may be different
            // tomorrow.
            log.warn("Could not reach {}: {}", request.uri(), ex.getMessage());
            return new HttpResponse(Outcome.TRANSIENT, null);
        }
    }

    /**
     * A 4xx is the interesting case: only some of them mean "and it will still
     * be 4xx tomorrow".
     */
    private static Outcome clientErrorOutcome(Request request, HttpClientErrorException ex) {
        val status = ex.getStatusCode();
        if (status.value() == HttpStatus.TOO_MANY_REQUESTS.value()) {
            // Logged loudly, and with what the authority told us, because this
            // is the signal that a per-run budget is set wrong.
            val retryAfter = ex.getResponseHeaders() == null
                ? null : ex.getResponseHeaders().getFirst("Retry-After");
            log.warn("{} rate limited us (429){}", request.uri(),
                retryAfter == null ? "" : ", Retry-After: " + retryAfter);
            return Outcome.RATE_LIMITED;
        }
        if (status.value() == HttpStatus.NOT_FOUND.value()
            || status.value() == HttpStatus.GONE.value()) {
            // The authority does not have it. A mistyped ORCID in a record will
            // 404 for ever, so this must not be allowed to hold a graph back.
            log.info("{} is not held by the authority ({})", request.uri(), status);
            return Outcome.DEFINITIVE;
        }
        // 401, 403, 400 and friends: our fault or a misconfiguration, treated as
        // transient so that it holds the graph back and is noticed rather than
        // quietly publishing less.
        log.warn("{} returned {}", request.uri(), status);
        return Outcome.TRANSIENT;
    }
}
