package uk.ac.ceh.gateway.catalogue.templateHelpers;

import lombok.ToString;
import lombok.val;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.gemini.DistributionInfo;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Decides which RDF node a distribution's file format is.
 *
 * <p>Before this existed, {@code templates/rdf/turtle/_dataset.ftl} emitted the
 * format as a blank node carrying the human-readable name twice, as both
 * {@code rdf:value} and {@code rdfs:label}. A production audit (dri-one #334)
 * found 1,762 such nodes standing for 51 distinct strings — the worst ratio of
 * any blank node in the graph. {@code "Comma-separated values (CSV)"} alone
 * accounted for 1,401 of them: 1,401 separate RDF nodes carrying byte-identical
 * labels, so "which datasets are CSV?" was not a question the graph could
 * answer.
 *
 * <p>Precedence, most trustworthy identifier first:
 * <ol>
 *   <li>the IANA media type registration, where the record supplies a media
 *       type — an externally-governed identifier shared with every other DCAT
 *       publisher</li>
 *   <li>a node minted from the format's own name, stable across records</li>
 * </ol>
 *
 * <h2>The media type was already there</h2>
 *
 * <p>{@link DistributionInfo} has carried {@code name}, {@code type} and {@code
 * version} throughout, where {@code type} is the media type — the editor's
 * predefined formats supply {@code text/csv}, {@code application/netcdf},
 * {@code application/vnd.apache.parquet} and {@code image/tiff}. The RDF export
 * discarded it and emitted only the name. A {@code dcterms:IMT} node — the
 * Internet Media Type class — with no media type on it says very little.
 *
 * <h2>What the name-derived node does and does not claim</h2>
 *
 * <p>The key is the name trimmed, case-folded and with whitespace runs
 * collapsed, matching {@link LicenceUri}'s rule for free-text licences. That
 * merges {@code png}/{@code PNG} and {@code rds}/{@code Rds}, and stops there.
 * It deliberately does not reconcile spelling variants: {@code png},
 * {@code Portable Network Graphics (png)} and {@code .rds} keep separate nodes.
 * The 51 strings in production are roughly 35 formats spelled 51 ways, and
 * collapsing those needs a decision per pair — that is a data-cleanup task,
 * and minting is what makes the variants visible enough to do it.
 *
 * <p>{@code version} is not part of the key, and is not emitted. Whether a
 * version belongs on the format concept (making NetCDF 3 and NetCDF 4 separate
 * formats) or on the distribution that encodes it is a modelling question
 * #334 did not settle, and in the datastore the field is overwhelmingly the
 * placeholder the editor's help text asks for — {@code unknown},
 * {@code various}, {@code not specified}. Left for whoever settles it.
 */
@Service
@ToString
public class FormatUri {

    /** Local-name prefix, so a minted node is recognisable as one in the store. */
    private static final String FORMAT_PREFIX = ":format_";

    /**
     * The IANA registry namespace. Note DCAT-AP specifies the {@code http}
     * form of this stem; {@code https} is used here to match how the rest of
     * the catalogue mints external identifiers (SPDX licences, ROR, ORCID),
     * and because iana.org redirects to it.
     */
    private static final String IANA_MEDIA_TYPES = "https://www.iana.org/assignments/media-types/";

    /**
     * {@code type/subtype} in RFC 6838 token characters, and nothing else — a
     * media type reaches us as depositor-typed free text, so anything carrying
     * a space, a second slash or a character that would need escaping is not
     * something to build a URI out of.
     */
    private static final Pattern MEDIA_TYPE = Pattern.compile(
        "[A-Za-z0-9][A-Za-z0-9!#$&^_.+-]*/[A-Za-z0-9][A-Za-z0-9!#$&^_.+-]*"
    );

    /**
     * @param format the distribution format to identify
     * @return a Turtle node — an {@code <IRI>} or a prefixed name — or blank
     *         where the record says nothing usable about the format, in which
     *         case the templates emit no format at all rather than an empty node
     */
    public String identify(DistributionInfo format) {
        val mediaType = mediaTypeUri(format);
        if (!mediaType.isEmpty()) {
            return "<" + mediaType + ">";
        }

        val key = nameKey(format.getName());
        return key.isEmpty() ? "" : MintedNode.from(FORMAT_PREFIX, key);
    }

    /**
     * @return the IANA registration URI for this format's media type, or blank
     *         where the record supplies none or supplies something that is not
     *         a media type. Templates branch on this to decide whether the node
     *         {@link #identify} returned is externally governed, and so must not
     *         be given a label from record text (dri-one #320).
     */
    public String mediaTypeUri(DistributionInfo format) {
        val type = format.getType().trim().toLowerCase(Locale.ROOT);
        return MEDIA_TYPE.matcher(type).matches() ? IANA_MEDIA_TYPES + type : "";
    }

    /**
     * @return whether this format has anything to identify it. A {@link
     *         DistributionInfo} with neither a name nor a media type would
     *         otherwise reach {@link #identify} and get a node carrying nothing
     *         but {@code rdf:type} — the empty-node problem dri-one #322 found
     *         832 instances of among grants. Templates use this to suppress the
     *         format, and the link to it, entirely.
     */
    public boolean hasContent(DistributionInfo format) {
        return !identify(format).isEmpty();
    }

    /**
     * Folds case and collapses whitespace runs so that trivial formatting
     * differences do not mint two nodes for the same format name. Matches
     * {@link LicenceUri}'s rule for the same reason: a depositor retyping the
     * same thing should not fork the node.
     */
    private static String nameKey(String name) {
        return name.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }
}
