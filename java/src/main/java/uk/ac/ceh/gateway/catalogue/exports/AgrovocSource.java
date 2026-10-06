package uk.ac.ceh.gateway.catalogue.exports;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * The FAO's multilingual thesaurus.
 *
 * <p>Not in the keyword harvest either. Each concept is fetched from AGROVOC's
 * Skosmos REST API, one request per concept, so unlike CAST there is no batch to
 * build.
 *
 * <p>Not by dereferencing the concept URI, which is what this did until
 * dri-one #414 and which never once worked. AGROVOC mints http, and its content
 * negotiation goes http -> https (301) -> http {@code .ttl} (303) -> https
 * {@code .ttl} (301). The authorities RestTemplate follows redirects with
 * {@code Redirect.NORMAL}, which rightly refuses the https -> http step, so
 * every concept came back as that 303's empty body. Nor would following it have
 * helped: the {@code .ttl} it ends on carries its labels double-encoded
 * ({@code карбоксин} arrives as {@code ÐºÐ°Ñ...}), and declares no charset. The
 * API answers over https in one hop, as {@code text/turtle; charset=utf-8}, with
 * the labels intact. It also returns neighbouring concepts, which
 * {@link SkosSource} already narrows away.
 */
@Profile("exports")
@Component
class AgrovocSource extends SkosSource {

    private static final String PREFIX = "http://aims.fao.org/aos/agrovoc/";
    private static final String API = "https://agrovoc.fao.org/browse/rest/v1/agrovoc/data";

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
        // Encoded here, as every source encodes its own query parameters (see Request).
        return Request.get(API
                + "?uri=" + URLEncoder.encode(batch.getFirst(), StandardCharsets.UTF_8)
                + "&format=" + URLEncoder.encode("text/turtle", StandardCharsets.UTF_8),
            "text/turtle");
    }
}
