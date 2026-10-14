package templates;

import com.fasterxml.jackson.annotation.JsonInclude;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.StringTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;
import uk.ac.ceh.gateway.catalogue.model.DataType;
import uk.ac.ceh.gateway.catalogue.model.ObservedProperty;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Where the pages show an {@link ObservedProperty}'s name: a data type's schema table, and a NERC
 * model's parameter list when a parameter has no title. fe683df84 (2024-10-25) renamed the property
 * {@code name} to {@code value} and neither template followed, so the data type page's Field column
 * has been empty since (NERC-CEH/dri-one#472). Records are read as production reads them, in both the
 * stored form older records have ({@code name}) and the current one ({@code value}).
 */
@DisplayName("Observed property names on the page")
class ObservedPropertyNameTemplateTest {

    // mirrors WebConfig#objectMapper
    private final JsonMapper mapper = JsonMapper.builder()
        .changeDefaultPropertyInclusion(v -> v.withValueInclusion(JsonInclude.Include.NON_EMPTY))
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .findAndAddModules()
        .build();

    private Configuration configuration;

    @BeforeEach
    void init() {
        StringTemplateLoader strings = new StringTemplateLoader();
        strings.putTemplate("parameters", "<#import \"html/nercmodels/macros.ftlh\" as m><@m.parameters params />");
        configuration = new Configuration(Configuration.VERSION_2_3_33);
        configuration.setTemplateLoader(new MultiTemplateLoader(new TemplateLoader[]{
            strings, uncheckedLoader()
        }));
    }

    @SneakyThrows
    private static FileTemplateLoader uncheckedLoader() {
        return new FileTemplateLoader(new File("../templates"));
    }

    @SneakyThrows
    private String render(String template, Object model) {
        return FreeMarkerTemplateUtils.processTemplateIntoString(configuration.getTemplate(template), model);
    }

    private String schemaTable(String schemaJson) {
        DataType dataType = mapper.readValue("{\"type\": \"dataType\", \"schema\": " + schemaJson + "}", DataType.class);
        return render("html/dataType/_schemaTable.ftlh", Map.of("schema", dataType.getSchema()));
    }

    @Test
    @DisplayName("the schema table names each field, as the editor now saves it")
    void schemaTableShowsValue() {
        String html = schemaTable("[{\"value\": \"SITE\", \"type\": \"string\"}]");

        assertThat(html).containsPattern("<td nowrap=\"nowrap\">\\s*SITE\\s*</td>");
    }

    @Test
    @DisplayName("the schema table names each field of a record saved before the rename")
    void schemaTableShowsOldName() {
        String html = schemaTable("[{\"name\": \"ID\", \"type\": \"integer\"}, {\"name\": \"YEAR\", \"type\": \"year\"}]");

        assertThat(html)
            .containsPattern("<td nowrap=\"nowrap\">\\s*ID\\s*</td>")
            .containsPattern("<td nowrap=\"nowrap\">\\s*YEAR\\s*</td>");
    }

    @Test
    @DisplayName("a NERC model's parameter with no title is listed by its name")
    void nercParameterWithoutTitle() {
        List<ObservedProperty> params = mapper.readValue(
            "[{\"value\": \"soil_ph\"}, {\"name\": \"rainfall\"}, {\"value\": \"t\", \"title\": \"Temperature\"}]",
            new TypeReference<>() {});

        String html = render("parameters", Map.of("params", params));

        assertThat(html).contains("soil_ph", "rainfall", "Temperature").doesNotContain(">t<");
    }
}
