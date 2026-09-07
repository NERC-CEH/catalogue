package uk.ac.ceh.gateway.catalogue.exports;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * The organisations the catalogue names, as ROR registers them.
 *
 * <p>ROR publishes an organisation's official name alongside its acronym and
 * aliases, which is what lets a differently-spelled record be recognised as the
 * same institution. It is worth being precise about how far that goes: ROR gives
 * UKCEH as {@code UK Centre for Ecology & Hydrology} and {@code UKCEH}, which
 * covers two of the four spellings dri-one #347 found fragmenting, and has no
 * former-name type at all, so {@code Institute of Terrestrial Ecology} will never
 * come from there. The remaining variants stay a data-cleanup matter.
 *
 * <p>ROR publishes JSON, so this is a hand mapping rather than a filter. Only the
 * fields that describe the organisation are taken: its names, where it is, when
 * it was established, its website, and the identifiers it is known by elsewhere.
 * ROR's own administrative bookkeeping is left behind.
 */
@Slf4j
@Profile("exports")
@Component
class RorSource implements IdentitySource {

    private static final String PREFIX = "https://ror.org/";
    private static final String FOAF = SourceGraphs.FOAF;

    /**
     * ROR's API, pinned to v2 explicitly. The unversioned path currently serves
     * the v2 schema, but the mapping below reads {@code names}, {@code locations}
     * and {@code external_ids} — all v2 shapes — so relying on the default would
     * mean a future change of default silently changing what we publish.
     */
    private static final String API = "https://api.ror.org/v2/organizations/";

    /**
     * ROR identifies a client by this header. Confirmed from the API gateway's
     * own CORS allow-list rather than the documentation, which does not say:
     *
     * <pre>access-control-allow-headers: ...,X-Amz-Security-Token,Client-Id</pre>
     *
     * <p>From Q3 2026 ROR requires one: an identified client keeps the 2,000
     * requests per 5 minutes, an unidentified one drops to 50.
     */
    private static final String CLIENT_ID_HEADER = "Client-Id";

    /**
     * Well-formed enough for a language tag: a primary subtag of letters, then
     * any number of alphanumeric subtags. ROR uses ISO 639-1 today, but this
     * accepts the longer codes BCP 47 allows rather than assuming two letters.
     */
    private static final Pattern LANGUAGE_TAG =
        Pattern.compile("[a-zA-Z]{2,8}(-[a-zA-Z0-9]{1,8})*");

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String clientId;
    private final int unidentifiedRequestsPerRun;

    RorSource(
        @Value("${ror.clientId:}") String clientId,
        @Value("${ror.unidentifiedRequestsPerRun:200}") int unidentifiedRequestsPerRun
    ) {
        this.clientId = clientId;
        this.unidentifiedRequestsPerRun = unidentifiedRequestsPerRun;
        log.info("Creating{}", clientId.isBlank()
            ? " without a ROR client id, so at most %d organisations a run"
                .formatted(unidentifiedRequestsPerRun)
            : "");
    }

    @Override
    public String graph() {
        return PREFIX;
    }

    @Override
    public String title() {
        return "ROR, the Research Organization Registry";
    }

    @Override
    public String description() {
        return "Research organisations as ROR registers them: official name, aliases and acronym, "
            + "country, website, and the identifiers they are known by elsewhere.";
    }

    @Override
    public List<String> vocabularies() {
        // More than ORCID's: a ROR record carries SKOS labels for its aliases,
        // owl:sameAs links to Fundref and Wikidata, and dcterms:spatial.
        return List.of(FOAF, SKOS.getURI(), DCTerms.getURI(), OWL.getURI(), RDFS.getURI());
    }

    @Override
    public String licence() {
        return SourceGraphs.CC0;
    }

    @Override
    public boolean describes(String iri) {
        return iri.startsWith(PREFIX) && !iri.contains("#");
    }

    @Override
    public Duration maxAge() {
        return Duration.ofDays(14);
    }

    /**
     * How many organisations one export may fetch.
     *
     * <p>Configurable because the constraint is not ours to predict: ROR's new
     * limits are announced but not yet enforced, and client id registration is
     * paused, so neither the ceiling nor our ability to raise it is fixed today.
     *
     * <p>Subject to a convergence rule. At 40 a run, 561 organisations take
     * fifteen runs — by which time the ones fetched on day one are already stale
     * again, so the budget goes on refetching them and the tail of the list is
     * never reached at all. 200 a run fills ROR in three, and steady state is
     * then the ~40 a day that age out, which fits inside even the
     * 50-per-5-minutes an unidentified client will get.
     */
    @Override
    public int requestsPerRun() {
        return clientId.isBlank() ? unidentifiedRequestsPerRun : 600;
    }

    @Override
    public Request request(List<String> batch) {
        val iri = batch.getFirst();
        val id = iri.substring(iri.lastIndexOf('/') + 1);
        // The header is plumbed through and left unset when unregistered rather
        // than omitted: ROR's announcement is a delay, not a cancellation, and
        // the only thing needed when registration reopens is the property.
        return Request.get(API + id, "application/json",
            clientId.isBlank() ? Map.of() : Map.of(CLIENT_ID_HEADER, clientId));
    }

