package uk.ac.ceh.gateway.catalogue.exports;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Set;

/**
 * The places the records name, as GeoNames describes them.
 *
 * <p>254 features. GeoNames is the one source in phase 4 that needs no mapping
 * at all — it publishes RDF/XML — and the one where deciding what <em>not</em>
 * to publish is the whole job.
 *
 * <h2>Two things about the URIs</h2>
 *
 * <p>The RDF lives at {@code <iri>/about.rdf} and nowhere else: asking the IRI
 * itself for Turtle returns 79 KB of HTML with a 200, so a content-negotiating
 * fetch would succeed and then fail to parse.
 *
 * <p>And the trailing slash is not fixed. GeoNames describes the feature as
 * {@code …/2635167/} with one; the production graph was built when
 * {@link uk.ac.ceh.gateway.catalogue.templateHelpers.UriNormaliser} stripped it,
 * so its 254 features are slashless, and that host is now {@code APPEND}, so
 * the next export will hold the slashed form instead. Statements are therefore
 * re-subjected onto whichever IRI was asked about rather than onto either form
 * specifically, which is what keeps the graph joining across that change.
 *
 * <p>The cost of the switch is one refetch: descriptions cached under the
 * slashless keys are orphaned, and the 254 features refill over two runs at this
 * source's budget.
 *
 * <h2>Why the names are dropped</h2>
 *
 * <p>A single feature's RDF is 22 KB, and 241 of its 268 properties are
 * {@code gn:alternateName}, {@code gn:officialName} and {@code gn:shortName} in
 * every language GeoNames holds — 133 official names for the United Kingdom
 * alone. Republishing those for 254 features would add roughly 50,000 triples
 * to a catalogue graph of 234,000: a fifth of the entire triplestore, spent on
 * multilingual place names nobody asked for. So only {@code gn:name} is taken,
 * along with what makes a place identifiable and joinable: its type, country,
 * coordinates, population and parent features.
 *
 * <p>If the aliases are ever wanted, the honest way is a separate graph rather
 * than quietly inflating this one.
 */
@Slf4j
@Profile("exports")
@Component
class GeoNamesSource implements ReferenceSource {

    private static final String PREFIX = "https://sws.geonames.org/";
    private static final String GN = "http://www.geonames.org/ontology#";
    private static final String WGS84 = "http://www.w3.org/2003/01/geo/wgs84_pos#";

    /**
     * What a feature's description may contribute. Deliberately excludes the
     * alias properties, and also {@code gn:childrenFeatures} and
     * {@code gn:neighbouringFeatures}, which point at further {@code .rdf}
     * documents rather than stating anything, and {@code gn:locationMap}, which
     * is an HTML page.
     */
    private static final Set<String> PUBLISHED = Set.of(
        GN + "name",
        GN + "featureClass",
        GN + "featureCode",
        GN + "countryCode",
        GN + "population",
        GN + "parentFeature",
        GN + "parentCountry",
        GN + "wikipediaArticle",
        WGS84 + "lat",
        WGS84 + "long",
        RDFS.seeAlso.getURI()
    );

    @Override
    public String graph() {
        return PREFIX;
    }

    @Override
    public String title() {
        return "GeoNames, the geographical database";
    }

    @Override
    public String licence() {
        // Declared in the payload itself as cc:license, so this is GeoNames'
        // own statement rather than our reading of a terms page. Attribution is
        // required, and the graph carries it.
        return "https://creativecommons.org/licenses/by/4.0/";
    }

    @Override
    public boolean describes(String iri) {
        return iri.startsWith(PREFIX);
    }

    @Override
    public String requestUrl(String iri) {
        // The trailing slash matters: /2635167/about.rdf is the document,
        // /2635167about.rdf is not.
        return iri.endsWith("/") ? iri + "about.rdf" : iri + "/about.rdf";
    }

    @Override
    public String accept() {
        return "application/rdf+xml";
    }

    @Override
    public Duration maxAge() {
        // Places move slowly. Populations are revised and boundaries
        // occasionally change, so not never.
        return Duration.ofDays(90);
    }

    @Override
    public int requestsPerRun() {
        // 254 features: two runs for a first fill, and nothing thereafter until
        // they age out.
        return 200;
    }

    @Override
    public Model describe(String iri, String body) {
        val description = ModelFactory.createDefaultModel();
        val parsed = ModelFactory.createDefaultModel();
        try {
            RDFDataMgr.read(parsed, new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)),
                Lang.RDFXML);
        } catch (Exception ex) {
            log.debug("Response for {} was not readable RDF/XML: {}", iri, ex.getMessage());
            return description;
        }

        // GeoNames' own form carries the trailing slash; accept either, since
        // being wrong about which costs the whole description.
        val feature = parsed.listStatements(
                parsed.getResource(iri.endsWith("/") ? iri : iri + "/"), null, (RDFNode) null).hasNext()
            ? parsed.getResource(iri.endsWith("/") ? iri : iri + "/")
            : parsed.getResource(iri);

        val subject = description.getResource(iri);
        description.add(subject, RDF.type, description.getResource(GN + "Feature"));
        parsed.listStatements(feature, null, (RDFNode) null).forEachRemaining(statement -> {
            if (PUBLISHED.contains(statement.getPredicate().getURI())
                && !statement.getObject().isAnon()) {
                description.add(subject, statement.getPredicate(), statement.getObject());
            }
        });
        // Only the type was added, so nothing was actually said about it.
        return description.size() <= 1 ? ModelFactory.createDefaultModel() : description;
    }
}
