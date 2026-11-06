package templates;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringFacility;
import uk.ac.ceh.gateway.catalogue.monitoring.MonitoringProgramme;
import uk.ac.ceh.gateway.catalogue.templateHelpers.ContactUri;
import uk.ac.ceh.gateway.catalogue.templateHelpers.FormatUri;
import uk.ac.ceh.gateway.catalogue.templateHelpers.FundingUri;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;
import uk.ac.ceh.gateway.catalogue.templateHelpers.KeywordUri;
import uk.ac.ceh.gateway.catalogue.templateHelpers.LicenceUri;
import uk.ac.ceh.gateway.catalogue.templateHelpers.UriNormaliser;
import uk.ac.ceh.gateway.catalogue.vocabularies.KeywordVocabularySolrQueryService;

import java.io.File;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;

/**
 * Renders every real production record through the Turtle templates and checks the output parses.
 * Runs only when PROD_RECORDS_DIR points at a directory of record JSON files (fetched from the
 * public catalogue, never committed). JenaLookupService is an unstubbed mock, so relationship
 * triples sourced from Jena are not exercised here.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@EnabledIfEnvironmentVariable(named = "PROD_RECORDS_DIR", matches = ".+")
class ProductionRecordsTurtleTest {

    private static final Set<String> GEMINI_TYPES =
        Set.of("dataset", "nonGeographicDataset", "service", "aggregate", "signpost");

    @Mock JenaLookupService jenaLookupService;
    @Mock KeywordVocabularySolrQueryService keywordVocabulary;

    @Test
    void everyProductionRecordRendersToParseableTurtle() throws Exception {
        val configuration = new Configuration(Configuration.VERSION_2_3_32);
        configuration.setDirectoryForTemplateLoading(new File("../templates"));
        configuration.setSharedVariable("jena", jenaLookupService);
        val uriNormaliser = new UriNormaliser();
        configuration.setSharedVariable("uriNormaliser", uriNormaliser);
        configuration.setSharedVariable("contactUri", new ContactUri(uriNormaliser));
        configuration.setSharedVariable("fundingUri", new FundingUri(uriNormaliser));
        configuration.setSharedVariable("licenceUris", new LicenceUri());
        configuration.setSharedVariable("formatUris", new FormatUri());
        configuration.setSharedVariable("keywordUri", new KeywordUri(uriNormaliser, keywordVocabulary));

        // Public JSON carries computed read-only fields (e.g. relCombinedGeometry) with no setter
        val mapper = new ObjectMapper().findAndRegisterModules().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        val model = ModelFactory.createDefaultModel();
        val failures = new ArrayList<String>();
        int records = 0;
        int skipped = 0;

        List<Path> files;
        try (Stream<Path> stream = Files.list(Path.of(System.getenv("PROD_RECORDS_DIR")))) {
            files = stream.filter(p -> p.getFileName().toString().endsWith(".json")).sorted().toList();
        }

        for (val file : files) {
            val id = file.getFileName().toString().replaceFirst("\\.json$", "");
            try {
                val json = mapper.readTree(file.toFile());
                val type = text(json, "type");
                final String template;
                final Object document;
                if (GEMINI_TYPES.contains(type)) {
                    template = "rdf/ttl.ftl";
                    document = mapper.treeToValue(json, GeminiDocument.class);
                } else if ("monitoringFacility".equals(type)) {
                    template = "rdf/monitoring/facility.ftl";
                    document = mapper.treeToValue(json, MonitoringFacility.class);
                } else if ("monitoringProgramme".equals(type)) {
                    template = "rdf/monitoring/programme.ftl";
                    document = mapper.treeToValue(json, MonitoringProgramme.class);
                } else {
                    skipped++;
                    continue;
                }
                val turtle = FreeMarkerTemplateUtils.processTemplateIntoString(
                    configuration.getTemplate(template), document);
                val recordModel = ModelFactory.createDefaultModel();
                RDFDataMgr.read(recordModel, new StringReader(turtle), "https://example.com/id/", Lang.TTL);
                val illTyped = illTypedLiterals(id, recordModel);
                if (illTyped.isEmpty()) {
                    model.add(recordModel);
                    records++;
                } else {
                    failures.addAll(illTyped);
                }
            } catch (Exception e) {
                failures.add(id + ": " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }

        report(model, records, skipped);
        assertThat("no records were read from PROD_RECORDS_DIR", records, greaterThan(0));
        assertThat(failures, empty());
    }

    /** Jena parses ill-typed literals without complaint; a datatype that rejects its lexical form is a failure. */
    private static List<String> illTypedLiterals(String id, Model recordModel) {
        val problems = new ArrayList<String>();
        recordModel.listStatements().forEachRemaining(s -> {
            if (s.getObject().isLiteral()) {
                val lit = s.getObject().asLiteral();
                if (lit.getDatatype() != null && !lit.getDatatype().isValid(lit.getLexicalForm())) {
                    problems.add(id + ": ill-typed literal \"" + lit.getLexicalForm() + "\"^^" + lit.getDatatypeURI());
                }
            }
        });
        return problems;
    }

    private static String text(JsonNode json, String field) {
        val node = json.get(field);
        return node == null || node.isNull() ? null : node.asText();
    }

    private static void report(Model model, int records, int skipped) {
        val perPredicate = new TreeMap<String, Long>();
        model.listStatements().forEachRemaining(s -> perPredicate.merge(s.getPredicate().getURI(), 1L, Long::sum));
        System.out.println("records: " + records);
        System.out.println("skipped: " + skipped);
        System.out.println("triples: " + model.size());
        perPredicate.forEach((predicate, count) -> System.out.println("predicate " + predicate + " " + count));
    }
}
