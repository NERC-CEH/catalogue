package uk.ac.ceh.gateway.catalogue.exports;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Retrieves what ORCID and ROR say about the people and organisations the
 * catalogue names.
 *
 * <p>dri-one #350 phase 3, and the counterpart to {@link SkosConceptRetriever}
 * for identity rather than vocabulary. It is the phase that pays for #348: that
 * change stopped the export writing record-derived names onto ORCID URIs, which
 * left 2,125 of them carrying only a type. This fills that gap with the
 * researcher's own name instead of a depositor's spelling of it.
 *
 * <h2>Two very different sources</h2>
 *
 * <p>ORCID publishes RDF by content negotiation, so its records need no mapping
 * — only reducing to the properties that describe a person. ROR publishes JSON,
 * so its records are mapped by hand. Both are CC0.
 *
 * <h2>Everything goes through the cache</h2>
 *
 * <p>2,686 entities against a daily export is why {@link DescriptionCache}
 * exists. ORCID has no bulk endpoint, so a researcher is one request; asking
 * again every day would be impolite and would add minutes to every run. A
 * description is refetched only once it has aged past {@link #MAX_AGE}.
 *
 * <p>When an authority cannot be reached and there is a stale copy, the stale
 * copy is used. A name from a fortnight ago is a better answer than no name,
 * and a researcher's name is not a fast-moving fact.
 */
@Slf4j
@Profile("exports")
@Service
@ToString(exclude = {"restTemplate", "cache", "objectMapper"})
public class IdentityRetriever {

    /** Which authority an entity URI belongs to, and how to ask it. */
    public enum Authority {
        ORCID("https://orcid.org/"),
        ROR("https://ror.org/");

        private final String uriPrefix;

        Authority(String uriPrefix) {
            this.uriPrefix = uriPrefix;
        }

        public String uriPrefix() {
            return uriPrefix;
        }
    }

    /**
     * How old a cached identity may be before it is fetched again. A fortnight:
     * names and affiliations change, but rarely, and at 2,686 entities this
     * still means roughly 200 requests a day rather than 2,686.
     */
    static final Duration MAX_AGE = Duration.ofDays(14);

    /**
     * ROR's API, pinned to v2 explicitly. The unversioned path currently serves
     * the v2 schema, but the mapping below reads {@code names}, {@code locations}
     * and {@code external_ids} — all v2 shapes — so relying on the default would
     * mean a future change of default silently changing what we publish.
     */
    private static final String ROR_API = "https://api.ror.org/v2/organizations/";

    /**
     * ROR identifies a client by this header. Confirmed from the API gateway's
     * own CORS allow-list rather than the documentation, which does not say:
     *
     *   access-control-allow-headers: ...,X-Amz-Security-Token,Client-Id
     *
     * From Q3 2026 ROR requires one: an identified client keeps the 2,000
     * requests per 5 minutes, an unidentified one drops to 50.
     *
     * <p>Registration is free but currently paused, and ROR says the new limits
     * are not yet being enforced, so the header is plumbed through and left
     * unset rather than omitted — the announcement is a delay, not a
     * cancellation, and the only thing needed when it reopens is the property.
     */
    private static final String ROR_CLIENT_ID_HEADER = "Client-Id";

    private static final String FOAF = "http://xmlns.com/foaf/0.1/";

    /**
     * What an ORCID record may contribute. Deliberately narrow: ORCID's RDF also
     * describes the profile document, the account node and its update history,
     * none of which is a statement about the person. Note ORCID publishes no
     * email address at all, which is why #348's removal of 2,429 of them from
     * these URIs could never have been corroborated.
     */
    private static final Set<Property> ORCID_PUBLISHED = Set.of(
        RDFS.label,
        propertyOf(FOAF + "givenName"),
        propertyOf(FOAF + "familyName")
    );

    /**
     * How an attempt to reach an authority turned out. The distinction that
     * matters is not success versus failure but whether a later run could do
     * better, because that is what decides whether a graph may be published.
     */
    private enum Outcome {
        /** A description was obtained. */
        OK,
        /** The authority asked us to slow down. Nothing more is asked of it this run. */
        RATE_LIMITED,
        /** A timeout, a 5xx, or a response that was not the RDF we asked for. */
        TRANSIENT,
        /** The authority does not hold this entity — a 404 for a mistyped identifier. */
        DEFINITIVE
    }

    private record Retrieved(Outcome outcome, Model model) {
        static Retrieved ok(Model model) {
            return new Retrieved(Outcome.OK, model);
        }

        static Retrieved failed(Outcome outcome) {
            return new Retrieved(outcome, ModelFactory.createDefaultModel());
        }
    }

    /** The age limit for the fallback: any copy at all, however old. */
    private static final Duration FOREVER = ChronoUnit.FOREVER.getDuration();

    private final RestTemplate restTemplate;
    private final DescriptionCache cache;
    private final String rorClientId;
    private final int rorUnidentifiedBudget;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public IdentityRetriever(
        @Qualifier("authorities") RestTemplate restTemplate,
        DescriptionCache cache,
        @Value("${ror.clientId:}") String rorClientId,
        @Value("${ror.unidentifiedRequestsPerRun:200}") int rorUnidentifiedBudget
    ) {
        this.restTemplate = restTemplate;
        this.cache = cache;
        this.rorClientId = rorClientId;
        this.rorUnidentifiedBudget = rorUnidentifiedBudget;
        log.info("Creating{}", rorClientId.isBlank()
            ? " without a ROR client id, so at most %d organisations a run".formatted(rorUnidentifiedBudget)
            : "");
    }

    /**
     * How many entities may be fetched from one authority in a single run.
     *
     * <p>Being under budget is not a failure: the cache persists, so a first
     * fill completes over several daily runs, and once full only the entities
     * that have aged past {@link #MAX_AGE} are fetched at all.
     *
     * <p>But a budget can be set too low to converge, and the interaction with
     * {@link #MAX_AGE} is what decides that. At 40 organisations a run, 561 of
     * them take fifteen runs — by which time the ones fetched on day one are
     * already stale again, so the budget goes on refetching them and the tail of
     * the list is never reached at all. A first fill has to finish comfortably
     * inside {@link #MAX_AGE} or it never finishes. 200 a run fills ROR in three,
     * and steady state is then the ~40 a day that age out, which fits inside even
     * the 50-per-5-minutes an unidentified client will get.
     *
     * <p>That number is configurable because the constraint is not ours to
     * predict: ROR's new limits are announced but not yet enforced, and client id
     * registration is paused, so neither the ceiling nor our ability to raise it
     * is fixed today. See {@code ror.unidentifiedRequestsPerRun}.
     *
     * <p>ORCID publishes no limit we could find and returns no rate-limit
     * headers, so it keeps a deliberately conservative budget — 500 a run still
     * fills its 2,125 people in five.
     */
    private int budgetFor(Authority authority) {
        return switch (authority) {
            case ROR -> rorClientId.isBlank() ? rorUnidentifiedBudget : 600;
            case ORCID -> 500;
        };
    }

    /**
     * What one run of {@link #describe} managed to obtain.
     *
     * <p>Two counts, because two different things make a run's model thinner
     * than the authority could have made it, and both mean a later run will do
     * better:
     *
     * <ul>
     *   <li>{@code deferred} — entities never asked about, because the budget
     *       ran out before reaching them (or the authority told us to stop) and
     *       no copy of any age was held.</li>
     *   <li>{@code transientFailures} — entities asked about, where the failure
     *       was of a kind that may not recur (a timeout, a 5xx, a rate limit, a
     *       response that was not RDF) and again nothing was held.</li>
     * </ul>
     *
     * <p>An entity the authority <em>definitively</em> does not have — a 404 for
     * a mistyped ORCID — is counted in neither, because no later run can help
     * it and blocking on it would block the graph for good.
     */
    public record Descriptions(Model model, int deferred, int transientFailures) {
        public boolean isEmpty() {
            return model.isEmpty();
        }

        /**
         * Whether this is the best the authority could currently give. A run
         * that is not complete must not be published: see
         * {@link IdentityGraphService#graphs}.
         */
        public boolean isComplete() {
            return deferred == 0 && transientFailures == 0;
        }
    }

    /**
     * @param uris      the entities to describe, all from one authority
     * @param authority which authority they belong to
     * @return a model describing as many of them as could be obtained, from the
     *         cache where it is fresh and from the authority otherwise, together
     *         with what was left for a later run
     */
    public Descriptions describe(Collection<String> uris, Authority authority) {
        val combined = ModelFactory.createDefaultModel();
        var attempted = 0;
        var fetched = 0;
        var cached = 0;
        var transientFailures = 0;
        var definitive = 0;
        var deferred = 0;
        var stopped = false;

        val budget = budgetFor(authority);

        for (val uri : uris) {
            val fresh = cache.get(uri, MAX_AGE);
            if (fresh.isPresent()) {
                combined.add(fresh.get());
                cached++;
                continue;
            }
            // Out of budget, or told to stop. Either way this entity is not
            // asked about: any copy we hold is still better than nothing, and
            // the rest are picked up by the next run.
            //
            // The budget counts attempts rather than successes on purpose. When
            // it counted successes, a failing authority cost no budget at all,
            // so the one condition the budget exists for -- being asked to slow
            // down -- was the condition in which it stopped limiting anything.
            if (stopped || attempted >= budget) {
                val held = cache.get(uri, FOREVER);
                if (held.isPresent()) {
                    combined.add(held.get());
                    cached++;
                } else {
                    deferred++;
                }
                continue;
            }

            attempted++;
            val retrieved = retrieve(uri, authority);
            if (retrieved.outcome() == Outcome.OK) {
                cache.put(uri, retrieved.model());
                combined.add(retrieved.model());
                fetched++;
                continue;
            }
            if (retrieved.outcome() == Outcome.RATE_LIMITED) {
                // The authority has asked us to stop. Continuing to the budget
                // ceiling would be both rude and pointless, so nothing more is
                // asked of it this run and the remainder is left for tomorrow.
                log.warn("{} is rate limiting us; asking it for nothing more this run", authority);
                stopped = true;
            }

            // A copy of any age beats nothing: a name from a fortnight ago is
            // still that person's name.
            val held = cache.get(uri, FOREVER);
            if (held.isPresent()) {
                combined.add(held.get());
                cached++;
            } else if (retrieved.outcome() == Outcome.DEFINITIVE) {
                // The authority has no such record. Nothing to publish and
                // nothing a later run can do, so this must not hold the graph.
                definitive++;
            } else {
                transientFailures++;
            }
        }
        log.info("{}: {} fetched, {} from cache, {} deferred, {} temporarily unavailable, "
                + "{} not held by the authority",
            authority, fetched, cached, deferred, transientFailures, definitive);
        if (fetched > 0) {
            // The only thing that changes the cache is a fetch, so this is the
            // only point at which the snapshot needs rewriting.
            cache.save();
        }
        return new Descriptions(combined, deferred, transientFailures);
    }

    private Retrieved retrieve(String uri, Authority authority) {
        return authority == Authority.ROR ? fetchRor(uri) : fetchOrcid(uri);
    }

    private Retrieved fetchOrcid(String uri) {
        val response = get(uri, "text/turtle", "");
        if (response.outcome() != Outcome.OK) {
            return Retrieved.failed(response.outcome());
        }
        val body = response.body();
        try {
            val parsed = ModelFactory.createDefaultModel();
            RDFDataMgr.read(parsed, new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)), Lang.TURTLE);
            val person = parsed.getResource(uri);
            val description = ModelFactory.createDefaultModel();
            description.add(description.getResource(uri), RDF.type, description.getResource(FOAF + "Person"));
            parsed.listStatements(person, null, (RDFNode) null).forEachRemaining(statement -> {
                if (ORCID_PUBLISHED.contains(statement.getPredicate()) && statement.getObject().isLiteral()) {
                    description.add(statement);
                }
            });
            return Retrieved.ok(description);
        } catch (Exception ex) {
            // Not the RDF we asked for. Treated as transient: the likeliest
            // causes are an error page or a partial response, not a record
            // that will always be unreadable.
            log.warn("ORCID response for {} was not readable RDF: {}", uri, ex.getMessage());
            return Retrieved.failed(Outcome.TRANSIENT);
        }
    }

    /**
     * ROR publishes JSON, so this is a hand mapping rather than a filter. Only
     * the fields that describe the organisation are taken: its names, where it
     * is, when it was established, its website, and the identifiers it is known
     * by elsewhere. ROR's own administrative bookkeeping is left behind.
     */
    private Retrieved fetchRor(String uri) {
        val id = uri.substring(uri.lastIndexOf('/') + 1);
        val response = get(ROR_API + id, "application/json", rorClientId);
        if (response.outcome() != Outcome.OK) {
            return Retrieved.failed(response.outcome());
        }
        val body = response.body();
        try {
            val json = objectMapper.readTree(body);
            val description = ModelFactory.createDefaultModel();
            val organisation = description.getResource(uri);
            description.add(organisation, RDF.type, description.getResource(FOAF + "Organization"));

            for (val name : json.path("names")) {
                val value = name.path("value").asString();
                if (value == null || value.isBlank()) {
                    continue;
                }
                val types = name.path("types");
                // ROR marks one name for display; the rest are aliases and
                // acronyms, which are what let a differently-spelled record be
                // recognised as the same institution.
                if (contains(types, "ror_display")) {
                    description.add(organisation, RDFS.label, value);
                    description.add(organisation, propertyOf(FOAF + "name"), value);
                } else {
                    // skos:altLabel, not org:alternateName. The Organization
                    // Ontology defines no such property -- and its namespace is
                    // http, not the https this used -- so the aliases and
                    // acronyms that are the whole point of publishing ROR were
                    // going out under a term no consumer can resolve.
                    description.add(organisation, SKOS.altLabel, value);
                }
            }

            val established = json.path("established");
            if (established.isNumber()) {
                description.add(organisation, propertyOf("http://purl.org/dc/terms/created"),
                    description.createTypedLiteral(established.asString(),
                        "http://www.w3.org/2001/XMLSchema#gYear"));
            }

            for (val link : json.path("links")) {
                if ("website".equals(link.path("type").asString())) {
                    val value = link.path("value").asString();
                    if (value != null && !value.isBlank()) {
                        description.add(organisation, propertyOf(FOAF + "homepage"),
                            description.getResource(value));
                    }
                }
            }

            // The identifiers the organisation is known by elsewhere. These are
            // the links that make the graph join up: a Fundref id connects an
            // organisation to the funder DOIs on grants, and a Wikidata id to
            // the 2,064 Wikidata entities the catalogue already references.
            for (val external : json.path("external_ids")) {
                val type = external.path("type").asString();
                // "preferred" is frequently null — UKCEH's wikidata id is, while
                // its "all" list holds Q5062417 — so fall back rather than lose
                // the most useful cross-reference in the record.
                var preferred = external.path("preferred").asString();
                if (preferred == null || preferred.isBlank()) {
                    val all = external.path("all");
                    preferred = all.isEmpty() ? null : all.get(0).asString();
                }
                if (preferred == null || preferred.isBlank() || type == null) {
                    continue;
                }
                val equivalent = switch (type) {
                    case "fundref" -> "https://doi.org/10.13039/" + preferred;
                    case "wikidata" -> "http://www.wikidata.org/entity/" + preferred;
                    case "isni" -> "https://isni.org/isni/" + preferred.replace(" ", "");
                    case "grid" -> "https://www.grid.ac/institutes/" + preferred;
                    default -> null;
                };
                if (equivalent != null) {
                    description.add(organisation, OWL.sameAs, description.getResource(equivalent));
                }
            }

            for (val location : json.path("locations")) {
                val countryCode = location.path("geonames_details").path("country_code").asString();
                if (countryCode != null && !countryCode.isBlank()) {
                    description.add(organisation, propertyOf("http://purl.org/dc/terms/spatial"),
                        description.getResource("http://publications.europa.eu/resource/authority/country/"
                            + countryCode));
                    break;
                }
            }

            return Retrieved.ok(description);
        } catch (Exception ex) {
            log.warn("ROR response for {} was not readable JSON: {}", uri, ex.getMessage());
            return Retrieved.failed(Outcome.TRANSIENT);
        }
    }

    private record HttpResponse(Outcome outcome, String body) {}

    /**
     * One request, with its failure classified rather than flattened.
     *
     * <p>It used to catch everything, log at {@code debug} and return null. Two
     * problems with that. {@code logging.level.root=warn} means debug is not
     * emitted in production at all, so a rate-limited export was
     * indistinguishable from a healthy one; and a 429 was handled identically
     * to a hostname that does not resolve, when the two call for opposite
     * responses.
     */
    private HttpResponse get(String url, String accept, String clientId) {
        try {
            val headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.valueOf(accept)));
            if (!clientId.isBlank()) {
                headers.set(ROR_CLIENT_ID_HEADER, clientId);
            }
            val response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            val body = response.getBody();
            if (body == null || body.isBlank()) {
                // A 200 with nothing in it. Not a record that does not exist —
                // more likely a proxy or an error page — so worth trying again.
                log.warn("Empty body from {}", url);
                return new HttpResponse(Outcome.TRANSIENT, null);
            }
            return new HttpResponse(Outcome.OK, body);
        } catch (HttpClientErrorException ex) {
            return new HttpResponse(clientErrorOutcome(url, ex), null);
        } catch (HttpServerErrorException ex) {
            log.warn("{} returned {}", url, ex.getStatusCode());
            return new HttpResponse(Outcome.TRANSIENT, null);
        } catch (Exception ex) {
            // Still deliberately wide: a timeout, a DNS failure, a reset
            // connection. None of it should stop the other entities or the
            // export, and all of it may be different tomorrow.
            log.warn("Could not reach {}: {}", url, ex.getMessage());
            return new HttpResponse(Outcome.TRANSIENT, null);
        }
    }

    /**
     * A 4xx is the interesting case: only some of them mean "and it will still
     * be 4xx tomorrow".
     */
    private static Outcome clientErrorOutcome(String url, HttpClientErrorException ex) {
        val status = ex.getStatusCode();
        if (status.value() == HttpStatus.TOO_MANY_REQUESTS.value()) {
            // Logged loudly, and with what the authority told us, because this
            // is the signal that our per-run budget is set wrong.
            val retryAfter = ex.getResponseHeaders() == null
                ? null : ex.getResponseHeaders().getFirst("Retry-After");
            log.warn("{} rate limited us (429){}", url,
                retryAfter == null ? "" : ", Retry-After: " + retryAfter);
            return Outcome.RATE_LIMITED;
        }
        if (status.value() == HttpStatus.NOT_FOUND.value()
            || status.value() == HttpStatus.GONE.value()) {
            // The authority does not have it. A mistyped ORCID in a record will
            // 404 for ever, so this must not be allowed to hold a graph back.
            log.info("{} is not held by the authority ({})", url, status);
            return Outcome.DEFINITIVE;
        }
        // 401, 403, 400 and friends: our fault or a misconfiguration, and
        // treated as transient so that it holds the graph back and is noticed
        // rather than quietly publishing less.
        log.warn("{} returned {}", url, status);
        return Outcome.TRANSIENT;
    }

    private static boolean contains(JsonNode array, String value) {
        for (val element : array) {
            if (value.equals(element.asString())) {
                return true;
            }
        }
        return false;
    }

    private static Property propertyOf(String uri) {
        return ModelFactory.createDefaultModel().getProperty(uri);
    }
}