    @Override
    public Map<String, Model> describe(List<String> batch, String body) {
        val iri = batch.getFirst();
        // Not caught, for the reason OrcidSource records: an unreadable body is
        // an error page or a partial response, so it is transient rather than
        // ROR saying it holds nothing about this organisation.
        val json = objectMapper.readTree(body);

        val description = ModelFactory.createDefaultModel();
        val organisation = description.getResource(iri);
        description.add(organisation, RDF.type, description.getResource(FOAF + "Organization"));

        for (val name : json.path("names")) {
            val value = name.path("value").asString();
            if (value == null || value.isBlank()) {
                continue;
            }
            // ROR tags each name with the language it is in, and the tag is what
            // makes the alias list usable rather than merely present. UKCEH's
            // record carries a Welsh and a French name alongside the English one;
            // published untagged, "Centre britannique pour l'Écologie et
            // l'Hydrologie" is not an alternative spelling of anything a consumer
            // can act on. Null for the acronym, which is correct -- UKCEH is not
            // English text.
            val literal = literal(description, value, name.path("lang").asString());
            val types = name.path("types");
            // ROR marks one name for display; the rest are aliases and acronyms,
            // which are what let a differently-spelled record be recognised as
            // the same institution.
            if (contains(types, "ror_display")) {
                description.add(organisation, RDFS.label, literal);
                description.add(organisation, propertyOf(FOAF + "name"), literal);
                // Also skos:prefLabel, so an organisation can be looked up
                // exactly as a vocabulary concept is: the source graphs are
                // queried together, and there is no reason for a consumer to need
                // one predicate for a concept's name and another for an
                // organisation's.
                description.add(organisation, SKOS.prefLabel, literal);
            } else {
                // skos:altLabel, not org:alternateName. The Organization Ontology
                // defines no such property -- and its namespace is http, not the
                // https this used -- so the aliases and acronyms that are the
                // whole point of publishing ROR were going out under a term no
                // consumer can resolve.
                description.add(organisation, SKOS.altLabel, literal);
            }
        }

        val established = json.path("established");
        if (established.isNumber()) {
            description.add(organisation, DCTerms.created,
                description.createTypedLiteral(established.asString(),
                    "http://www.w3.org/2001/XMLSchema#gYear"));
        }

        for (val link : json.path("links")) {
            if ("website".equals(link.path("type").asString())) {
                addIfPublishable(description, organisation, propertyOf(FOAF + "homepage"),
                    link.path("value").asString(), iri);
            }
        }

        // The identifiers the organisation is known by elsewhere. These are the
        // links that make the graph join up: a Fundref id connects an
        // organisation to the funder DOIs on grants, and a Wikidata id to the
        // 2,064 Wikidata entities the catalogue already references.
        for (val external : json.path("external_ids")) {
            val type = external.path("type").asString();
            // "preferred" is frequently null -- UKCEH's wikidata id is, while its
            // "all" list holds Q5062417 -- so fall back rather than lose the most
            // useful cross-reference in the record.
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
                // The identifier is ROR's, not ours, and it is concatenated into
                // a URI here -- a stray space in one makes an IRI that Jena will
                // happily write and no consumer can use.
                addIfPublishable(description, organisation, OWL.sameAs, equivalent, iri);
            }
        }

        for (val location : json.path("locations")) {
            val countryCode = location.path("geonames_details").path("country_code").asString();
            if (countryCode != null && !countryCode.isBlank()) {
                addIfPublishable(description, organisation, DCTerms.spatial,
                    "http://publications.europa.eu/resource/authority/country/" + countryCode,
                    iri);
                break;
            }
        }

        return Map.of(iri, description);
    }

    /**
     * A literal carrying the language ROR recorded, where it recorded one.
     *
     * <p>The tag is checked for the same reason IRIs are (see {@link Iris}): a
     * malformed one serialises into Turtle that will not re-parse, and the
     * export's PUT is all-or-nothing, so a single bad tag in one organisation's
     * record would reject the whole graph. Jena does not validate it.
     */
    private static Literal literal(Model model, String value, String lang) {
        if (lang == null || lang.isBlank()) {
            return model.createLiteral(value);
        }
        if (!LANGUAGE_TAG.matcher(lang).matches()) {
            log.warn("Ignoring malformed language tag '{}' on '{}'", lang, value);
            return model.createLiteral(value);
        }
        return model.createLiteral(value, lang);
    }

    /**
     * Adds a statement whose object is an IRI taken from the authority's data,
     * unless that IRI is unusable.
     *
     * <p>Jena writes a bad IRI with only a {@code WARN} and then re-reads it, so
     * without this it reaches the endpoint and each consumer discovers it
     * separately. See {@link Iris}.
     */
    private static void addIfPublishable(
        Model model, Resource subject, Property predicate, String iri, String source
    ) {
        if (!Iris.isPublishable(iri)) {
            if (iri != null && !iri.isBlank()) {
                log.warn("Ignoring unusable IRI '{}' from {}", iri, source);
            }
            return;
        }
        model.add(subject, predicate, model.getResource(iri));
    }

    private static boolean contains(JsonNode array, String value) {
        for (val element : array) {
            if (value.equals(element.asString())) {
                return true;
            }
        }
        return false;
    }

    /**
     * A property, without building a whole {@link Model} to get one — which is
     * what the method this replaced did, inside the names loop above.
     */
    private static Property propertyOf(String uri) {
        return ResourceFactory.createProperty(uri);
    }
}
