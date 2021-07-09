package uk.ac.ceh.gateway.catalogue.upload.hubbub;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@Profile("upload:hubbub")
public class Config {

    @Bean
    public ExecutorService executorService() {
        return Executors.newCachedThreadPool();
    }
}
