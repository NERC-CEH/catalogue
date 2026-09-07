package templates;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.StringTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.infrastructure.InfrastructureRecord;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Renders {@code infrastructurerecord.ftlh}, which carries its own copy of the keyword loop rather
 * than including {@code _tags.ftlh}, so the blank-entry guard has to be fixed - and covered - twice.
 * <p>
 * Production record 876eb47c-ecf5-4760-be45-491a359e0628 ("Atmospheric Analysis Facility :
 * Greenhouse Gases") stores {@code "keywords":[{}]}. {@link Keyword} applies {@code nullToEmpty}, so
 * {@code keywords??} and {@code keywords?size} both counted that entry and the page rendered a
 * labelled Keyword row with an empty value column.
 * <p>
 * The page chrome ({@code skeleton.ftlh}, {@code blocks.ftlh}, {@code _admin.ftlh}) is stubbed via a
 * {@link StringTemplateLoader} listed ahead of the real templates, so this exercises the record
 * template's own markup without standing up the navbar, auth and code-lookup scaffolding.
 */
class InfrastructureKeywordsTemplateTest {
    private static final String TEMPLATE = "html/infrastructure/infrastructurerecord.ftlh";
    private static final String RECORD_ID = "876eb47c-ecf5-4760-be45-491a359e0628";

    Configuration configuration;

    @SneakyThrows
    @BeforeEach
    void init() {
        val strings = new StringTemplateLoader();
        strings.putTemplate("html/skeleton.ftlh",
            "<#macro master title catalogue footer=true><#nested></#macro>");
        strings.putTemplate("html/blocks.ftlh",
            "<#macro linebreaksAndLinks text>${text!''}</#macro>");
        strings.putTemplate("html/infrastructure/_admin.ftlh", "");

        configuration = new Configuration(Configuration.VERSION_2_3_33);
        configuration.setTemplateLoader(new MultiTemplateLoader(new TemplateLoader[]{
            strings, new FileTemplateLoader(new File("../templates"))
        }));
        configuration.setSharedVariable("jena", jena());
        configuration.setSharedVariable("catalogues", catalogues());
    }

    private JenaLookupService jena() {
        val jena = mock(JenaLookupService.class);
        when(jena.allRelatedRecords(anyString())).thenReturn(Collections.emptyList());
        return jena;
    }

    private CatalogueService catalogues() {
        val catalogues = mock(CatalogueService.class);
        when(catalogues.retrieve(anyString())).thenReturn(Catalogue.builder()
            .id("eidc")
            .title("Environmental Information Data Centre")
            .url("https://catalogue.ceh.ac.uk")
            .contactUrl("https://catalogue.ceh.ac.uk/contact")
            .logo("UKCEH_EIDC_black.png")
            .build());
        return catalogues;
    }

    @SneakyThrows
    private String render(InfrastructureRecord record) {
        return FreeMarkerTemplateUtils.processTemplateIntoString(
            configuration.getTemplate(TEMPLATE),
            record
        );
    }

    private InfrastructureRecord recordWithKeywords(Keyword... keywords) {
        return (InfrastructureRecord) new InfrastructureRecord()
            .setTitle("Atmospheric Analysis Facility : Greenhouse Gases")
            .setKeywords(List.of(keywords))
            .setId(RECORD_ID)
            .setUri("https://catalogue.ceh.ac.uk/id/" + RECORD_ID)
            .setMetadata(MetadataInfo.builder().catalogue("eidc").state("published").build());
    }

    @Test
    void keywordRowIsOmittedWhenTheOnlyKeywordIsBlank() {
        //given
        val record = recordWithKeywords(Keyword.builder().build());

        //when
        val actual = render(record);

        //then
        assertThat(actual).doesNotContain("Keyword");
    }

    /** Guards against the filter swallowing real keywords along with the blank. */
    @Test
    void populatedKeywordsStillRenderAlongsideABlank() {
        //given
        val record = recordWithKeywords(
            Keyword.builder().value("Greenhouse gases").build(),
            Keyword.builder().build()
        );

        //when
        val actual = render(record);

        //then
        assertThat(actual).contains("Greenhouse gases");
    }

    /** The label is singular or plural by count, so it must count only surviving keywords. */
    @Test
    void labelIsSingularWhenOnlyOneKeywordSurvivesTheFilter() {
        //given
        val record = recordWithKeywords(
            Keyword.builder().value("Greenhouse gases").build(),
            Keyword.builder().build()
        );

        //when
        val actual = render(record);

        //then
        assertThat(actual).contains(">Keyword<").doesNotContain(">Keywords<");
    }

    @Test
    void labelIsPluralWhenTwoKeywordsSurviveTheFilter() {
        //given
        val record = recordWithKeywords(
            Keyword.builder().value("Greenhouse gases").build(),
            Keyword.builder().value("Atmospheric chemistry").build(),
            Keyword.builder().build()
        );

        //when
        val actual = render(record);

        //then
        assertThat(actual).contains(">Keywords<");
    }
}
