package uk.ac.ceh.gateway.catalogue.indexing.solr;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.solr.client.solrj.beans.Field;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The managed-schema declares no catch-all dynamicField, so Solr rejects an
 * entire document if it carries a field the schema does not know about. A
 * SolrIndex property annotated with @Field but never added to the schema
 * therefore breaks indexing for every record, not just the ones using it.
 */
class SolrIndexSchemaConsistencyTest {

    private static final Path MANAGED_SCHEMA =
        Path.of("..", "solr", "documents", "conf", "managed-schema");

    private static final Pattern SCHEMA_FIELD = Pattern.compile("<field name=\"([^\"]+)\"");

    @Test
    @SneakyThrows
    void everyIndexedFieldIsDeclaredInTheManagedSchema() {
        //given
        assertTrue(
            Files.exists(MANAGED_SCHEMA),
            "managed-schema not found at " + MANAGED_SCHEMA.toAbsolutePath()
        );
        val declared = schemaFieldNames();

        //when
        val missing = indexedFieldNames()
            .stream()
            .filter(name -> !declared.contains(name))
            .sorted()
            .toList();

        //then
        assertThat(
            "SolrIndex fields with no matching <field> in managed-schema",
            missing,
            empty()
        );
    }

    @SneakyThrows
    private Set<String> schemaFieldNames() {
        val names = new HashSet<String>();
        Matcher matcher = SCHEMA_FIELD.matcher(Files.readString(MANAGED_SCHEMA));
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        return names;
    }

    private Set<String> indexedFieldNames() {
        return Arrays.stream(SolrIndex.class.getDeclaredFields())
            .filter(field -> field.isAnnotationPresent(Field.class))
            .map(field -> {
                val annotated = field.getAnnotation(Field.class).value();
                return unnamed().equals(annotated) ? field.getName() : annotated;
            })
            .collect(Collectors.toSet());
    }

    // @Field's default value is a sentinel meaning "use the property name". It
    // lives in a package-private SolrJ constant, so read it off the annotation.
    @SneakyThrows
    private String unnamed() {
        return (String) Field.class.getMethod("value").getDefaultValue();
    }
}
