package uk.ac.ceh.gateway.catalogue.upload.hubbub;

import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Executors;

@Configuration
@Profile("upload:hubbub")
public class Config {
    private final String hubbubLocation;
    private final HubbubService hubbubService;

    public Config(
        @Value("${hubbub.location}") String hubbubLocation,
        HubbubService hubbubService
    ) {
        this.hubbubLocation = hubbubLocation;
        this.hubbubService = hubbubService;
    }

    @Bean
    public UploadDocumentService uploadDocumentService() {
        Map<String, File> folders = Maps.newHashMap();
        folders.put("documents", new File(hubbubLocation));
        return new UploadDocumentService(hubbubService, folders,  Executors.newCachedThreadPool());
    }
}
