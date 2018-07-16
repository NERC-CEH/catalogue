package uk.ac.ceh.gateway.catalogue.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.google.common.eventbus.EventBus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.git.GitDataRepository;
import uk.ac.ceh.components.userstore.AnnotatedUserHelper;
import uk.ac.ceh.components.userstore.UsernameAlreadyTakenException;
import uk.ac.ceh.components.userstore.inmemory.InMemoryUserStore;
import uk.ac.ceh.gateway.catalogue.file.FileRepository;
import uk.ac.ceh.gateway.catalogue.file.FilesystemRepository;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.services.CodeLookupService;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

@Configuration
@PropertySource("classpath:application.properties")
public class ApplicationConfig {
    @Value("${data.repository.location}") private String dataRespository;
    
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
    
    @Bean
    public EventBus communicationBus() {
        return new EventBus();
    }
    
    @Bean
    public DataRepository<CatalogueUser> catalogDataRepository() throws IOException, UsernameAlreadyTakenException {
        return new GitDataRepository<>(new File(dataRespository),
                                        new InMemoryUserStore<>(),
                                        phantomUserBuilderFactory(), 
                                        communicationBus());
    }
    
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .registerModule(new GuavaModule())
            .registerModule(new JaxbAnnotationModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    
    @Bean
    public AnnotatedUserHelper<CatalogueUser> phantomUserBuilderFactory() {
        return new AnnotatedUserHelper<>(CatalogueUser.class);
    }

    @Bean
    public CodeLookupService codeNameLookupService() throws IOException {
        Properties properties = PropertiesLoaderUtils.loadAllProperties("codelist.properties");
        return new CodeLookupService(properties);
    }

    @Bean
    public FileRepository fileRepository() {
        return new FilesystemRepository(
            new FileSystemResourceLoader(),
            "file:///var/ceh-catalogue/datastore"
        );
    }
}
