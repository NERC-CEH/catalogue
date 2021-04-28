package uk.ac.ceh.gateway.catalogue.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageConvertersConfig {
    @Autowired
    private freemarker.template.Configuration freemarkerConfiguration;

//    @Bean
//    Object2TemplatedMessageConverter<SearchResults> searchResults() {
//        return new Object2TemplatedMessageConverter<>(SearchResults.class, freemarkerConfiguration);
//    }

//    @Bean
//    Object2TemplatedMessageConverter<Activity> efActivity() {
//        return new Object2TemplatedMessageConverter<>(Activity.class, freemarkerConfiguration);
//    }
//
//    @Bean
//    Object2TemplatedMessageConverter<Facility> efFacility() {
//        return new Object2TemplatedMessageConverter<>(Facility.class, freemarkerConfiguration);
//    }
//
//    @Bean
//    Object2TemplatedMessageConverter<Network> efNetwork() {
//       return new Object2TemplatedMessageConverter<>(Network.class,  freemarkerConfiguration);
//    }

//    @Bean("converters")
//    public List<HttpMessageConverter<?>> messageConverters() {
//        List<HttpMessageConverter<?>> converters = new ArrayList<>();
//        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
//        mappingJackson2HttpMessageConverter.setObjectMapper(objectMapper());
//
//        // EF Message Converters
//        converters.add(new Object2TemplatedMessageConverter<>(Activity.class, freemarkerConfiguration));
//        converters.add(new Object2TemplatedMessageConverter<>(Facility.class, freemarkerConfiguration));
//        converters.add(new Object2TemplatedMessageConverter<>(Network.class,  freemarkerConfiguration));
//        converters.add(new Object2TemplatedMessageConverter<>(Programme.class, freemarkerConfiguration));
//        converters.add(new UkeofXml2EFDocumentMessageConverter());
//
//        // IMP Message Converters
//        converters.add(new Object2TemplatedMessageConverter<>(uk.ac.ceh.gateway.catalogue.imp.Model.class, freemarkerConfiguration));
//        converters.add(new Object2TemplatedMessageConverter<>(ModelApplication.class, freemarkerConfiguration));
//        converters.add(new Object2TemplatedMessageConverter<>(CaseStudy.class, freemarkerConfiguration));
//
//        // CEH model catalogue
//        converters.add(new Object2TemplatedMessageConverter<>(CehModel.class, freemarkerConfiguration));
//        converters.add(new Object2TemplatedMessageConverter<>(CehModelApplication.class, freemarkerConfiguration));
//
//        //OSDP
//        converters.add(new Object2TemplatedMessageConverter<>(Agent.class, freemarkerConfiguration));
//        converters.add(new Object2TemplatedMessageConverter<>(uk.ac.ceh.gateway.catalogue.osdp.Dataset.class, freemarkerConfiguration));
//        converters.add(new Object2TemplatedMessageConverter<>(uk.ac.ceh.gateway.catalogue.osdp.Model.class, freemarkerConfiguration));
//        converters.add(new Object2TemplatedMessageConverter<>(MonitoringActivity.class, freemarkerConfiguration));
//        converters.add(new Object2TemplatedMessageConverter<>(MonitoringFacility.class, freemarkerConfiguration));
//        converters.add(new Object2TemplatedMessageConverter<>(MonitoringProgramme.class, freemarkerConfiguration));
//        converters.add(new Object2TemplatedMessageConverter<>(Publication.class, freemarkerConfiguration));
//        converters.add(new Object2TemplatedMessageConverter<>(Sample.class, freemarkerConfiguration));
//
//        //ERAMMP
//        converters.add(new Object2TemplatedMessageConverter<>(ErammpModel.class, freemarkerConfiguration));
//        converters.add(new Object2TemplatedMessageConverter<>(ErammpDatacube.class, freemarkerConfiguration));
//
//        //Sample Archive
//        converters.add(new Object2TemplatedMessageConverter<>(SampleArchive.class, freemarkerConfiguration));
//
//        // Gemini Message Converters
//        converters.add(new Object2TemplatedMessageConverter<>(GeminiDocument.class, freemarkerConfiguration));
//        converters.add(new Object2TemplatedMessageConverter<>(LinkDocument.class, freemarkerConfiguration));
//        converters.add(new Object2TemplatedMessageConverter<>(SearchResults.class, freemarkerConfiguration));
//        converters.add(new Object2TemplatedMessageConverter<>(Citation.class, freemarkerConfiguration));
//        converters.add(new Object2TemplatedMessageConverter<>(StateResource.class, freemarkerConfiguration));
//        converters.add(new Object2TemplatedMessageConverter<>(PermissionResource.class, freemarkerConfiguration));
//        converters.add(new Object2TemplatedMessageConverter<>(CatalogueResource.class, freemarkerConfiguration));
//        converters.add(new Object2TemplatedMessageConverter<>(MaintenanceResponse.class, freemarkerConfiguration));
//        converters.add(new Object2TemplatedMessageConverter<>(SparqlResponse.class, freemarkerConfiguration));
//        converters.add(new Object2TemplatedMessageConverter<>(ValidationResponse.class, freemarkerConfiguration));
//        converters.add(new Object2TemplatedMessageConverter<>(ErrorResponse.class, freemarkerConfiguration));
//        converters.add(new TransparentProxyMessageConverter(httpClient()));
//        converters.add(new ResourceHttpMessageConverter());
//        converters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
//        converters.add(new WmsFeatureInfo2XmlMessageConverter());
//        converters.add(mappingJackson2HttpMessageConverter);
//
//        converters.add(new Object2TemplatedMessageConverter<>(UploadDocument.class, freemarkerConfiguration));
//
//        converters.add(new Object2TemplatedMessageConverter<>(DataType.class, freemarkerConfiguration));
//
//        return converters;
//    }
}
