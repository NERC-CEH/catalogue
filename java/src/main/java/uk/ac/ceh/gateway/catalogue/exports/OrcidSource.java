package uk.ac.ceh.gateway.catalogue.exports;

import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The researchers the catalogue names, as they describe themselves.
 *
 * <p>This is the source that pays for dri-one #348. That change stopped the
 * export writing record-derived names onto ORCID URIs, which was right — 281 of
 * them had accumulated more than one name, one asserting a different person
 * entirely — but it left 2,125 ORCID nodes carrying nothing but a type. Here
 * they get the researcher's own name instead of a depositor's spelling of it.
 *
 * <p>ORCID publishes RDF by content negotiation, so no hand mapping is needed —
 * only reducing the record to the properties that describe a person. It has no
 * bulk endpoint, so a researcher is one request, which is why the budget and the
 * cache in {@link AuthorityRetriever} matter here more than anywhere.
 */
@Profile("exports")
@Component
class OrcidSource implements AuthoritySource {

    private static final String PREFIX = "https://orcid.org/";
    private static final String FOAF = SourceGraphs.FOAF;

    /**
     * What an ORCID record may contribute. Deliberately narrow: ORCID's RDF also
     * describes the profile document, the account node and its update history,
     * none of which is a statement about the person. Note ORCID publishes no
     * email address at all, which is why #348's removal of 2,429 of them from
     * these URIs could never have been corroborated.
     */
    private static final Set<Property> PUBLISHED = Set.of(
        RDFS.label,
        ResourceFactory.createProperty(FOAF + "givenName"),
        ResourceFactory.createProperty(FOAF + "familyName")
    );

    private final int requestsPerRun;

    OrcidSource(@Value("${orcid.requestsPerRun:500}") int requestsPerRun) {
        this.requestsPerRun = requestsPerRun;
    }

    @Override
    public String graph() {
        return PREFIX;
    }

    @Override
    public String title() {
        return "ORCID, as published by the researchers themselves";
    }

    @Override
    public String description() {
        return "Researchers as they describe themselves in ORCID: their own name, given and family.";
    }

    @Override
    public List<String> vocabularies() {
        return List.of(FOAF, RDFS.getURI());
    }

    @Override
    public String licence() {
        // Established, unlike the vocabularies': ORCID releases its public
        // records under CC0.
        return SourceGraphs.CC0;
    }

    @Override
    public boolean describes(String iri) {
        // An ORCID's account node (…#orcid-id) is referenced by ORCID's own RDF,
        // not by us, and is not a person.
        return iri.startsWith(PREFIX) && !iri.contains("#");
    }

    @Override
    public Duration maxAge() {
        // A fortnight: names and affiliations change, but rarely, and at this
        // volume it still means roughly 200 requests a day rather than 2,125.
        return Duration.ofDays(14);
    }

    @Override
    public int requestsPerRun() {
        return requestsPerRun;
    }

    @Override
    public Request request(List<String> batch) {
        // ORCID content-negotiates, so the IRI is the request. It 302s to
        // pub.orcid.org, which the authorities RestTemplate follows.
        return Request.get(batch.getFirst(), "text/turtle");
    }

    @Override
    public Map<String, Model> describe(List<String> batch, String body) {
        val iri = batch.getFirst();
        // A parse failure is deliberately not caught. The likeliest causes are
        // an error page or a partial response, not a record that will always be
        // unreadable, so it must count as transient and hold the graph back --
        // which is what the retriever does with anything thrown from here.
        val parsed = ModelFactory.createDefaultModel();
        RDFDataMgr.read(parsed, new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)),
            Lang.TURTLE);

        val person = parsed.getResource(iri);
        val description = ModelFactory.createDefaultModel();
        description.add(description.getResource(iri), RDF.type,
            description.getResource(FOAF + "Person"));
        parsed.listStatements(person, null, (RDFNode) null).forEachRemaining(statement -> {
            if (PUBLISHED.contains(statement.getPredicate()) && statement.getObject().isLiteral()) {
                description.add(statement);
            }
        });
        return Map.of(iri, description);
    }
}
