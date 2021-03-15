package uk.ac.ceh.gateway.catalogue.upload.hubbub;

import com.google.common.collect.Maps;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import uk.ac.ceh.gateway.catalogue.services.JiraService;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Map;
import java.util.concurrent.Executors;

@Configuration
@Profile("upload:hubbub")
public class Config {
    @Value("${jira.username}") private String jiraUsername;
    @Value("${jira.password}") private String jiraPassword;
    @Value("${jira.address}") private String jiraAddress;

    @Autowired
    private HubbubService hubbubService;
    @Autowired
    private freemarker.template.Configuration config;

    @Bean
    public UploadDocumentService uploadDocumentService() {
        Map<String, File> folders = Maps.newHashMap();
        folders.put("documents", new File("/var/ceh-catalogue/dropbox"));
        return new UploadDocumentService(hubbubService, folders,  Executors.newCachedThreadPool());
    }

    @Bean
    public JiraService jiraService() {
        Client client = Client.create();
        client.addFilter(new HTTPBasicAuthFilter(jiraUsername, jiraPassword));
        WebResource jira = client.resource(jiraAddress);
        return new JiraService(jira);
    }

    @PostConstruct
    @SneakyThrows
    public void configureFreemarker() {
        config.setSharedVariable("jira", jiraService());
    }
}
