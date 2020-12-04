package uk.ac.ceh.gateway.catalogue.upload.hubbub;

import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Executors;

@Configuration
@Profile("upload:hubbub")
public class Config {
    @Autowired
    private HubbubService hubbubService;

    @Bean
    public UploadDocumentService uploadDocumentService() {
        Map<String, File> folders = Maps.newHashMap();
        folders.put("documents", new File("/var/ceh-catalogue/dropbox"));
        return new UploadDocumentService(hubbubService, folders,  Executors.newCachedThreadPool());
    }
}
