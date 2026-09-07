package templates;

import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.gemini.InspireTheme;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Renders {@code _tags.ftlh} - the Additional metadata tags block - against records carrying a
 * blank keyword entry.
 * <p>
 * {@link Keyword} and {@link InspireTheme} both apply {@code nullToEmpty} to every field, so a
 * keyword saved with nothing filled in deserialises to a present-but-blank object rather than being
 * dropped. It therefore survives every {@code ??} / {@code ?has_content} guard and counts towards
 * the list size, and {@code ?sort_by("value")} sorts the empty string first. Before the fix that
 * produced an empty {@code <span>} in position one followed by a live {@code <#sep>}, which reads as
 * a leading comma on the Keywords row of the record page.
 */
class KeywordTagsTemplateTest {
    private static final String TEMPLATE = "html/dataResource/_tags.ftlh";

    Configuration configuration;

    @SneakyThrows
    @BeforeEach
    void init() {
        configuration = new Configuration(Configuration.VERSION_2_3_33);
        configuration.setTemplateLoader(new FileTemplateLoader(new File("../templates")));
    }

    @SneakyThrows
    private String render(GeminiDocument gemini) {
        return FreeMarkerTemplateUtils.processTemplateIntoString(
            configuration.getTemplate(TEMPLATE),
            gemini
        );
    }

    /** A keyword row entry saved with neither a value nor a URI, as stored: {@code {}}. */
    private Keyword blank() {
        return Keyword.builder().build();
    }

    private Keyword linked(String value, String uri) {
        return Keyword.builder().value(value).URI(uri).build();
    }

    private Keyword plain(String value) {
        return Keyword.builder().value(value).build();
    }

    private GeminiDocument recordWithPlaceKeywords(Keyword... keywords) {
        return (GeminiDocument) new GeminiDocument()
            .setKeywordsPlace(List.of(keywords))
            .setType("dataset");
    }

    /**
     * The reported defect: record 4b9135ba-7655-443d-94c8-935eddbf111d had a blank fourth entry in
     * {@code keywordsPlace} and rendered {@code <span></span> , <span>...Catchment scale...}.
     */
    @Test
    void blankKeywordDoesNotRenderAnEmptySpan() {
        //given
        val gemini = recordWithPlaceKeywords(
            plain("Wales"),
            linked("Catchment scale", "https://digital.ceh.ac.uk/vocab/fdri/203"),
            blank()
        );

        //when
        val actual = render(gemini);

        //then
        assertThat(actual).doesNotContain("<span></span>");
    }

    /**
     * The blank sorts first, so its separator lands in front of the first real keyword. Asserted on
     * the tag-stripped text, because the empty span collapses to nothing in the markup - so an
     * assertion on the markup alone would pass even with the stray separator still there.
     */
    @Test
    void keywordsRowDoesNotStartWithASeparator() {
        //given
        val gemini = recordWithPlaceKeywords(
            plain("Wales"),
            linked("Catchment scale", "https://digital.ceh.ac.uk/vocab/fdri/203"),
            blank()
        );

        //when
        val actual = render(gemini);

        //then
        assertThat(keywordsText(actual)).doesNotStartWith(",").startsWith("Catchment scale");
    }

    /** Guards against the filter being so eager that real keywords disappear too. */
    @Test
    void populatedKeywordsStillRender() {
        //given
        val gemini = recordWithPlaceKeywords(
            plain("Wales"),
            linked("Catchment scale", "https://digital.ceh.ac.uk/vocab/fdri/203"),
            blank()
        );

        //when
        val actual = render(gemini);

        //then
        assertThat(actual).contains("Wales").contains("Catchment scale");
    }

    /**
     * The separator sat on its own template line, so HTML whitespace collapsing put a space in
     * front of every comma - "Wales , Weather station".
     */
    @Test
    void commaSeparatorHasNoSpaceBeforeIt() {
        //given
        val gemini = recordWithPlaceKeywords(
            plain("Wales"),
            plain("Weather station"),
            linked("Catchment scale", "https://digital.ceh.ac.uk/vocab/fdri/203")
        );

        //when
        val actual = render(gemini);

        //then
        assertThat(keywordsRow(actual)).doesNotContain(" ,");
    }

    /** Separated keywords still need a space after the comma, or they run together. */
    @Test
    void commaSeparatorIsFollowedByWhitespace() {
        //given
        val gemini = recordWithPlaceKeywords(plain("Wales"), plain("Weather station"));

        //when
        val actual = render(gemini);

        //then
        assertThat(keywordsRow(actual)).contains("<span>Wales</span>, <span>Weather station</span>");
    }

    /** A row built entirely from blanks has nothing to show, so the label should go too. */
    @Test
    void keywordsRowIsOmittedWhenEveryKeywordIsBlank() {
        //given
        val gemini = recordWithPlaceKeywords(blank(), blank());

        //when
        val actual = render(gemini);

        //then
        assertThat(actual).doesNotContain("Keywords");
    }

    /**
     * Two live production records - "Seabird 2000 Census" (1661085a-fc82-4eb5-9f5f-c60a382a05b0) and
     * "ASSIST large scale field experiment earthworm data" (dd24687a-fab1-4634-a34d-6017d5f78ed7) -
     * store {@code "topicCategories":[{}]}, so the whole list is one blank. That rendered a labelled
     * Topic categories row whose value column held nothing but {@code <span></span>}.
     */
    @Test
    void topicCategoriesRowIsOmittedWhenTheOnlyCategoryIsBlank() {
        //given
        val gemini = (GeminiDocument) new GeminiDocument()
            .setTopicCategories(List.of(blank()))
            .setType("dataset");

        //when
        val actual = render(gemini);

        //then
        assertThat(actual).doesNotContain("Topic categories").doesNotContain("<span></span>");
    }

    /**
     * Record 6aada746-7ac4-4ab5-b151-6f7b60d8f0ca stores {@code "inspireThemes":[{}]}. That block
     * guards with {@code ??} rather than {@code ?has_content}, so it rendered a labelled row with an
     * empty value.
     */
    @Test
    void inspireThemeRowIsOmittedWhenTheOnlyThemeIsBlank() {
        //given
        val gemini = (GeminiDocument) new GeminiDocument()
            .setInspireThemes(List.of(InspireTheme.builder().build()))
            .setType("dataset");

        //when
        val actual = render(gemini);

        //then
        assertThat(actual).doesNotContain("INSPIRE theme");
    }

    @Test
    void populatedInspireThemeStillRenders() {
        //given
        val gemini = (GeminiDocument) new GeminiDocument()
            .setInspireThemes(List.of(
                InspireTheme.builder()
                    .theme("Environmental Monitoring Facilities")
                    .uri("http://inspire.ec.europa.eu/theme/ef")
                    .build(),
                InspireTheme.builder().build()
            ))
            .setType("dataset");

        //when
        val actual = render(gemini);

        //then
        assertThat(actual)
            .contains("INSPIRE theme")
            .contains("Environmental Monitoring Facilities")
            .doesNotContain("<span></span>");
    }

    /**
     * The rendered content of the Keywords value column, collapsed the way a browser would collapse
     * it, so assertions can talk about what the reader actually sees.
     */
    private String keywordsText(String rendered) {
        return keywordsRow(rendered).replaceAll("<[^>]*>", "").replaceAll("\\s+", " ").trim();
    }

    private String keywordsRow(String rendered) {
        val label = "<div class=\"col-sm-3 key\">Keywords</div>";
        val start = rendered.indexOf(label);
        assertThat(start).as("Keywords row is present").isNotNegative();
        val valueStart = rendered.indexOf("value\">", start) + "value\">".length();
        val valueEnd = rendered.indexOf("</div>", valueStart);
        return rendered.substring(valueStart, valueEnd).replaceAll("\\s+", " ").trim();
    }
}
