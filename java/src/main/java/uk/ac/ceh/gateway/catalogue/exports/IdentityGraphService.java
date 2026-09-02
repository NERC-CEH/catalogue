package uk.ac.ceh.gateway.catalogue.exports;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Publishes what ORCID and ROR say about the people and organisations the
 * catalogue names — dri-one #350 phase 3.
 *
 * <p>This is the phase that pays for #348. That change stopped the export
 * writing record-derived names onto ORCID URIs, which was right — 281 of them
 * had accumulated more than one name, one asserting a different person
 * entirely — but it left 2,125 ORCID nodes carrying nothing but a type. Here
 * they get the researcher's own name instead of a depositor's spelling of it.
 *
 * <p>For organisations the win is different. ROR publishes an organisation's
 * official name alongside its acronym and aliases, which is what lets a
 * differently-spelled record be recognised as the same institution. It is worth
 * being precise about how far that goes: ROR gives UKCEH as
 * {@code UK Centre for Ecology & Hydrology} and {@code UKCEH}, which covers two
 * of the four spellings #347 found fragmenting, and has no former-name type at
 * all, so {@code Institute of Terrestrial Ecology} will never come from there.
 * The remaining variants stay a data-cleanup matter.
 *
 * <p>Both authorities publish under CC0, which is why no {@code dcterms:license}
 * appears on these graphs while the vocabulary ones are still unresolved — the
 * terms here are known, and stated.
 */
@Slf4j
@Profile("exports")
@Service
@ToString(exclude = "identityRetriever")
public class IdentityGraphService implements SourceGraphProvider {

    private static final String VOID = "http://rdfs.org/ns/void#";
    private static final String PROV = "http://www.w3.org/ns/prov#";
    private static final String CC0 = "https://creativecommons.org/publicdomain/zero/1.0/";

    private static final Map<IdentityRetriever.Authority, String> TITLES = Map.of(
        IdentityRetriever.Authority.ORCID,
        "ORCID, as published by the researchers themselves",
        IdentityRetriever.Authority.ROR,
        "ROR, the Research Organization Registry"
    );

    private final IdentityRetriever identityRetriever;
    private final Clock clock;

    /** @see VocabularyGraphService for why this annotation is needed. */
    @Autowired
    public IdentityGraphService(IdentityRetriever identityRetriever) {
        this(identityRetriever, Clock.systemUTC());
    }

    /** Package-private, so a test can fix the clock in the provenance header. */
    IdentityGraphService(IdentityRetriever identityRetriever, Clock clock) {
        this.identityRetriever = identityRetriever;
        this.clock = clock;
        log.info("Creating");
    }

    @Override
    public List<SourceGraph> sourceGraphs() {
        return List.of(
            new SourceGraph(IdentityRetriever.Authority.ORCID.uriPrefix(),
                TITLES.get(IdentityRetriever.Authority.ORCID)),
            new SourceGraph(IdentityRetriever.Authority.ROR.uriPrefix(),
                TITLES.get(IdentityRetriever.Authority.ROR))
        );
    }

    @Override
    public Map<String, String> graphs(Set<String> referencedIris) {
        val turtleByGraph = new LinkedHashMap<String, String>();

        for (val authority : IdentityRetriever.Authority.values()) {
            val wanted = referencedIris.stream()
                .filter(iri -> iri.startsWith(authority.uriPrefix()))
                // An ORCID's account node (…#orcid-id) is referenced by ORCID's
                // own RDF, not by us, and is not a person.
                .filter(iri -> !iri.contains("#"))
                .sorted()
                .toList();
            if (wanted.isEmpty()) {
                continue;
            }

            val described = identityRetriever.describe(wanted, authority);
            if (described.isEmpty()) {
                // Nothing at all, from the authority or the cache. Publishing an
                // empty graph would replace whatever is already there with less.
                log.warn("No identities retrieved for {}, leaving its graph as it is", authority.uriPrefix());
                continue;
            }

            addProvenance(described, authority);
            turtleByGraph.put(authority.uriPrefix(), serialise(described));
        }
        return turtleByGraph;
    }

    private void addProvenance(Model model, IdentityRetriever.Authority authority) {
        val graph = model.getResource(authority.uriPrefix());
        model.add(graph, RDF.type, model.getResource(VOID + "Dataset"));
        model.add(graph, DCTerms.title, TITLES.get(authority));
        model.add(graph, DCTerms.description,
            "Identities as published by the authority, republished unchanged; "
                + "the catalogue asserts nothing of its own here.");
        // Unlike the vocabulary graphs, these terms are established: both ORCID
        // and ROR release their public records under CC0.
        model.add(graph, DCTerms.license, model.getResource(CC0));
        model.add(graph, model.getProperty(PROV + "generatedAtTime"),
            model.createTypedLiteral(
                Instant.now(clock).truncatedTo(ChronoUnit.SECONDS).toString(),
                "http://www.w3.org/2001/XMLSchema#dateTime"));
    }

    private static String serialise(Model model) {
        model.setNsPrefix("foaf", "http://xmlns.com/foaf/0.1/");
        model.setNsPrefix("org", "https://www.w3.org/ns/org#");
        model.setNsPrefix("owl", OWL.getURI());
        model.setNsPrefix("rdfs", RDFS.getURI());
        model.setNsPrefix("dcterms", DCTerms.getURI());
        model.setNsPrefix("void", VOID);
        model.setNsPrefix("prov", PROV);
        model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
        val writer = new StringWriter();
        RDFDataMgr.write(writer, model, Lang.TURTLE);
        return writer.toString();
    }
}
