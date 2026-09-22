package templates;

import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import uk.ac.ceh.gateway.catalogue.gemini.Funding;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.researchActivity.ResearchActivity;

import java.io.File;
import java.io.StringReader;
import java.util.List;

import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.apache.jena.rdf.model.ResourceFactory.createStatement;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Turtle serialisation of a research activity is assembled entirely from free-text
 * editor fields, so every test here asserts that the rendered output actually parses as
 * Turtle. {@link RDFDataMgr#read} throws on malformed input, which is what catches
 * unguarded predicates, empty separators and identifiers that are not legal IRIs.
 */
@Slf4j
@DisplayName("Research activity Turtle templating")
class ResearchActivityTurtleTest {

    private static final String TEMPLATE = "html/researchactivity/researchactivity.ttl";
    private static final String URI = "https://example.com/id/ra-1";
    private static final String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

    Configuration configuration;
    Model model;

    @SneakyThrows
    @BeforeEach
    void init() {
        configuration = new Configuration(Configuration.VERSION_2_3_32);
        configuration.setDirectoryForTemplateLoading(new File("../templates"));
        model = ModelFactory.createDefaultModel();
    }

    @SneakyThrows
    private void render(ResearchActivity document) {
        val string = FreeMarkerTemplateUtils.processTemplateIntoString(
            configuration.getTemplate(TEMPLATE),
            document
        );
        log.debug(string);
        RDFDataMgr.read(model, new StringReader(string), "https://example.com/id/", Lang.TTL);
    }

    private ResearchActivity activity() {
        val activity = new ResearchActivity();
        activity.setId("ra-1");
        activity.setUri(URI);
        activity.setTitle("Test research activity");
        return activity;
    }

    private void assertIsProject() {
        assertTrue(
            model.contains(
                createStatement(
                    createResource(URI),
                    createProperty(RDF_TYPE),
                    createResource("http://xmlns.com/foaf/0.1/Project")
                )
            ),
            "expected the activity to be typed as foaf:Project"
        );
    }

    @Test
    @DisplayName("a minimal activity parses")
    void minimal() {
        render(activity());

        assertIsProject();
    }

    @Test
    @DisplayName("funding with no award number parses")
    void fundingWithoutAwardNumber() {
        // getFunders() keeps an award when *any* of number/title/URI is set, so a
        // title-only award reaches the template with a blank award number.
        val activity = activity();
        activity.setFunding(List.of(
            Funding.builder()
                .funderName("Natural Environment Research Council")
                .funderIdentifier("https://ror.org/02b5d8509")
                .awardTitle("Grant with no reference number")
                .build()
        ));

        render(activity);

        assertIsProject();

        // The award still has to be a *defined* grant, not a bare reference that
        // frapo:awards points at and nothing declares.
        val awarded = model.listObjectsOfProperty(
            createProperty("http://purl.org/cerif/frapo/awards")
        ).toList();
        assertEquals(1, awarded.size(), "expected the funder to award exactly one grant");
        assertTrue(
            model.contains(
                createStatement(
                    awarded.getFirst().asResource(),
                    createProperty(RDF_TYPE),
                    createResource("http://purl.org/cerif/frapo/Grant")
                )
            ),
            "expected the awarded grant to be declared as a frapo:Grant"
        );
        assertTrue(
            model.contains(
                createStatement(
                    awarded.getFirst().asResource(),
                    createProperty("http://purl.org/dc/terms/title"),
                    model.createLiteral("Grant with no reference number")
                )
            ),
            "expected the title-only award to keep its title"
        );
    }

    @Test
    @DisplayName("a mix of funding with and without award numbers parses")
    void mixedFunding() {
        // The separator between award identifiers has to survive entries that are
        // skipped, including the case where the *last* entry is the skipped one.
        val activity = activity();
        activity.setFunding(List.of(
            Funding.builder()
                .funderName("NERC")
                .funderIdentifier("https://ror.org/02b5d8509")
                .awardNumber("NE/J015644/1")
                .build(),
            Funding.builder()
                .funderName("NERC")
                .funderIdentifier("https://ror.org/02b5d8509")
                .awardTitle("Untracked contribution")
                .build()
        ));

        render(activity);

        assertIsProject();
    }

    @Test
    @DisplayName("an award number containing spaces and punctuation parses")
    void awardNumberNeedingEscaping() {
        val activity = activity();
        activity.setFunding(List.of(
            Funding.builder()
                .funderName("Defra")
                .funderIdentifier("https://ror.org/00tnppw48")
                .awardNumber("Grant 123 (phase 2).")
                .build()
        ));

        render(activity);

        assertIsProject();
    }

    @Test
    @DisplayName("a hand-typed contributor organisation identifier parses")
    void contributorIdentifierNotAUri() {
        val activity = activity();
        activity.setContributors(List.of(
            ResponsibleParty.builder()
                .familyName("Smith")
                .givenName("Jo")
                .organisationName("Test Organisation")
                .organisationIdentifier("not a url")
                .build()
        ));

        render(activity);

        assertIsProject();
    }

    @Test
    @DisplayName("a contributor name identifier that is not a URI parses")
    void contributorNameIdentifierNotAUri() {
        val activity = activity();
        activity.setContributors(List.of(
            ResponsibleParty.builder()
                .familyName("Smith")
                .givenName("Jo")
                .nameIdentifier("0000-0001-2345-6789")
                .organisationName("Test Organisation")
                .build()
        ));

        render(activity);

        assertIsProject();
    }

    @Test
    @DisplayName("a contributor role is emitted as a SCoRO term")
    void contributorRole() {
        val activity = activity();
        activity.setContributors(List.of(
            ResponsibleParty.builder()
                .familyName("Smith")
                .givenName("Jo")
                .organisationName("Test Organisation")
                .contributorRole("project-leader")
                .build()
        ));

        render(activity);

        assertTrue(
            model.contains(
                createStatement(
                    model.listSubjectsWithProperty(
                        createProperty("http://www.w3.org/ns/prov#hadRole")
                    ).next(),
                    createProperty("http://www.w3.org/ns/prov#hadRole"),
                    createResource("http://purl.org/spar/scoro/project-leader")
                )
            ),
            "expected the contributor role to be emitted as scoro:project-leader"
        );
    }

    @Test
    @DisplayName("outputs are emitted as frapo:hasOutput")
    void outputs() {
        val activity = activity();
        activity.setRelHasOutput(List.of(
            Link.builder()
                .href("https://example.com/id/dataset-1")
                .title("An output dataset")
                .associationType("dataset")
                .build()
        ));

        render(activity);

        assertTrue(
            model.contains(
                createStatement(
                    createResource(URI),
                    createProperty("http://purl.org/cerif/frapo/hasOutput"),
                    createResource("https://example.com/id/dataset-1")
                )
            ),
            "expected the output to be linked with frapo:hasOutput"
        );
    }

    @Test
    @DisplayName("a title containing quotes and a backslash parses")
    void titleNeedingEscaping() {
        val activity = activity();
        activity.setTitle("The \"big\" C:\\data project");

        render(activity);

        assertIsProject();
    }
}
