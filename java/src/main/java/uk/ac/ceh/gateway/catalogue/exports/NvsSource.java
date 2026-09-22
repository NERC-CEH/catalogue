package uk.ac.ceh.gateway.catalogue.exports;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * The NERC Vocabulary Server's concepts.
 *
 * <p>Not in the keyword harvest, so before #350 phase 2 the catalogue
 * referenced its concepts and published nothing at all about any of them.
 *
 * <p>Its concept URIs canonically end in a slash and it returns
 * {@code <http://...>} as the subject even when the request is made over
 * https, which is why {@link uk.ac.ceh.gateway.catalogue.templateHelpers.UriNormaliser}
 * pins this host to http and leaves its trailing slash alone.
 *
 * <p>Dereferences each concept URI, asking for Turtle. One request per concept,
 * so unlike CAST there is no batch to build.
 */
@Profile("exports")
@Component
class NvsSource extends SkosSource {

    private static final String PREFIX = "http://vocab.nerc.ac.uk/";

    @Override
    public String graph() {
        return PREFIX;
    }

    @Override
    public String title() {
        return "NVS, the NERC Vocabulary Server";
    }

    @Override
    public boolean describes(String iri) {
        return iri.startsWith(PREFIX);
    }

    @Override
    public Duration maxAge() {
        // Shorter than the fortnight an identity gets, and deliberately: a
        // researcher's name is settled, whereas a thesaurus can gain a
        // definition or move a concept in its hierarchy on any release.
        return Duration.ofDays(7);
    }

    @Override
    public int requestsPerRun() {
        // Comfortably above the whole referenced set, which is a hundred
        // concepts across the three fetching vocabularies. The budget is here so
        // that a set which grows unnoticed cannot turn into an unbounded run,
        // not because this authority is near a limit.
        return 500;
    }

    @Override
    public Request request(List<String> batch) {
        return Request.get(batch.getFirst(), "text/turtle");
    }
}
