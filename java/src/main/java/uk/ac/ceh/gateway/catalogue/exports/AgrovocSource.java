package uk.ac.ceh.gateway.catalogue.exports;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * The FAO's multilingual thesaurus.
 *
 * <p>Not in the keyword harvest either, and it mints http: it 301s an http
 * request to https, which the authorities RestTemplate follows.
 *
 * <p>Dereferences each concept URI, asking for Turtle. One request per concept,
 * so unlike CAST there is no batch to build.
 */
@Profile("exports")
@Component
class AgrovocSource extends SkosSource {

    private static final String PREFIX = "http://aims.fao.org/aos/agrovoc/";

    @Override
    public String graph() {
        return PREFIX;
    }

    @Override
    public String title() {
        return "AGROVOC, the FAO multilingual thesaurus";
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
