package templates;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A tag with two class attributes keeps only the first, so a class added second silently does
 * nothing: "Move to a different catalogue" carried its catalogue-control hook as a second class
 * attribute, so the link never opened the dialog (NERC-CEH/dri-one#461).
 */
class DuplicateAttributeTemplateTest {
    // One start tag (no FreeMarker directive inside it) with class= twice
    private static final Pattern TWO_CLASSES = Pattern.compile("<[a-zA-Z][^<>]*?\\sclass=[^<>]*?\\sclass=[^<>]*>");

    @Test
    @SneakyThrows
    void noTagHasTwoClassAttributes() {
        List<String> found = new ArrayList<>();
        try (Stream<Path> files = Files.walk(Path.of("../templates"))) {
            for (Path file : files.filter(p -> p.toString().matches(".*\\.ftl[hx]?$")).toList()) {
                String source = Files.readString(file);
                var matcher = TWO_CLASSES.matcher(source);
                while (matcher.find()) {
                    long line = source.substring(0, matcher.start()).lines().count();
                    found.add(file + ":" + line + "  " + matcher.group());
                }
            }
        }
        assertThat(found).isEmpty();
    }
}
