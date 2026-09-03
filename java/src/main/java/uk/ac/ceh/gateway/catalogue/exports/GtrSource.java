package uk.ac.ceh.gateway.catalogue.exports;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * The grants the records acknowledge, as Gateway to Research describes them.
 *
 * <p>259 projects, all referenced as {@code gtr.ukri.org/projects?ref=NE/…} —
 * a query string rather than a path, which is why the request URL has to be
 * built rather than derived.
 *
 * <h2>{@code ?ref=} is silently ignored, and that is dangerous</h2>
 *
 * <p>The obvious request, {@code /gtr/api/projects?ref=NE/R016429/1}, returns
 * HTTP 200 with {@code totalSize: 158712} — every project GtR holds, page one
 * first. The parameter is not rejected, it is discarded. A mapping that took
 * the first project from that response would have described an unrelated grant
 * under our IRI, with no error anywhere to show it: the first probe of this API
 * returned {@code AH/V01241X/1} for a query naming {@code NE/R016429/1}.
 *
 * <p>{@code ?q=} does filter, returning {@code totalSize: 1} here. But it is a
 * <em>search</em>, so a result is a candidate rather than an answer — hence
 * {@link #projectWithRef}, which will only accept a project whose own
 * identifiers carry the exact reference asked for. Trusting position in a
 * search result is the same mistake in a different coat.
 *
 * <h2>Investigators are not taken</h2>
 *
 * <p>Each project links its principal and co-investigators as
 * {@code /gtr/api/persons/<uuid>}. Those are left alone for the reason
 * {@link DoiSource} leaves Crossref's contributors alone: they carry no ORCID,
 * so they would form a third population of person nodes that cannot be joined
 * to the people phase 3 publishes.
 */
@Slf4j
@Profile("exports")
@Component
class GtrSource implements ReferenceSource {

    private static final String PREFIX = "https://gtr.ukri.org/";
    private static final String PROJECT_PREFIX = PREFIX + "projects?ref=";
    private static final String API = PREFIX + "gtr/api/projects?q=";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String graph() {
        return PREFIX;
    }

    @Override
    public String title() {
        return "Gateway to Research, UKRI's record of the projects it funded";
    }

    @Override
    public boolean describes(String iri) {
        return iri.startsWith(PROJECT_PREFIX) && iri.length() > PROJECT_PREFIX.length();
    }

    @Override
    public String requestUrl(String iri) {
        // The reference contains slashes, which must not survive into the query
        // string as path separators.
        return API + URLEncoder.encode(reference(iri), StandardCharsets.UTF_8);
    }

    @Override
    public String accept() {
        return "application/json";
    }

    @Override
    public Duration maxAge() {
        // A grant's title, funder and dates are settled once it is awarded; its
        // status changes when it closes.
        return Duration.ofDays(60);
    }

    @Override
    public int requestsPerRun() {
        // All 259 in one run, comfortably inside the refresh window.
        return 300;
    }

    @Override
    public Model describe(String iri, String body) {
        val description = ModelFactory.createDefaultModel();
        JsonNode json;
        try {
            json = objectMapper.readTree(body);
        } catch (Exception ex) {
            log.debug("Response for {} was not readable JSON: {}", iri, ex.getMessage());
            return description;
        }

        val project = projectWithRef(json, reference(iri));
        if (project == null) {
            return description;
        }

        val grant = description.getResource(iri);
        addLiteral(description, grant, RDFS.label, project.path("title").asString());
        addLiteral(description, grant, DCTerms.title, project.path("title").asString());
        addLiteral(description, grant, DCTerms.identifier, reference(iri));
        addLiteral(description, grant, DCTerms.description, project.path("abstractText").asString());
        addLiteral(description, grant, DCTerms.type, project.path("grantCategory").asString());
        addLiteral(description, grant, DCTerms.contributor, project.path("leadFunder").asString());
        addLiteral(description, grant, DCTerms.provenance, project.path("status").asString());

        // GtR's own classification of what the project is about. Published as
        // labels rather than IRIs because the ids are internal GtR UUIDs that
        // do not dereference.
        for (val group : new String[]{"researchSubjects", "researchTopics"}) {
            val singular = group.substring(0, group.length() - 1);
            for (val topic : project.path(group).path(singular)) {
                addLiteral(description, grant, DCTerms.subject, topic.path("text").asString());
            }
        }

        if (!description.isEmpty()) {
            description.add(grant, RDF.type, description.getResource(PREFIX + "Project"));
        }
        return description;
    }

    /** The grant reference, e.g. {@code NE/R016429/1}. */
    private static String reference(String iri) {
        return iri.substring(PROJECT_PREFIX.length());
    }

    /**
     * The project in a search result that actually carries the reference asked
     * for, or null if none does.
     *
     * <p>The guard that matters. {@code ?q=} is a search and {@code ?ref=} is
     * ignored outright, so position in the response says nothing about identity
     * — and the pipeline caches per entity, meaning one wrong match would be
     * remembered as that grant's description for two months.
     */
    private static JsonNode projectWithRef(JsonNode json, String reference) {
        val projects = json.path("project");
        for (val project : projects) {
            for (val identifier : project.path("identifiers").path("identifier")) {
                if (reference.equals(identifier.path("value").asString())) {
                    return project;
                }
            }
        }
        log.debug("No project carrying reference {} in a response of {} projects",
            reference, projects.size());
        return null;
    }

    private static void addLiteral(
        Model model, Resource subject, org.apache.jena.rdf.model.Property predicate, String value
    ) {
        if (value != null && !value.isBlank()) {
            model.add(subject, predicate, value);
        }
    }
}
