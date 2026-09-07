package uk.ac.ceh.gateway.catalogue.quality;

import com.jayway.jsonpath.DocumentContext;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.val;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.templateHelpers.UriNormaliser;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.String.format;
import static uk.ac.ceh.gateway.catalogue.quality.Results.Severity.ERROR;
import static uk.ac.ceh.gateway.catalogue.quality.Results.Severity.INFO;

/**
 * Reports externally-supplied URIs in a record that are not in the form the RDF
 * templates will emit.
 *
 * <p>The templates under {@code templates/rdf/} canonicalise on the way out (see
 * {@link UriNormaliser}), so the triplestore is correct either way — but the
 * record still holds the non-canonical value, and only an editor can put that
 * right. Reporting it here is what makes that possible.
 *
 * <p>Unusable URIs are an {@code ERROR}: they are dropped from the RDF, and
 * before canonicalisation existed they became dead-end nodes or broke the
 * Turtle outright. Merely non-canonical URIs are {@code INFO} — a prompt to
 * tidy the record, not a reason to hold up publication.
 */
@Service
@ToString
@RequiredArgsConstructor
public class UriChecks {
    private final UriNormaliser uriNormaliser;

    /**
     * @param parsed the record being checked
     * @param fields JSONPath to the description used in the message. Paths may
     *               be definite ({@code $.accessLimitation.uri}) or wildcarded
     *               ({@code $.funding[*].awardURI}).
     * @return one check per distinct offending URI, in field order
     */
    public List<MetadataCheck> check(@NonNull DocumentContext parsed, @NonNull Map<String, String> fields) {
        val problems = new LinkedHashSet<MetadataCheck>();
        fields.forEach((path, description) ->
            readStrings(parsed, path)
                .filter(raw -> !raw.isBlank())
                .forEach(raw -> checkUri(description, raw).ifPresent(problems::add))
        );
        return new ArrayList<>(problems);
    }

    private Optional<MetadataCheck> checkUri(String description, String raw) {
        val given = raw.trim();
        val canonical = uriNormaliser.normalise(given);
        if (canonical.isEmpty()) {
            return Optional.of(new MetadataCheck(
                // Deliberately does not claim the value "cannot be published as linked data":
                // not every field checked here reaches the RDF templates (see the field maps in
                // GeminiMetadataQualityService and MonitoringQualityService), so that wording was
                // false for most of them. A URI this malformed is worth reporting either way.
                format("%s is not a usable URI: %s", description, given),
                ERROR
            ));
        }
        if (!canonical.equals(given)) {
            return Optional.of(new MetadataCheck(
                format("%s is not in its canonical form, %s should be %s", description, given, canonical),
                INFO
            ));
        }
        return Optional.empty();
    }

    /**
     * Reads a path that may resolve to a single value or to a list of them, so
     * that definite and wildcarded paths can sit side by side in one field map.
     */
    private Stream<String> readStrings(DocumentContext parsed, String path) {
        val value = parsed.read(path, Object.class);
        if (value instanceof List<?> list) {
            return list.stream().filter(String.class::isInstance).map(String.class::cast);
        }
        return value instanceof String single ? Stream.of(single) : Stream.empty();
    }
}
