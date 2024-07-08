package uk.ac.ceh.gateway.catalogue.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import uk.ac.ceh.gateway.catalogue.CatalogueWebTest;
import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
import uk.ac.ceh.gateway.catalogue.document.writing.DocumentWritingService;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.serviceagreement.*;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.UploadController;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.UploadService;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@ActiveProfiles({"auth:crowd", "upload:hubbub", "server:eidc", "search:basic", "service-agreement"})
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
        objectMapper.getRegisteredModuleIds().forEach(module -> log.debug(module.toString()));
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
        val freemarkerConfiguration = (Configuration) applicationContext.getBean(freemarker.template.Configuration.class);
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

        val expected = "{\"type\":\"dataset\",\"title\":\"Test\",\"resourceType\":{\"value\":\"dataset\"},\"notGEMINI\":false,\"resourceStatus\":\"Unknown\",\"incomingCitationCount\":0}";
        //when
        val documentWritingService = applicationContext.getBean(DocumentWritingService.class);
        assertNotNull(documentWritingService);

        documentWritingService.write(gemini, MediaType.APPLICATION_JSON, outputStream);

        //then
        val actual = outputStream.toString(StandardCharsets.UTF_8);
        JSONAssert.assertEquals(expected, actual, true);
    }
}
