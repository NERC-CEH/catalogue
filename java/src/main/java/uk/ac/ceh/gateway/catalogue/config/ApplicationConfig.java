package uk.ac.ceh.gateway.catalogue.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import java.io.File;
import java.io.IOException;
import javax.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.git.GitDataRepository;
import uk.ac.ceh.components.userstore.AnnotatedUserHelper;
import uk.ac.ceh.components.userstore.UsernameAlreadyTakenException;
import uk.ac.ceh.components.userstore.inmemory.InMemoryUserStore;
import uk.ac.ceh.gateway.catalogue.model.User;

/**
 *
 * @author cjohn
 */
@Configuration
public class ApplicationConfig {
    
    private String dataRespository = "e:/gitRepo";
    
    @Bean
    public EventBus communicationBus() {
        return new EventBus();
    }
    
    @Bean
    public DataRepository<User> catalogDataRepository() throws IOException, UsernameAlreadyTakenException {
        return new GitDataRepository<>(new File(dataRespository), 
                                        new InMemoryUserStore(), 
                                        phantomUserBuilderFactory(), 
                                        communicationBus());
    }
    
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }
    
    @Bean
    public AnnotatedUserHelper<User> phantomUserBuilderFactory() {
        return new AnnotatedUserHelper(User.class);
    }
}
