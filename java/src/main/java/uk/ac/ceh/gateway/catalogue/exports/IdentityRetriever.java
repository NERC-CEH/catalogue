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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
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
    private static final String ORG = "https://www.w3.org/ns/org#";

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
     * <p>{@code deferred} is the number of entities this run never asked about
     * at all, because the budget ran out before reaching them and no copy of any
     * age was held. It is deliberately separate from the entities that were
     * asked about and could not be reached: those we can do nothing more for,
     * whereas a deferred entity will simply be described by a later run.
     *
     * <p>That distinction is what tells a caller whether the model in hand is
     * the best that can currently be had, or merely the first slice of a cache
     * that is still filling. Only the caller can decide what to do about it —
     * see {@link IdentityGraphService#graphs}.
     */
    public record Descriptions(Model model, int deferred) {
        public boolean isEmpty() {
            return model.isEmpty();
        }
    }

    /**
     * @param uris      the entities to describe, all from one authority
     * @param authority which authority they belong to
     * @return a model describing as many of them as could be obtained, from the
     *         cache where it is fresh and from the authority otherwise, together
     *         with the number left for a later run
     */
    public Descriptions describe(Collection<String> uris, Authority authority) {
        val combined = ModelFactory.createDefaultModel();
        var fetched = 0;
        var cached = 0;
        var failed = 0;

        val budget = budgetFor(authority);
        var deferred = 0;

        for (val uri : uris) {
            val fresh = cache.get(uri, MAX_AGE);
            if (fresh.isPresent()) {
                combined.add(fresh.get());
                cached++;
                continue;
            }
            if (fetched >= budget) {
                // Out of budget for this run. Any copy we hold is still better
                // than nothing, and the rest are picked up by the next run.
                val held = cache.get(uri, Duration.ofDays(365 * 100));
                held.ifPresent(combined::add);
                deferred++;
                continue;
            }
            val retrieved = retrieve(uri, authority);
            if (retrieved != null) {
                cache.put(uri, retrieved);
                combined.add(retrieved);
                fetched++;
                continue;
            }
            // Unreachable. A copy of any age beats nothing: a name from a
            // fortnight ago is still that person's name.
            val stale = cache.get(uri, Duration.ofDays(365 * 100));
            if (stale.isPresent()) {
                combined.add(stale.get());
                cached++;
            } else {
                failed++;
            }
        }
        log.info("{}: {} fetched, {} from cache, {} deferred to a later run, {} unavailable",
            authority, fetched, cached, deferred, failed);
        if (fetched > 0) {
            // The only thing that changes the cache is a fetch, so this is the
            // only point at which the snapshot needs rewriting.
            cache.save();
        }
        return new Descriptions(combined, deferred);
    }

    private Model retrieve(String uri, Authority authority) {
        return authority == Authority.ROR ? fetchRor(uri) : fetchOrcid(uri);
    }

    private Model fetchOrcid(String uri) {
        val body = get(uri, "text/turtle", "");
        if (body == null) {
            return null;
        }
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
            return description;
        } catch (Exception ex) {
            log.debug("Could not read ORCID RDF for {}: {}", uri, ex.getMessage());
            return null;
        }
    }

    /**
     * ROR publishes JSON, so this is a hand mapping rather than a filter. Only
     * the fields that describe the organisation are taken: its names, where it
     * is, when it was established, its website, and the identifiers it is known
     * by elsewhere. ROR's own administrative bookkeeping is left behind.
     */
    private Model fetchRor(String uri) {
        val id = uri.substring(uri.lastIndexOf('/') + 1);
        val body = get(ROR_API + id, "application/json", rorClientId);
        if (body == null) {
            return null;
        }
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
                    description.add(organisation, propertyOf(ORG + "alternateName"), value);
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

            return description;
        } catch (Exception ex) {
            log.debug("Could not read ROR JSON for {}: {}", uri, ex.getMessage());
            return null;
        }
    }

    private String get(String url, String accept, String clientId) {
        try {
            val headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.valueOf(accept)));
            if (!clientId.isBlank()) {
                headers.set(ROR_CLIENT_ID_HEADER, clientId);
            }
            val response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            val body = response.getBody();
            return body == null || body.isBlank() ? null : body;
        } catch (Exception ex) {
            // Deliberately wide: an authority can fail in every way an HTTP call
            // can, and none of it should stop the other entities or the export.
            log.debug("Could not reach {}: {}", url, ex.getMessage());
            return null;
        }
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
