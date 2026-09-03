package uk.ac.ceh.gateway.catalogue.exports;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.DCTerms;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Set;

/**
 * The papers the records cite, as their publishers describe them.
 *
 * <p>882 external DOIs, mostly Elsevier, Wiley and Copernicus. Our own 2,126
 * {@code 10.5285} DOIs are deliberately excluded: they resolve to this
 * catalogue, so content-negotiating them would fetch our own metadata back and
 * republish it as though an authority had said it.
 *
 * <h2>The DOI is not the subject</h2>
 *
 * <p>{@code doi.org} content-negotiates Turtle, so no hand mapping is needed —
 * but Crossref describes the work as {@code http://dx.doi.org/10.1016/…}, the
 * old http {@code dx} form, while the catalogue's graph holds
 * {@code https://doi.org/10.1016/…}. Copying statements as they arrive would
 * produce a graph that joins to nothing, so the work node is found by its DOI
 * and its statements re-subjected onto the IRI the catalogue actually uses.
 *
 * <h2>What is taken, and what is left</h2>
 *
 * <p>Enough to render a citation: title, date, publisher, and the journal, whose
 * own title is included so a consumer does not need a second lookup to find out
 * what a paper was published in. That is still Crossref's statement about
 * Crossref's own node, so attribution stays intact.
 *
 * <p>Authors are deliberately <em>not</em> taken, though Crossref supplies them.
 * Each is a {@code id.crossref.org/contributor/…} node with a name and no
 * ORCID, so importing them would mint a second population of person nodes with
 * no way to join them to the ORCID-identified people phase 3 publishes — undoing
 * exactly the consolidation dri-one #334 achieved when it cut 10,684
 * record-scoped contact nodes down to 3,442 shared ones. If author identity is
 * wanted here, the route is Crossref's ORCID field where it exists, not its
 * contributor slugs.
 */
@Slf4j
@Profile("exports")
@Component
class DoiSource implements ReferenceSource {

    private static final String PREFIX = "https://doi.org/";
    /** Our own prefix. These DOIs resolve here, so asking about them asks us. */
    private static final String OWN_PREFIX = PREFIX + "10.5285/";

    private static final String BIBO = "http://purl.org/ontology/bibo/";
    private static final String PRISM = "http://prismstandard.org/namespaces/basic/2.1/";

    /**
     * What a work's description may contribute. Volume and page numbers are
     * omitted: they matter for a citation string, which is not what this graph
     * is for, and the catalogue already renders citations from its own records.
     */
    private static final Set<Property> PUBLISHED = Set.of(
        DCTerms.title,
        DCTerms.date,
        DCTerms.publisher,
        DCTerms.isPartOf
    );

    @Override
    public String graph() {
        return PREFIX;
    }

    @Override
    public String title() {
        return "Crossref and DataCite, as the publishers of the cited works registered them";
    }

    @Override
    public boolean describes(String iri) {
        return iri.startsWith(PREFIX) && !iri.startsWith(OWN_PREFIX);
    }

    @Override
    public String requestUrl(String iri) {
        return iri;
    }

    @Override
    public String accept() {
        return "text/turtle";
    }

    @Override
    public Duration maxAge() {
        // A published paper's title, journal and date do not change. Refetched
        // only so a correction or a retraction notice is eventually picked up.
        return Duration.ofDays(90);
    }

    @Override
    public int requestsPerRun() {
        // 882 works, so three runs for a first fill against a 90-day window.
        return 300;
    }

    @Override
    public Model describe(String iri, String body) {
        val description = ModelFactory.createDefaultModel();
        val parsed = ModelFactory.createDefaultModel();
        try {
            RDFDataMgr.read(parsed, new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)),
                Lang.TURTLE);
        } catch (Exception ex) {
            log.debug("Response for {} was not readable Turtle: {}", iri, ex.getMessage());
            return description;
        }

        val work = workNode(parsed, iri);
        if (work == null) {
            return description;
        }

        val subject = description.getResource(iri);
        parsed.listStatements(work, null, (RDFNode) null).forEachRemaining(statement -> {
            if (!PUBLISHED.contains(statement.getPredicate()) || statement.getObject().isAnon()) {
                return;
            }
            description.add(subject, statement.getPredicate(), statement.getObject());
            if (DCTerms.isPartOf.equals(statement.getPredicate())
                && statement.getObject().isURIResource()) {
                // The journal, named rather than merely pointed at.
                copyJournal(parsed, statement.getObject().asResource(), description);
            }
        });
        return description;
    }

    /**
     * Finds the node the response describes as the work. Crossref uses the
     * {@code dx.doi.org} http form and DataCite the {@code doi.org} https one,
     * so rather than guess, this looks for whichever subject carries this DOI.
     */
    private static Resource workNode(Model parsed, String iri) {
        val doi = iri.substring(PREFIX.length());
        for (val predicate : new String[]{BIBO + "doi", PRISM + "doi"}) {
            val subjects = parsed.listSubjectsWithProperty(parsed.getProperty(predicate), doi);
            if (subjects.hasNext()) {
                return subjects.next();
            }
        }
        // No doi property. Fall back to the two IRI forms the registration
        // agencies are known to use, before giving up.
        for (val candidate : new String[]{iri, "http://dx.doi.org/" + doi, PREFIX + doi}) {
            val resource = parsed.getResource(candidate);
            if (parsed.listStatements(resource, null, (RDFNode) null).hasNext()) {
                return resource;
            }
        }
        log.debug("No work node found in the response for {}", iri);
        return null;
    }

    private static void copyJournal(Model parsed, Resource journal, Model description) {
        val target = description.getResource(journal.getURI());
        parsed.listStatements(journal, DCTerms.title, (RDFNode) null)
            .forEachRemaining(statement -> description.add(target, DCTerms.title, statement.getObject()));
        parsed.listStatements(journal, parsed.getProperty(BIBO + "issn"), (RDFNode) null)
            .forEachRemaining(statement ->
                description.add(target, description.getProperty(BIBO + "issn"), statement.getObject()));
    }
}
