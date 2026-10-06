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
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards {@code functions.ftlh}'s {@code expiringBanner} macro, which is how site-wide notices
 * are published: an announcement is wrapped in the macro with an expiry date and removes itself
 * once that date has passed, with no deploy needed to take it down.
 * <p>
 * The boundary is the thing to hold still, because it decides what date an announcement is
 * written with and it is only observably wrong on one day of the notice's life. The contract is
 * that the expiry date is the <em>last day the banner shows</em>. Getting there needs both sides
 * of the comparison parsed at midnight: {@code ?date} on {@code .now} re-tags the date-time as a
 * date without dropping the time of day, so comparing that against an expiry parsed from a plain
 * date would put today ahead of an expiry of today and hide the banner a day early.
 * <p>
 * The expiry also has to be spelled {@code yyyy-MM-dd}; any other spelling throws, and because
 * production sets {@code template_exception_handler=rethrow} that takes the whole page down
 * rather than just dropping the banner.
 * <p>
 * The macro also owns the alert markup and the per-catalogue guard, so that a call site is only
 * ever the message. That is what makes the scoping testable here rather than being a property of
 * whichever announcement happens to be live.
 */
class ExpiringBannerTemplateTest {
    private static final String WRAPPER = "test-expiringBanner.ftlh";
    private static final String WRAPPER_SOURCE = """
        <#import "functions.ftlh" as func>
        <@func.expiringBanner expiry=expiry catalogueId=catalogueId shownOn=shownOn>NOTICE</@func.expiringBanner>
        """;
    private static final String NESTED = "NOTICE";
    private static final String EVERY_CATALOGUE = "";

    /** The format the macro parses its expiry with. */
    private static final DateTimeFormatter EXPIRY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    Configuration configuration;

    @SneakyThrows
    @BeforeEach
    void init() {
        val strings = new StringTemplateLoader();
        strings.putTemplate(WRAPPER, WRAPPER_SOURCE);

        configuration = new Configuration(Configuration.VERSION_2_3_33);
        configuration.setTemplateLoader(new MultiTemplateLoader(new TemplateLoader[]{
            strings, new FileTemplateLoader(new File("../templates"))
        }));
        configuration.setDateFormat("yyyy-MM-dd");
    }

    /** An unscoped banner, so that the date tests are not also exercising the catalogue guard. */
    private String render(LocalDate expiry) {
        return render(expiry, EVERY_CATALOGUE, List.of());
    }

    @SneakyThrows
    private String render(LocalDate expiry, String catalogueId, List<String> shownOn) {
        return FreeMarkerTemplateUtils.processTemplateIntoString(
            configuration.getTemplate(WRAPPER),
            Map.of(
                "expiry", EXPIRY_FORMAT.format(expiry),
                "catalogueId", catalogueId,
                "shownOn", shownOn
            )
        );
    }

    @Test
    void anExpiryInTheFutureShowsTheNotice() {
        //when
        val actual = render(LocalDate.now().plusDays(1));

        //then
        assertThat(actual).contains(NESTED);
    }

    /**
     * The boundary the publication dates are chosen against: the banner is still up on its own
     * expiry date, so the date to write is the last day it should show. This is the assertion
     * that fails if either side of the comparison stops being parsed at midnight.
     */
    @Test
    void anExpiryOfTodayStillShowsTheNotice() {
        //when
        val actual = render(LocalDate.now());

        //then
        assertThat(actual).contains(NESTED);
    }

    @Test
    void anExpiryInThePastHidesTheNotice() {
        //when
        val actual = render(LocalDate.now().minusDays(1));

        //then
        assertThat(actual).doesNotContain(NESTED);
    }

    // ------------------------------------------------------------------ catalogue scoping

    @Test
    void aBannerScopedToACatalogueShowsOnThatCatalogue() {
        //when
        val actual = render(LocalDate.now(), "eidc", List.of("eidc"));

        //then
        assertThat(actual).contains(NESTED);
    }

    /**
     * The scoping the EIDC notices rely on. A banner naming EIDC support and EIDC help pages
     * must not appear on the other catalogues that this one application serves.
     */
    @Test
    void aBannerScopedToACatalogueIsHiddenOnTheOthers() {
        //when
        val actual = render(LocalDate.now(), "assist", List.of("eidc"));

        //then
        assertThat(actual).doesNotContain(NESTED);
    }

    /**
     * An empty scope means every catalogue, which is how an announcement that genuinely affects
     * all of them - the maintenance window notices - is written.
     */
    @Test
    void aBannerWithNoScopeShowsOnEveryCatalogue() {
        //when
        val actual = render(LocalDate.now(), "assist", List.of());

        //then
        assertThat(actual).contains(NESTED);
    }

    // ---------------------------------------------------------------------- alert markup

    /**
     * The macro supplies the alert markup so the call sites do not repeat it. The dismiss button
     * is the part worth pinning: {@code data-bs-dismiss="alert"} is the whole of the close
     * behaviour, and Bootstrap needs {@code alert-dismissible} on the container for it to work.
     */
    @Test
    void theBannerCarriesDismissibleAlertMarkupAroundTheMessage() {
        //when
        val actual = render(LocalDate.now());

        //then
        assertThat(actual).contains("alert-dismissible");
        assertThat(actual).contains("role=\"alert\"");
        assertThat(actual).contains("data-bs-dismiss=\"alert\"");
        assertThat(actual.indexOf(NESTED)).isLessThan(actual.indexOf("btn-close"));
    }

    /** No markup at all when hidden - not an empty alert box taking up space on the page. */
    @Test
    void anExpiredBannerLeavesNoAlertMarkupBehind() {
        //when
        val actual = render(LocalDate.now().minusDays(1));

        //then
        assertThat(actual).doesNotContain("alert");
        assertThat(actual).doesNotContain("btn-close");
    }

    // ------------------------------------------------------------------------ call sites

    /**
     * Every live call site has to spell its expiry in the configured format, because an
     * unparseable one is a rendered 500 on that page rather than a missing notice. Asserting
     * on the source rather than on a render keeps this from expiring with the notices it checks.
     */
    @Test
    @SneakyThrows
    void everyLiveExpiryIsWrittenInTheParseableFormat() {
        //given
        val callSite = Pattern.compile("<@func\\.expiringBanner\\s+(?:expiry=)?\"([^\"]*)\"");

        //when
        try (val templates = Files.walk(Path.of("../templates"))) {
            val found = templates
                .filter(path -> path.toString().endsWith(".ftlh"))
                .flatMap(path -> {
                    Matcher matcher = callSite.matcher(read(path));
                    return matcher.results().map(result -> Map.entry(path, result.group(1)));
                })
                .toList();

            //then
            assertThat(found).isNotEmpty();
            found.forEach(entry -> assertThatParses(entry.getKey(), entry.getValue()));
        }
    }

    private void assertThatParses(Path template, String expiry) {
        try {
            LocalDate.parse(expiry, EXPIRY_FORMAT);
        } catch (DateTimeParseException ex) {
            throw new AssertionError(
                "%s has an expiringBanner expiry of \"%s\", which ?date cannot parse as yyyy-MM-dd"
                    .formatted(template, expiry), ex
            );
        }
    }

    @SneakyThrows
    private String read(Path path) {
        return Files.readString(path);
    }
}
