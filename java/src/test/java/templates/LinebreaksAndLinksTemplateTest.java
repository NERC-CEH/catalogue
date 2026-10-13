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

import java.io.File;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code blocks.linebreaksAndLinks} renders record text (dataset description, lineage, reason
 * for change, monitoring description and objectives…) on public record pages, turning bare URLs
 * into links. It switched auto-escaping off to emit those links, so markup stored in a record
 * ran for every visitor (NERC-CEH/dri-one#454). The text must be escaped before it is linkified.
 */
class LinebreaksAndLinksTemplateTest {
    private static final String WRAPPER = "test-linebreaksAndLinks.ftlh";

    Configuration configuration;

    @SneakyThrows
    @BeforeEach
    void init() {
        val strings = new StringTemplateLoader();
        strings.putTemplate(WRAPPER, """
            <#import "html/blocks.ftlh" as blocks>
            <@blocks.linebreaksAndLinks value />""");
        configuration = new Configuration(Configuration.VERSION_2_3_33);
        configuration.setTemplateLoader(new MultiTemplateLoader(new TemplateLoader[]{
            strings, new FileTemplateLoader(new File("../templates"))
        }));
    }

    @SneakyThrows
    private String render(String value) {
        return FreeMarkerTemplateUtils.processTemplateIntoString(configuration.getTemplate(WRAPPER), Map.of("value", value));
    }

    @Test
    void escapesMarkupInTheText() {
        val html = render("Soil <script>alert(document.cookie)</script> & <img src=x onerror=alert(1)>");

        assertThat(html)
            .doesNotContain("<script>")
            .doesNotContain("<img")
            .contains("&lt;script&gt;alert(document.cookie)&lt;/script&gt; &amp; &lt;img src=x onerror=alert(1)&gt;");
    }

    @Test
    void stillLinksBareUrls() {
        val html = render("See https://example.com/a?b=1&c=2 for details");

        assertThat(html).contains(
            "<a target='_blank' rel='noopener noreferrer' href='https://example.com/a?b=1&amp;c=2'>https://example.com/a?b=1&amp;c=2</a> for details");
    }

    @Test
    void aQuoteInAUrlCannotLeaveTheHref() {
        val html = render("https://example.com/'onmouseover='alert(1)");

        assertThat(html)
            .doesNotContain("'onmouseover='")
            .contains("href='https://example.com/'>https://example.com/</a>&#39;onmouseover=&#39;alert(1)");
    }

    @Test
    void aUrlInAngleBracketsOrQuotesEndsBeforeThem() {
        val html = render("<https://example.com/x> and \"https://example.org/y\"");

        assertThat(html)
            .contains("&lt;<a target='_blank' rel='noopener noreferrer' href='https://example.com/x'>https://example.com/x</a>&gt;")
            .contains("&quot;<a target='_blank' rel='noopener noreferrer' href='https://example.org/y'>https://example.org/y</a>&quot;");
    }

    @Test
    void keepsLineBreaks() {
        val html = render("first line\nsecond line");

        assertThat(html).containsPattern("first line\\s*<br>\\s*second line");
    }
}
