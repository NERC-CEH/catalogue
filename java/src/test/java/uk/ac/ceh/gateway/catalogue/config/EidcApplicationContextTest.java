package uk.ac.ceh.gateway.catalogue.config;

import tools.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.task.TaskSchedulingProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ActiveProfiles;
import uk.ac.ceh.gateway.catalogue.CatalogueWebTest;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.document.writing.DocumentWritingService;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.metrics.JDBCMetricsService;
import uk.ac.ceh.gateway.catalogue.metrics.MetricsService;
import uk.ac.ceh.gateway.catalogue.serviceagreement.*;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.UploadController;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.UploadService;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
// Mirrors the SPRING_PROFILES_ACTIVE the EIDC deployment actually runs, "metrics" included: that
// profile builds JDBCMetricsService, which record page rendering reads through, so leaving it out of
// the production-context test left its wiring unexercised.
@ActiveProfiles({"auth-crowd", "upload-hubbub", "server-eidc", "search-basic", "service-agreement", "metrics"})
@CatalogueWebTest
@DisplayName("EIDC production context")
class EidcApplicationContextTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Some critical beans configured")
    void testContext() {
        assertNotNull(applicationContext.getBean(CatalogueService.class));
        assertNotNull(applicationContext.getBean("codeLookupService"));
        assertNotNull(applicationContext.getBean("dataciteService"));
        assertNotNull(applicationContext.getBean("jenaLookupService"));
        assertNotNull(applicationContext.getBean("permission"));
        val objectMapper = applicationContext.getBean(ObjectMapper.class);
        assertNotNull(objectMapper);
    }

    @Test
    @DisplayName("Hubbub configured correctly, Simple Upload controller not created")
    void hubbubUploadBeansPresent() {
        assertNotNull(applicationContext.getBean(UploadController.class));
        assertNotNull(applicationContext.getBean(UploadService.class));
        Assertions.assertThrows(NoSuchBeanDefinitionException.class, () ->
            applicationContext.getBean(uk.ac.ceh.gateway.catalogue.upload.simple.UploadController.class)
        );
    }

    @Test
    void serviceAgreementBeansPresent() {
        assertNotNull(applicationContext.getBean(ServiceAgreementController.class));
        assertNotNull(applicationContext.getBean(ServiceAgreementService.class));
        assertNotNull(applicationContext.getBean(ServiceAgreementModelAssembler.class));
        assertNotNull(applicationContext.getBean(ServiceAgreementQualityService.class));
        assertNotNull(applicationContext.getBean(ServiceAgreementSearch.class));
    }

    @Test
    @DisplayName("Freemarker shared variables present")
    void freemarkerConfiguredCorrectly() {
        val freemarkerConfiguration = applicationContext.getBean(Configuration.class);
        assertNotNull(freemarkerConfiguration);
        assertNotNull(freemarkerConfiguration.getSharedVariable("catalogues"));
        assertNotNull(freemarkerConfiguration.getSharedVariable("codes"));
        assertNotNull(freemarkerConfiguration.getSharedVariable("downloadOrderDetails"));
        assertNotNull(freemarkerConfiguration.getSharedVariable("geminiHelper"));
        assertNotNull(freemarkerConfiguration.getSharedVariable("jena"));
        assertNotNull(freemarkerConfiguration.getSharedVariable("mapServerDetails"));
        assertNotNull(freemarkerConfiguration.getSharedVariable("metadataQuality"));
        assertNotNull(freemarkerConfiguration.getSharedVariable("permission"));
        assertNotNull(freemarkerConfiguration.getSharedVariable("profile"));
        assertNotNull(freemarkerConfiguration.getSharedVariable("serviceAgreementQuality"));
        assertNotNull(freemarkerConfiguration.getSharedVariable("metrics"));
    }

    /**
     * {@code _metrics.ftlh} reads view and download counts while a record page renders, and those reads
     * are {@code @Cacheable} because uncached they query a SQLite database on a CIFS mount — seconds per
     * call, which pinned almost the whole Tomcat pool in production. The caching only applies if the
     * instance handed to FreeMarker is the proxy rather than the raw bean, and FreeMarker receives it by
     * injection, so proving the injected bean is proxied proves the render path is covered.
     */
    @Test
    @DisplayName("Metrics counts are read through a caching proxy, not the raw bean")
    void metricsCountsAreCached() {
        val metricsService = applicationContext.getBean(MetricsService.class);
        assertNotNull(metricsService);
        Assertions.assertTrue(AopUtils.isAopProxy(metricsService),
            "MetricsService must be proxied or @Cacheable on the count reads is inert");

        val cacheManager = applicationContext.getBean(CacheManager.class);
        assertNotNull(cacheManager.getCache(JDBCMetricsService.VIEW_TOTALS_CACHE));
        assertNotNull(cacheManager.getCache(JDBCMetricsService.DOWNLOAD_TOTALS_CACHE));
    }

    /**
     * {@code spring.task.scheduling.pool.size} only reaches a scheduler that Boot auto-configures:
     * {@code TaskSchedulingConfigurations.TaskSchedulerConfiguration} backs off entirely if the
     * application defines a {@code TaskScheduler} or {@code ScheduledExecutorService} bean of its
     * own, and the property then silently governs nothing. Checking the bean the production context
     * actually holds is what catches that. See dri-one #354.
     * <p>
     * The assertion is against the property as resolved <em>in this context</em> rather than the
     * literal 4, because {@code test.properties} pins the pool back to Boot's default of 1 here: the
     * scheduler is real, and so are the ungated {@code @Scheduled} methods hanging off it, so a pool
     * of 4 in a cached test context means four of the vocabulary downloads running at once inside a
     * 512m Gradle worker. What matters here is the wiring — that the property reaches the bean at
     * all — and that holds whatever the number is. The shipped value of 4 is asserted against the
     * real {@code application.properties} by {@code SchedulingConfigTest}.
     */
    @Test
    @DisplayName("Scheduled tasks share the auto-configured pool the property asks for")
    void taskSchedulerHonoursConfiguredPoolSize() {
        val taskScheduler = applicationContext.getBean(TaskScheduler.class);
        Assertions.assertInstanceOf(
            ThreadPoolTaskScheduler.class,
            taskScheduler,
            "auto-configuration backed off, so spring.task.scheduling.* governs nothing"
        );
        Assertions.assertEquals(
            applicationContext.getBean(TaskSchedulingProperties.class).getPool().getSize(),
            ((ThreadPoolTaskScheduler) taskScheduler).getScheduledThreadPoolExecutor().getCorePoolSize()
        );
    }

    @Test
    @SneakyThrows
    @DisplayName("DocumentWritingService is configured to write JSON")
    void documentWritingService() {
        //given
        val gemini = new GeminiDocument();
        gemini.setTitle("Test");
        gemini.setType("dataset");
        val outputStream = new ByteArrayOutputStream();

        val expected = "{\"type\":\"dataset\",\"title\":\"Test\",\"resourceType\":{\"value\":\"dataset\"},\"notGEMINI\":false,\"availability\":\"Unknown\",\"incomingCitationCount\":0}";
        //when
        val documentWritingService = applicationContext.getBean(DocumentWritingService.class);
        assertNotNull(documentWritingService);

        documentWritingService.write(gemini, MediaType.APPLICATION_JSON, outputStream);

        //then
        val actual = outputStream.toString(StandardCharsets.UTF_8);
        JSONAssert.assertEquals(expected, actual, true);
    }
}
