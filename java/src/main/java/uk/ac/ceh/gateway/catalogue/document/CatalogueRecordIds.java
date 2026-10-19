package uk.ac.ceh.gateway.catalogue.document;

import java.net.URI;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Recognises references to catalogue records, whether written as a canonical
 * record URI, an http variant, a production-host URI or a bare id.
 */
public final class CatalogueRecordIds {
    private static final String PRODUCTION_HOST = "catalogue.ceh.ac.uk";

    private CatalogueRecordIds() {
    }

    /**
     * @return the bare record id for {@code http(s)://{catalogue.ceh.ac.uk | host of baseUri}/id/{id}}
     * or for a bare id (no scheme); empty for anything else, including null or blank input.
     */
    public static Optional<String> recordId(String identifier, String baseUri) {
        if (identifier == null || identifier.isBlank()) {
            return Optional.empty();
        }
        String trimmed = identifier.trim();
        if (!trimmed.contains(":") && !trimmed.contains("/")) {
            return Optional.of(trimmed);
        }
        Matcher matcher = pattern(baseUri).matcher(trimmed);
        return matcher.matches() ? Optional.of(matcher.group(2)) : Optional.empty();
    }

    private static Pattern pattern(String baseUri) {
        String hosts = Pattern.quote(PRODUCTION_HOST);
        String baseHost = host(baseUri);
        if (baseHost != null) {
            hosts += "|" + Pattern.quote(baseHost);
        }
        return Pattern.compile("^https?://(" + hosts + ")/id/([^/?#]+)$", Pattern.CASE_INSENSITIVE);
    }

    private static String host(String baseUri) {
        try {
            return baseUri == null ? null : URI.create(baseUri).getHost();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
