package uk.ac.ceh.gateway.catalogue.templateHelpers;

import lombok.ToString;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Consolidates the many URI forms a licence or access-rights statement
 * arrives under, and mints a stable node for the ones that arrive as free
 * text only.
 *
 * <p>A production audit (dri-one #327) found ~440 of 2,188 datasets spread
 * across ~100 one-off licence URIs that were, in practice, a handful of real
 * licences spelled differently:
 * <ul>
 *   <li>the same catalogue-local licence under both {@code eidc.ac.uk} and
 *       {@code eidc.ceh.ac.uk} — a host split {@link UriNormaliser} does not
 *       resolve, since it canonicalises the form a single known host
 *       publishes under, not which of two hosts is the "real" one</li>
 *   <li>four spellings of the Open Government Licence, and four of
 *       Creative Commons Attribution, none of them the SPDX identifier</li>
 *   <li>44 datasets whose licence was a blank node — unidentifiable, so it
 *       could not be filtered or compared against any other record's</li>
 * </ul>
 *
 * <p>{@link #canonicalise} handles the first two (URI known, but not in
 * canonical form); {@link #mintLicence}/{@link #mintAccessRights} handle the
 * third (no URI at all, only free text) by minting a node the same way
 * {@link ContactUri} mints one for a person with no identifier — a hash of
 * the normalised text, so the same wording is one node wherever it recurs,
 * and record-order/formatting differences do not fork it.
 */
@Service
@ToString
public class LicenceUri {

    private static final String LICENCE_PREFIX = ":licence_";
    private static final String ACCESS_RIGHTS_PREFIX = ":accessRights_";
    private static final String COPYRIGHT_PREFIX = ":copyright_";

    private static final String OGL_UK_3_0 = "https://spdx.org/licenses/OGL-UK-3.0.ttl";
    private static final String CC_BY_4_0 = "https://spdx.org/licenses/CC-BY-4.0.ttl";
    private static final String CC_BY_NC_4_0 = "https://spdx.org/licenses/CC-BY-NC-4.0.ttl";
    private static final String CC_BY_ND_4_0 = "https://spdx.org/licenses/CC-BY-ND-4.0.ttl";

    /**
     * Catalogue-local, bespoke licences (no SPDX equivalent) seen minted under
     * both hosts below. They converge on {@link #CANONICAL_EIDC_HOST}, the host
     * the rest of the application already treats as canonical when it needs to
     * recognise one of these licences (schema.org export, Croissant export,
     * the DataCite request builder).
     */
    private static final Set<String> EIDC_LICENCE_HOSTS = Set.of("eidc.ac.uk", "eidc.ceh.ac.uk");
    private static final String CANONICAL_EIDC_HOST = "eidc.ac.uk";

    private static final Set<String> NATIONAL_ARCHIVES_HOSTS =
        Set.of("nationalarchives.gov.uk", "www.nationalarchives.gov.uk");

    /** {@code scheme://host} plus everything up to a query string or fragment, already a legal absolute URI. */
    private static final Pattern HOST_PATH = Pattern.compile(
        "^[A-Za-z][A-Za-z0-9+.\\-]*://(?<host>[^/?#]*)(?<path>[^?#]*)"
    );

    /**
     * Maps an already-normalised licence or access-rights URI (see
     * {@link UriNormaliser#normalise}) onto the canonical form it stands for.
     * A URI matching none of the known alternate spellings is returned
     * unchanged: the pre-#327 default of passing an unrecognised URI straight
     * through is preserved.
     *
     * @param normalisedUri output of {@link UriNormaliser#normalise}; must not be null
     * @return the canonical URI to emit, or {@code normalisedUri} unchanged if no mapping applies
     */
    public String canonicalise(String normalisedUri) {
        if (normalisedUri.isEmpty()) {
            return normalisedUri;
        }

        val matcher = HOST_PATH.matcher(normalisedUri);
        if (!matcher.matches()) {
            return normalisedUri;
        }

        val host = matcher.group("host").toLowerCase(Locale.ROOT);
        val path = matcher.group("path");
        val lowerPath = stripTrailingSlash(path.toLowerCase(Locale.ROOT));

        if (lowerPath.equals("/licences/ogl") || lowerPath.startsWith("/licences/ogl/")) {
            return OGL_UK_3_0;
        }
        if (NATIONAL_ARCHIVES_HOSTS.contains(host) && lowerPath.startsWith("/doc/open-government-licence")) {
            return OGL_UK_3_0;
        }
        if (host.equals("creativecommons.org")) {
            if (lowerPath.equals("/licenses/by-nc/4.0") || lowerPath.startsWith("/licenses/by-nc/4.0/")) {
                return CC_BY_NC_4_0;
            }
            if (lowerPath.equals("/licenses/by-nd/4.0") || lowerPath.startsWith("/licenses/by-nd/4.0/")) {
                return CC_BY_ND_4_0;
            }
            if (lowerPath.equals("/licenses/by/4.0") || lowerPath.startsWith("/licenses/by/4.0/")) {
                return CC_BY_4_0;
            }
        }
        if (EIDC_LICENCE_HOSTS.contains(host) && isBespokeEidcLicencePath(lowerPath)) {
            return "https://" + CANONICAL_EIDC_HOST + stripTrailingSlash(path);
        }

        return normalisedUri;
    }

    private static boolean isBespokeEidcLicencePath(String lowerPath) {
        return lowerPath.startsWith("/licences/ecn")
            || lowerPath.startsWith("/licences/chessmet")
            || lowerPath.startsWith("/licences/standard-click-through")
            || lowerPath.startsWith("/licences/ogl-ukbms");
    }

    private static String stripTrailingSlash(String component) {
        return component.length() > 1 && component.endsWith("/")
            ? component.substring(0, component.length() - 1)
            : component;
    }

    /**
     * Mints a stable node for a licence supplied as free text with no URI, so
     * it can be told apart from (and matched against) another record's
     * licence text instead of being an unidentifiable blank node.
     *
     * @param text the licence's free-text description ({@code licence.value}); must not be null
     * @return a Turtle prefixed name, stable for the same text however many times it is called
     */
    public String mintLicence(String text) {
        return MintedNode.from(LICENCE_PREFIX, textKey(text));
    }

    /**
     * The {@code dcterms:accessRights} counterpart of {@link #mintLicence}, kept
     * on a separate prefix so an access-rights statement and a licence that
     * happen to share wording never collide on the same node.
     *
     * @param text the access-rights free-text description ({@code accessLimitation.value}); must not be null
     * @return a Turtle prefixed name, stable for the same text however many times it is called
     */
    public String mintAccessRights(String text) {
        return MintedNode.from(ACCESS_RIGHTS_PREFIX, textKey(text));
    }

    /**
     * The {@code dcterms:rights} counterpart of {@link #mintLicence}, for the
     * copyright notice a record states alongside its licence.
     *
     * <p>These were the last free-text rights statement still emitted as a
     * blank node: a production audit (dri-one #334) found 2,185 of them
     * standing for 270 distinct notices, so the same notice — typically one
     * institution's standard wording, repeated across its whole holding —
     * could not be counted, compared or corrected in one place.
     *
     * <p>Keyed on the notice as the record stores it, before the template's
     * cosmetic substitutions for Turtle output (the © sign, embedded
     * newlines). {@link #textKey} already folds whitespace, and keying on the
     * stored text keeps the node independent of how it is rendered.
     *
     * @param text the copyright notice ({@code copyright.value}); must not be null
     * @return a Turtle prefixed name, stable for the same text however many times it is called
     */
    public String mintCopyright(String text) {
        return MintedNode.from(COPYRIGHT_PREFIX, textKey(text));
    }

    /**
     * Folds case and collapses whitespace runs so that trivial formatting
     * differences (a trailing newline, doubled spaces) do not mint two nodes
     * for what is otherwise the same text.
     */
    private static String textKey(String text) {
        return text.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }
}
