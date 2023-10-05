//package uk.ac.ceh.gateway.catalogue.services;
//
//import freemarker.template.Configuration;
//import lombok.SneakyThrows;
//import lombok.val;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.context.annotation.Import;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;
//import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
//import uk.ac.ceh.gateway.catalogue.auth.oidc.WithMockCatalogueUser;
//import uk.ac.ceh.gateway.catalogue.catalogue.Catalogue;
//import uk.ac.ceh.gateway.catalogue.catalogue.CatalogueService;
//import uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig;
//import uk.ac.ceh.gateway.catalogue.config.FreemarkerConfig;
//import uk.ac.ceh.gateway.catalogue.config.SecurityConfigCrowd;
////import uk.ac.ceh.gateway.catalogue.controllers.MaintenanceController;
//import uk.ac.ceh.gateway.catalogue.indexing.jena.JenaIndexingService;
//import uk.ac.ceh.gateway.catalogue.indexing.mapserver.MapServerIndexingService;
//import uk.ac.ceh.gateway.catalogue.indexing.solr.SolrIndexingService;
//import uk.ac.ceh.gateway.catalogue.indexing.validation.ValidationIndexingService;
//
//import java.util.Arrays;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.BDDMockito.given;
////import static org.mockito.Mockito.mock;
////import static uk.ac.ceh.gateway.catalogue.config.DevelopmentUserStoreConfig.ADMIN;
////import static uk.ac.ceh.gateway.catalogue.controllers.DocumentController.MAINTENANCE_ROLE;
//
////@WithMockCatalogueUser(
////        username=ADMIN,
////        grantedAuthorities=MAINTENANCE_ROLE
////)
////@ActiveProfiles("test")
////@DisplayName("FusekiExportService")
////@Import({SecurityConfigCrowd.class, DevelopmentUserStoreConfig.class})
////@WebMvcTest(
////        properties="spring.freemarker.template-loader-path=file:../templates"
////)
//public class FusekiExportServiceTest {
////    @MockBean DataRepositoryOptimizingService repoService;
////    @MockBean @Qualifier("solr-index")
////    SolrIndexingService indexService;
////    @MockBean @Qualifier("jena-index")
////    JenaIndexingService linkingService;
////    @MockBean @Qualifier("validation-index")
////    ValidationIndexingService validationService;
////    @MockBean @Qualifier("mapserver-index")
////    MapServerIndexingService mapserverService;
////    @MockBean CatalogueService catalogueService;
////    @MockBean Ca
//
////    @Autowired private MockMvc mvc;
//
//    @Autowired
//    @Qualifier(value = "")
//    private FreeMarkerConfigurationFactoryBean configuration;
//
////    private FusekiExportService exportService;
//    private final String catalogueKey = "eidc";
//
//    @BeforeEach
////    public void createExportService() {
////        exportService = new FusekiExportService(configuration);
////    }
//
//    @SneakyThrows
//    private void givenFreemarkerConfiguration() {
//        configuration.setSharedVariable("catalogues", catalogueService);
//    }
//
//    private void givenDefaultCatalogue() {
//        given(catalogueService.defaultCatalogue())
//                .willReturn(
//                        Catalogue.builder()
//                                .id(catalogueKey)
//                                .title("Env Data Centre")
//                                .url("https://example.com")
//                                .contactUrl("")
//                                .logo("eidc.png")
//                                .build()
//                );
//    }
//
//    @Test
//    @SneakyThrows
//    void getCatalogueTtl() {
////        freemarker.template.Template freemarkerTemplate = mock(freemarker.template.Template.class);
////        given(configuration.getTemplate("bob")).willReturn(freemarkerTemplate);
//        givenDefaultCatalogue();
//        givenFreemarkerConfiguration();
//
//        List<String> records = Arrays.asList("rec1","rec2","rec3","rec4");
//        val freemarkerTemplate = configuration.getTemplate("rdf/catalogue.ttl");
//        String processedTemplate = FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerTemplate, records);
//        assertFalse(processedTemplate.isEmpty());
////
////        val exporter = new FusekiExportService(configuration);
////        String result = exporter.getCatalogueTtl();
////        System.out.println(result);
////        assertFalse(result.isEmpty());
//    }
//}