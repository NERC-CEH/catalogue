package uk.ac.ceh.gateway.catalogue.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringActivity;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringFacility;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringNetwork;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringProgramme;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * The {@code rel*} fields on metadata documents are derived server-side by
 * {@code populateFromJenaService} and must never be settable from an incoming document body,
 * otherwise a raw {@code PUT} persists arbitrary relationships into the datastore.
 *
 * <p>These tests walk the fields reflectively rather than naming them, so a {@code rel*} field
 * added in future without the split-annotation pattern fails here immediately.
 */
@DisplayName("Server-derived rel* fields")
class DerivedRelationshipFieldsTest {

    // mirrors WebConfig#objectMapper - the split-annotation pattern depends on mapper configuration,
    // so a plain JsonMapper would not be testing what production does
    private final JsonMapper mapper = JsonMapper.builder()
        .changeDefaultPropertyInclusion(v -> v.withValueInclusion(JsonInclude.Include.NON_EMPTY))
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .findAndAddModules()
        .build();

    private static final String POISON = "POISON";

    @ParameterizedTest(name = "{0} ignores them on deserialization")
    @ValueSource(classes = {
        GeminiDocument.class,
        MonitoringActivity.class,
        MonitoringFacility.class,
        MonitoringNetwork.class,
        MonitoringProgramme.class
    })
    void areNotWritableFromJson(Class<? extends AbstractMetadataDocument> documentType) throws Exception {
        //given
        val fields = derivedFields(documentType);
        assertThat("no rel* fields found, test is not exercising anything", fields.isEmpty(), equalTo(false));
        val json = fields.stream()
            .map(field -> "\"" + field.getName() + "\":" + poisonFor(field))
            .reduce((a, b) -> a + "," + b)
            .map(body -> "{" + body + "}")
            .orElseThrow();

        //when
        val document = mapper.readValue(json, documentType);

        //then
        val written = new ArrayList<String>();
        for (val field : fields) {
            field.setAccessible(true);
            if (field.get(document) != null) {
                written.add(field.getName());
            }
        }
        assertThat(
            "fields of " + documentType.getSimpleName() + " set from the document body",
            written, equalTo(List.of())
        );
    }

    @ParameterizedTest(name = "{0} still serializes them")
    @ValueSource(classes = {
        GeminiDocument.class,
        MonitoringActivity.class,
        MonitoringFacility.class,
        MonitoringNetwork.class,
        MonitoringProgramme.class
    })
    void areStillReadableAsJson(Class<? extends AbstractMetadataDocument> documentType) throws Exception {
        //given
        val document = documentType.getDeclaredConstructor().newInstance();
        val fields = derivedFields(documentType);
        for (val field : fields) {
            field.setAccessible(true);
            field.set(document, populatedValueFor(field));
        }

        //when
        val json = mapper.writeValueAsString(document);

        //then
        val missing = new ArrayList<String>();
        val tree = mapper.readTree(json);
        for (val field : fields) {
            if (tree.get(field.getName()) == null) {
                missing.add(field.getName());
            }
        }
        assertThat(
            "fields of " + documentType.getSimpleName() + " missing from the response",
            missing, equalTo(List.of())
        );
    }

    /**
     * Fields matching {@code rel} followed by an upper case letter, which excludes the
     * user-editable {@code relationships} field.
     */
    private List<Field> derivedFields(Class<?> documentType) {
        val fields = new ArrayList<Field>();
        for (var type = documentType; type != Object.class; type = type.getSuperclass()) {
            for (val field : type.getDeclaredFields()) {
                if (field.getName().matches("rel[A-Z].*") && !Modifier.isStatic(field.getModifiers())) {
                    fields.add(field);
                }
            }
        }
        return fields;
    }

    private String poisonFor(Field field) {
        if (field.getType().equals(List.class)) {
            return "[{\"href\":\"" + POISON + "\",\"title\":\"" + POISON + "\"}]";
        }
        if (field.getType().equals(String.class)) {
            return "\"" + POISON + "\"";
        }
        return fail("unhandled derived field type " + field.getType() + " for " + field.getName());
    }

    private Object populatedValueFor(Field field) {
        if (field.getType().equals(List.class)) {
            return List.of(Link.builder().href("https://example.com/doc/1").title("Document 1").build());
        }
        if (field.getType().equals(String.class)) {
            return "combined-geometry-wkt";
        }
        return fail("unhandled derived field type " + field.getType() + " for " + field.getName());
    }
}
