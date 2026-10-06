package uk.ac.ceh.gateway.catalogue.templateHelpers;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.val;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.gemini.Funding;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Decides which RDF node a funding entry's grant is, and returns it ready to
 * emit as Turtle.
 *
 * <p>Before this existed, {@code templates/rdf/_macros.ftl} keyed a grant on
 * {@code awardURI} where one was supplied, and otherwise on the record and the
 * funding entry's position within it — {@code :<recordId>_fund1}. A grant is
 * frequently cited by more than one record, and the funder (GtR in particular)
 * publishes more than one URI form for the same award, so a production audit
 * (dri-one #322) found 301 {@code gtr.ukri.org} project nodes standing for 257
 * distinct grants.
 *
 * <p>Precedence, most trustworthy identifier first:
 * <ol>
 *   <li>a node minted from {@code awardNumber} — the funder's own grant
 *       reference, stable regardless of which URI variant a record happens to
 *       quote</li>
 *   <li>{@code awardURI}, canonicalised by {@link UriNormaliser}, where no
 *       award number was supplied</li>
 *   <li>the record-scoped node, for a funding entry with nothing to identify
 *       it</li>
 * </ol>
 *
 * <p>The award number is hashed rather than slugged, for the same reason a
 * contact's name is (see {@link ContactUri}): an award number is not URI-safe
 * (it typically contains a slash, e.g. {@code NE/R016429/1}), and a readable
 * slug would invite consumers to parse the URI as an assertion about the
 * number. {@code frapo:hasGrantNumber} sits on the node itself, so nothing is
 * lost by making the identifier opaque.
 */
@Service
@ToString
@RequiredArgsConstructor
public class FundingUri {

    /** Local-name prefix, so a minted node is recognisable as one in the store. */
    private static final String GRANT_PREFIX = ":grant_";

    /** Must match {@code exports/GtrSource}'s project prefix, or the source won't fetch it. */
    private static final String GTR_PROJECT_PREFIX = "https://gtr.ukri.org/projects?ref=";

    /**
     * A research council's grant reference: a council code, then slash-separated
     * letters and digits, e.g. {@code NE/S00310X/1} or {@code BBS/E/C/000I0320}.
     * The council codes are a whitelist, so a reference like
     * {@code DST/TM/INDO-UK/2K17/55(C)} is not mistaken for one.
     */
    private static final Pattern UKRI_REFERENCE =
        Pattern.compile("(AH|BB|BBS|EP|ES|MR|NE|ST)(/[A-Z0-9]+)+");

    private final UriNormaliser uriNormaliser;

    /**
     * @param fund     the funding entry to identify
     * @param recordId the id of the record being rendered, for the fallback node
     * @param index    the funding entry's position in its list, for the fallback node
     * @return a Turtle node: an {@code <IRI>} or a prefixed name, never blank
     */
    public String identify(Funding fund, String recordId, int index) {
        if (!fund.getAwardNumber().isBlank()) {
            return MintedNode.from(GRANT_PREFIX, awardNumberKey(fund.getAwardNumber()));
        }

        return canonicalAwardUri(fund)
            .map(canonical -> "<" + canonical + ">")
            .orElseGet(() -> ":" + recordId + "_fund" + index);
    }

    /**
     * @return whether this funding entry has anything of its own to assert —
     *         an award number, a resolvable award URI, an award title, or a
     *         funder identifier. When none of these are present, {@link
     *         #identify} can only fall back to the record-scoped stub node,
     *         and that node would carry nothing beyond {@code rdf:type}: a
     *         production audit (dri-one #322) found 832 such empty {@code
     *         prov:Activity} nodes. Templates use this to suppress the node
     *         — and the link to it — entirely, rather than assert a grant
     *         the record says nothing about.
     */
    public boolean hasContent(Funding fund) {
        return !fund.getAwardNumber().isBlank()
            || canonicalAwardUri(fund).isPresent()
            || !fund.getAwardTitle().isBlank()
            || !fund.getFunderIdentifier().isBlank();
    }

    /**
     * The Gateway to Research page for a UKRI grant the record identifies only
     * by its number (dri-one #430).
     *
     * <p>A grant's name comes from {@code awardTitle}, which records often leave
     * out. GtR knows the title, and {@code exports/GtrSource} already publishes
     * it, but the source graphs fetch only the IRIs the catalogue graph
     * references. Without this link, a grant whose record gives just an award
     * number is never looked up. A production audit found 157 such grants that
     * GtR describes exactly.
     *
     * <p>Templates emit this as {@code rdfs:seeAlso}, not {@code owl:sameAs}.
     * The link is inferred from the shape of the reference, not recorded, and
     * GtR does not hold every well-formed reference (doctoral training grants,
     * for one). For a reference GtR doesn't hold, GtrSource's exact-reference
     * match publishes nothing, never another project's title.
     *
     * @return the GtR project IRI, or an empty string when the award number is
     *         not a UKRI reference, or when the record's own award URI is already
     *         that page (so the link would only repeat its {@code owl:sameAs})
     */
    public String gtrProject(Funding fund) {
        val reference = fund.getAwardNumber().trim().toUpperCase(Locale.ROOT);
        if (!UKRI_REFERENCE.matcher(reference).matches()) {
            return "";
        }
        val project = GTR_PROJECT_PREFIX + reference;
        return canonicalAwardUri(fund).filter(project::equals).isPresent() ? "" : project;
    }

    private Optional<String> canonicalAwardUri(Funding fund) {
        if (fund.getAwardURI().isBlank()) {
            return Optional.empty();
        }
        val canonical = uriNormaliser.normalise(fund.getAwardURI());
        return canonical.isEmpty() ? Optional.empty() : Optional.of(canonical);
    }

    /**
     * Trims and case-folds the award number so that incidental formatting
     * differences (surrounding whitespace, a differently-cased letter in the
     * scheme) do not fork the grant. Unlike a person's name, an award number is
     * otherwise treated as a structured code and not reduced further: its
     * separators are part of what a funder-provided identifier means.
     */
    private static String awardNumberKey(String awardNumber) {
        return awardNumber.trim().toLowerCase(Locale.ROOT);
    }
}
