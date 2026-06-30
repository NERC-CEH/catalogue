package templates;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;
import uk.ac.ceh.gateway.catalogue.gemini.DistributionInfo;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertFalse;

@Slf4j
public class XmlTemplateTest {
    Configuration configuration;
    GeminiDocument gemini;
    Map<String, Object> model;

    @SneakyThrows
    private String expected(String filename) {
        val expected = Objects.requireNonNull(getClass().getResourceAsStream(filename));
        return IOUtils.toString(expected, StandardCharsets.UTF_8);
    }

    @SneakyThrows
    private String template(String templateFilename) {
        return FreeMarkerTemplateUtils.processTemplateIntoString(
            configuration.getTemplate(templateFilename),
            model
        );
    }

    private void compare(String expectedXml, String actualXml) {
        Diff diff = DiffBuilder.compare(expectedXml)
            .withTest(actualXml)
            .ignoreWhitespace()
            .ignoreComments()
            .checkForSimilar()
            .build();
        if (diff.hasDifferences()) {
            log.debug(actualXml);
        }
        assertFalse(diff.hasDifferences());
    }

    @SneakyThrows
    @BeforeEach
    void init() {
        configuration = new Configuration(Configuration.VERSION_2_3_23);
        configuration.setDirectoryForTemplateLoading(new File("../templates"));

        gemini = new GeminiDocument();
        gemini.setId("example_identifier");
        gemini.setTitle("Example Dataset Title");
        gemini.setDescription("Example description.");
        Keyword resourceType = Keyword.builder().value("dataset").URI("example_uri").build();
        gemini.setResourceType(resourceType);
        ResponsibleParty contacts = ResponsibleParty.builder()
            .givenName("John")
            .familyName("Doe")
            .organisationName("Example Research Institute")
            .email("contact@example.org")
            .organisationIdentifier("https://ror.org/00000")
            .build();
        gemini.setContactPoints(List.of(contacts));

        model = new HashMap<>();
    }

    private void prepareModelForGemini() {
        model.clear();
        model.put("id", gemini.getId());
        model.put("title", gemini.getTitle());
        model.put("description", gemini.getDescription());
        model.put("metadataDateTime", "2024-08-05");
        model.put("resourceType", gemini.getResourceType());
        model.put("type", gemini.getType());
        model.put("contactPoints", gemini.getContactPoints());
    }

    @SneakyThrows
    @Test
    void testGeminiXml() {
        // Given
        prepareModelForGemini();

        // When
        String expected = expected("xml/gemini.xml");
        String actual = template("xml/gemini.ftlx");

        // Then
        compare(expected, actual);
    }

    @SneakyThrows
    @Test
    void testGeminiWithDistributionInfo() {
        // Given
        prepareModelForGemini();
        DistributionInfo format = DistributionInfo.builder()
            .name("CSV")
            .version("unknown")
            .build();
        gemini.setDistributionFormats(List.of(format));

        ResponsibleParty distributor = ResponsibleParty.builder()
            .givenName("John")
            .familyName("Doe")
            .organisationName("Example Research Institute")
            .email("contact@example.org")
            .build();
        gemini.setDistributorContacts(List.of(distributor));

        model.put("distributionFormats", gemini.getDistributionFormats());
        model.put("distributorContacts", gemini.getDistributorContacts());

        // When
        String actual = template("xml/gemini.ftlx");
        String expected = expected("xml/distributionInfo.xml");

        // Then
        compare(expected, actual);
    }

    @SneakyThrows
    @Test
    void testGeminiWithDataQualityInfo() {
        // Given
        prepareModelForGemini();
        gemini.setLineage("Data lineage is not specified.");
        model.put("lineage", gemini.getLineage());

        // When
        String actual = template("xml/gemini.ftlx");
        String expected = expected("xml/dataQualityInfo.xml");

        // Then
        compare(expected, actual);
    }
}
