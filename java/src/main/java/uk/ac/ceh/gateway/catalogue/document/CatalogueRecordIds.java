package uk.ac.ceh.gateway.catalogue.document;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Recognises references to catalogue records, whether written as a record URI
 * on this instance's host ({@code documents.baseUri}), on the canonical host
 * that published records are identified by ({@code documents.canonicalBaseUri}),
 * or as a bare id. Both hosts are matched over either scheme and in any case.
 * <p>
 * The canonical host is separate because records copied from production keep
 * their production URIs on staging and local instances, whose baseUri differs.
 */
@Component
public class CatalogueRecordIds {
    private final Set<String> hosts;
    private final Pattern recordUri;

    public CatalogueRecordIds(
        @Value("${documents.baseUri}") String baseUri,
        @Value("${documents.canonicalBaseUri}") String canonicalBaseUri
    ) {
        this.hosts = Stream.of(host(canonicalBaseUri), host(baseUri))
            .filter(host -> host != null && !host.isBlank())
            .map(String::toLowerCase)
            .collect(Collectors.toUnmodifiableSet());
        String alternatives = hosts.isEmpty()
            ? "(?!)"
            : hosts.stream().map(Pattern::quote).collect(Collectors.joining("|"));
        this.recordUri = Pattern.compile("^https?://(" + alternatives + ")/id/([^/?#]+)$", Pattern.CASE_INSENSITIVE);
    }

    /**
     * @return the bare record id for {@code http(s)://{catalogue host}/id/{id}}
     * or for a bare id (no scheme); empty for anything else, including null or blank input.
     */
    public Optional<String> recordId(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return Optional.empty();
        }
        String trimmed = identifier.trim();
        if (!trimmed.contains(":") && !trimmed.contains("/")) {
            return Optional.of(trimmed);
        }
        Matcher matcher = recordUri.matcher(trimmed);
        return matcher.matches() ? Optional.of(matcher.group(2)) : Optional.empty();
    }

    /**
     * @return whether the host (any case, no port) is this instance's or the canonical catalogue host
     */
    public boolean isCatalogueHost(String host) {
        return host != null && hosts.contains(host.toLowerCase());
    }

    private static String host(String uri) {
        try {
            return uri == null ? null : URI.create(uri).getHost();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
