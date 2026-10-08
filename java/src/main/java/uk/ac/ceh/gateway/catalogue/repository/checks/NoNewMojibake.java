package uk.ac.ceh.gateway.catalogue.repository.checks;

import tools.jackson.databind.json.JsonMapper;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MojibakeTextException;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Guards against dri-one #328 (double-encoded/"mojibake" literals) by scanning the document as
 * it will actually be written for the CP1252-decoded-as-UTF-8 signature.
 *
 * <p>Rejects only what this save <em>introduces</em>, comparing against what the stored version
 * already contains. The corruption predates the guard - #328's own verification query exists
 * because production records already match it - so failing any save that merely *contains* a
 * match would make every one of those records uneditable, and an editor fixing an unrelated
 * typo would get a 400 they cannot act on. Counting occurrences rather than comparing a set
 * means an existing {@code Â©} is tolerated while a second one pasted elsewhere is still
 * caught.
 *
 * <p>A document with no stored version (a create) is compared against nothing, so any match in
 * it is new and is rejected.
 */
public class NoNewMojibake implements SaveCheck {
    /**
     * Telltale signature of double-encoded ("mojibake") text - UTF-8 bytes decoded as
     * CP1252/Latin-1 and re-encoded as UTF-8. Guards against any ingest path (known or not yet
     * found) baking further corrupted literals into the store.
     *
     * <p>dri-one #328's verification query uses {@code "â€|Â[^ ]"}; the second alternative is
     * narrowed to a non-letter here. Real mojibake is a {@code Â} standing in for a byte that
     * decodes as punctuation or a symbol - {@code Â°}, {@code Â£}, {@code Â©}, {@code Â} plus a
     * non-breaking space - whereas {@code Â} followed by a letter is ordinary text in several
     * languages: Vietnamese {@code Ân}, upper-cased Romanian {@code CÂMPINA}, Welsh {@code TÂN}.
     * Any of those is plausible in a name, title or place keyword, and would otherwise be
     * rejected at save with no override.
     */
    private static final Pattern MOJIBAKE_PATTERN = Pattern.compile("â€|Â[^ A-Za-z]");

    private final JsonMapper objectMapper;

    public NoNewMojibake(JsonMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void check(CatalogueUser user, MetadataDocument incoming, String id, StoredVersion stored) {
        Map<String, Long> found = mojibakeCounts(objectMapper.writeValueAsString(incoming));
        if (found.isEmpty()) {
            return;
        }
        Map<String, Long> existing = stored.get()
            .map(document -> mojibakeCounts(objectMapper.writeValueAsString(document)))
            .orElseGet(Map::of);
        String introduced = found.entrySet().stream()
            .filter(e -> e.getValue() > existing.getOrDefault(e.getKey(), 0L))
            .map(Map.Entry::getKey)
            .sorted()
            .collect(Collectors.joining(", "));
        if (!introduced.isEmpty()) {
            // Deliberately self-contained and free of issue references or internal jargon: whoever
            // sees this is a depositor trying to save a record, not someone who can look up a
            // ticket. Name the characters found and say what to do about them.
            throw new MojibakeTextException(
                "Document " + id + " contains new text with misread characters: " + introduced +
                    ". Sequences like these appear where a quotation mark, apostrophe, dash or " +
                    "symbol was intended, and usually come from text copied out of a PDF or web " +
                    "page. Please retype the affected text, or paste it as plain text."
            );
        }
    }

    private static Map<String, Long> mojibakeCounts(String text) {
        return MOJIBAKE_PATTERN.matcher(text).results()
            .map(MatchResult::group)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }
}
