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

import java.time.Duration;
import java.util.regex.Pattern;

/**
 * The monitoring sites the records reference, as DEIMS-SDR describes them.
 *
 * <p>61 sites — the smallest of phase 4's four authorities and the richest per
 * record. DEIMS publishes JSON, so this is a hand mapping.
 *
 * <h2>What is deliberately not taken</h2>
 *
 * <p>A DEIMS site record carries its site manager's name <em>and email
 * address</em>, along with operating and metadata-provider organisations. None
 * of it is published here. dri-one #348 removed 2,429 contact email addresses
 * from this export precisely because the record page withholds them, and
 * reintroducing them through a different authority's API would defeat that for
 * no gain — the addresses are UKCEH staff addresses in many cases, so this is
 * the same personal data by another route.
 *
 * <p>Site boundary polygons are also left out for now. They are ~3 KB of WKT
 * each, the catalogue publishes its own geometry as GeoJSON elsewhere, and a
 * single representative point is enough to place a site on a map or join it to
 * a region. The centroid is published instead, in the same
 * {@code wgs84_pos:lat}/{@code long} form {@link GeoNamesSource} uses, so the
 * two location-bearing sources in this phase agree.
 */
@Slf4j
@Profile("exports")
@Component
class DeimsSource implements ReferenceSource {

    private static final String PREFIX = "https://deims.org/";
    private static final String API = PREFIX + "api/sites/";
    private static final String WGS84 = "http://www.w3.org/2003/01/geo/wgs84_pos#";
    private static final String GN = "http://www.geonames.org/ontology#";

    /** {@code POINT (-3.843425 57.114196)} — longitude first, as WKT requires. */
    private static final Pattern POINT =
        Pattern.compile("POINT\\s*\\(\\s*(-?[0-9.]+)\\s+(-?[0-9.]+)\\s*\\)");

    /** A site id is a UUID; anything else under this host is not a site. */
    private static final Pattern SITE_ID =
        Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String graph() {
        return PREFIX;
    }

    @Override
    public String title() {
        return "DEIMS-SDR, the Dynamic Ecological Information Management System";
    }

    @Override
    public boolean describes(String iri) {
        if (!iri.startsWith(PREFIX)) {
            return false;
        }
        // Not every deims.org URI is a site: the same host serves
        // /networks/<uuid> and /activities/<uuid>, which this API path would
        // 404 on. Matching the shape avoids asking a question we know is wrong.
        return SITE_ID.matcher(iri.substring(PREFIX.length())).matches();
    }

    @Override
    public String requestUrl(String iri) {
        return API + iri.substring(PREFIX.length());
    }

    @Override
    public String accept() {
        return "application/json";
    }

    @Override
    public Duration maxAge() {
        // Actively curated, unlike a published paper or a place name.
        return Duration.ofDays(30);
    }

    @Override
    public int requestsPerRun() {
        // All 61 in one run; there is nothing to spread.
        return 100;
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

        val site = description.getResource(iri);
        val attributes = json.path("attributes");
        val general = attributes.path("general");

        addLiteral(description, site, RDFS.label, json.path("title").asString());
        addLiteral(description, site, DCTerms.title, general.path("siteName").asString());
        addLiteral(description, site, DCTerms.abstract_, general.path("abstract").asString());
        addLiteral(description, site, DCTerms.type, general.path("siteType").asString());
        addLiteral(description, site, DCTerms.created, general.path("yearEstablished").asString());

        // The operational status is a concept in eLTER's vocabulary, so it can
        // be linked rather than restated as text.
        val statusUri = general.path("status").path("uri").asString();
        if (Iris.isPublishable(statusUri)) {
            description.add(site, description.getProperty(GN + "featureCode"),
                description.getResource(statusUri));
        }

        val geographic = attributes.path("geographic");
        addPoint(description, site, geographic.path("coordinates").asString());
        for (val country : geographic.path("country")) {
            addLiteral(description, site, DCTerms.spatial, country.asString());
        }

        // The networks a site belongs to, by their DEIMS identifiers, which is
        // what lets a record be found through its network.
        for (val network : attributes.path("affiliation").path("networks")) {
            val id = network.path("network").path("id");
            val networkIri = id.path("prefix").asString() + id.path("suffix").asString();
            if (Iris.isPublishable(networkIri)) {
                description.add(site, DCTerms.isPartOf, description.getResource(networkIri));
                addLiteral(description, description.getResource(networkIri), RDFS.label,
                    network.path("network").path("name").asString());
            }
        }

        if (!description.isEmpty()) {
            description.add(site, RDF.type, description.getResource(PREFIX + "site"));
        }
        return description;
    }

    private static void addLiteral(
        Model model, Resource subject, org.apache.jena.rdf.model.Property predicate, String value
    ) {
        if (value != null && !value.isBlank()) {
            model.add(subject, predicate, value);
        }
    }

    /**
     * DEIMS gives a site's representative point as WKT, which is longitude
     * first. Getting that order wrong would put every UK site in Somalia, so
     * the two are named rather than positional here.
     */
    private static void addPoint(Model model, Resource subject, String wkt) {
        if (wkt == null || wkt.isBlank()) {
            return;
        }
        val matcher = POINT.matcher(wkt.trim());
        if (!matcher.matches()) {
            log.debug("Unrecognised coordinate form: {}", wkt);
            return;
        }
        val longitude = matcher.group(1);
        val latitude = matcher.group(2);
        model.add(subject, model.getProperty(WGS84 + "lat"), latitude);
        model.add(subject, model.getProperty(WGS84 + "long"), longitude);
    }
}
