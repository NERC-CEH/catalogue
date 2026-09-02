package uk.ac.ceh.gateway.catalogue.templateHelpers;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Canonicalises externally-supplied URIs before they are emitted as RDF node
 * identifiers.
 *
 * <p>Without this, the same real-world thing arrives in the triplestore under
 * several identifiers purely because of formatting: {@code http} versus
 * {@code https}, percent-encoded versus literal slashes in a query, and a
 * trailing slash or not. A production audit found 301 {@code gtr.ukri.org}
 * project nodes standing for 257 distinct grants.
 *
 * <p>Rules applied to every URI:
 * <ol>
 *   <li>reject anything that cannot be emitted as a Turtle {@code IRIREF}, or
 *       whose scheme is not {@code http}/{@code https} — this catches typos such
 *       as {@code hhttp://}</li>
 *   <li>lower-case the scheme and host</li>
 *   <li>upper-case percent-encoding hex digits ({@code %2f} to {@code %2F})</li>
 *   <li>percent-decode only where the decoded form is unambiguous, so
 *       {@code ?ref=NE%2FS008926%2F1} and {@code ?ref=NE/S008926/1} converge</li>
 * </ol>
 *
 * <p>Scheme preference and trailing-slash policy are deliberately per-host
 * (see {@link #HOST_POLICIES}). Both are only safe where we know which form the
 * host publishes: {@code vocab.nerc.ac.uk} concept URIs, for instance,
 * canonically <em>end</em> in a slash, so a blanket strip would break them.
 * Unlisted hosts keep their scheme and trailing slash untouched.
 *
 * <p>Rejected input is returned as an empty string so that templates can fall
 * back to a blank node or a plain literal via {@code ?has_content}.
 */
@Slf4j
@Service
@ToString
public class UriNormaliser {

    /** Whether the trailing slash of a URI on a given host is significant. */
    private enum TrailingSlash { STRIP, LEAVE }

    private record HostPolicy(boolean preferHttps, TrailingSlash trailingSlash) {}

    private static final HostPolicy DEFAULT_POLICY = new HostPolicy(false, TrailingSlash.LEAVE);

    /**
     * Hosts we have measured emitting more than one form of the same URI, and
     * which we have confirmed serve https. Hosts with a mixture of bare and
     * trailing-slash forms in production are additionally given
     * {@link TrailingSlash#STRIP}; hosts seen only in the bare form get it too,
     * so a future record cannot reintroduce the split.
     */
    private static final Map<String, HostPolicy> HOST_POLICIES = Map.ofEntries(
        Map.entry("gtr.ukri.org", new HostPolicy(true, TrailingSlash.STRIP)),
        Map.entry("sws.geonames.org", new HostPolicy(true, TrailingSlash.STRIP)),
        Map.entry("doi.org", new HostPolicy(true, TrailingSlash.STRIP)),
        Map.entry("ror.org", new HostPolicy(true, TrailingSlash.STRIP)),
        Map.entry("orcid.org", new HostPolicy(true, TrailingSlash.STRIP)),
        // Pre-emptive: no isni.org URI appears in production yet (dri-one #319),
        // but ISNI publishes its identifiers over https with no trailing slash,
        // and the first record to supply one should not split against the next.
        Map.entry("isni.org", new HostPolicy(true, TrailingSlash.STRIP)),
        Map.entry("www.wikidata.org", new HostPolicy(false, TrailingSlash.STRIP)),
        Map.entry("creativecommons.org", new HostPolicy(true, TrailingSlash.STRIP)),
        Map.entry("vocabs.lter-europe.net", new HostPolicy(false, TrailingSlash.STRIP)),
        Map.entry("hdl.handle.net", new HostPolicy(true, TrailingSlash.LEAVE)),
        Map.entry("codes.wmo.int", new HostPolicy(true, TrailingSlash.LEAVE)),
        Map.entry("cdm21006.contentdm.oclc.org", new HostPolicy(true, TrailingSlash.LEAVE)),
        Map.entry("www.nationalarchives.gov.uk", new HostPolicy(true, TrailingSlash.LEAVE)),
        Map.entry("gotw.nerc.ac.uk", new HostPolicy(true, TrailingSlash.LEAVE)),
        Map.entry("eidc.ceh.ac.uk", new HostPolicy(true, TrailingSlash.LEAVE))
    );

    /** RFC 3986 Appendix B, restricted to the hierarchical {@code scheme://authority} form. */
    private static final Pattern URI_PATTERN = Pattern.compile(
        "^(?<scheme>[A-Za-z][A-Za-z0-9+.\\-]*)://(?<authority>[^/?#]*)(?<path>[^?#]*)"
            + "(?:\\?(?<query>[^#]*))?(?:#(?<fragment>.*))?$"
    );

    /**
     * Characters that Turtle forbids inside {@code <...>}, plus anything below
     * {@code U+0021}. A URI containing one of these cannot be emitted at all.
     */
    private static final Pattern ILLEGAL_IN_IRIREF = Pattern.compile("[\\x00-\\x20<>\"{}|^`\\\\]");

    private static final Pattern PERCENT_ESCAPE = Pattern.compile("%([0-9A-Fa-f]{2})");

    /**
     * Unreserved characters, whose percent-encoding is never significant
     * (RFC 3986 §2.3). {@code .} is held back: decoding {@code %2E%2E} would
     * turn an ordinary segment into a dot segment that resolvers collapse.
     */
    private static final String UNRESERVED_EXTRA = "-_~";

    /**
     * Additionally safe to decode inside a query string: legal there unencoded
     * and not a query delimiter. {@code &}, {@code =} and {@code +} are excluded
     * because decoding them would change how the query parses.
     */
    private static final String QUERY_SAFE_EXTRA = "/:@";

    /**
     * @param raw a URI as supplied by a metadata record, possibly null or blank
     * @return the canonical form, or an empty string if it must not be emitted
     */
    public String normalise(String raw) {
        if (raw == null) {
            return "";
        }
        var trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        if (ILLEGAL_IN_IRIREF.matcher(trimmed).find()) {
            log.warn("Not emitting URI as RDF, illegal characters for a Turtle IRI: {}", trimmed);
            return "";
        }

        var matcher = URI_PATTERN.matcher(trimmed);
        if (!matcher.matches()) {
            log.warn("Not emitting URI as RDF, not an absolute hierarchical URI: {}", trimmed);
            return "";
        }

        var scheme = matcher.group("scheme").toLowerCase();
        if (!scheme.equals("http") && !scheme.equals("https")) {
            log.warn("Not emitting URI as RDF, scheme is neither http nor https: {}", trimmed);
            return "";
        }

        var authority = lowerCaseHost(matcher.group("authority"));
        if (authority.isEmpty()) {
            log.warn("Not emitting URI as RDF, no host: {}", trimmed);
            return "";
        }

        var path = decodeUnambiguous(matcher.group("path"), UNRESERVED_EXTRA);
        var query = matcher.group("query") == null
            ? null
            : decodeUnambiguous(matcher.group("query"), UNRESERVED_EXTRA + QUERY_SAFE_EXTRA);
        var fragment = matcher.group("fragment");

        var policy = HOST_POLICIES.getOrDefault(host(authority), DEFAULT_POLICY);
        if (policy.preferHttps()) {
            scheme = "https";
        }
        if (policy.trailingSlash() == TrailingSlash.STRIP && fragment == null) {
            if (query != null) {
                query = stripTrailingSlash(query);
            } else if (path.length() > 1) {
                path = stripTrailingSlash(path);
            }
        }

        var normalised = new StringBuilder(scheme).append("://").append(authority).append(path);
        if (query != null) {
            normalised.append('?').append(query);
        }
        if (fragment != null) {
            normalised.append('#').append(fragment);
        }
        return normalised.toString();
    }

    private static String stripTrailingSlash(String component) {
        return component.endsWith("/") ? component.substring(0, component.length() - 1) : component;
    }

    /** Lower-cases the host, leaving any userinfo alone (it is case-sensitive). */
    private static String lowerCaseHost(String authority) {
        var at = authority.lastIndexOf('@');
        return at < 0
            ? authority.toLowerCase()
            : authority.substring(0, at + 1) + authority.substring(at + 1).toLowerCase();
    }

    private static String host(String authority) {
        var afterUserInfo = authority.substring(authority.lastIndexOf('@') + 1);
        var colon = afterUserInfo.lastIndexOf(':');
        return colon < 0 ? afterUserInfo : afterUserInfo.substring(0, colon);
    }

    /**
     * Upper-cases every percent-escape and decodes those whose byte is a
     * character that carries no meaning when encoded. Multi-byte (non-ASCII)
     * sequences are left encoded, as are structural delimiters.
     */
    private static String decodeUnambiguous(String component, String safeExtra) {
        var result = new StringBuilder();
        var matcher = PERCENT_ESCAPE.matcher(component);
        var last = 0;
        while (matcher.find()) {
            result.append(component, last, matcher.start());
            var value = (char) Integer.parseInt(matcher.group(1), 16);
            if (isSafeToDecode(value, safeExtra)) {
                result.append(value);
            } else {
                result.append('%').append(matcher.group(1).toUpperCase());
            }
            last = matcher.end();
        }
        return result.append(component, last, component.length()).toString();
    }

    private static boolean isSafeToDecode(char value, String safeExtra) {
        return (value >= 'a' && value <= 'z')
            || (value >= 'A' && value <= 'Z')
            || (value >= '0' && value <= '9')
            || safeExtra.indexOf(value) >= 0;
    }
}
