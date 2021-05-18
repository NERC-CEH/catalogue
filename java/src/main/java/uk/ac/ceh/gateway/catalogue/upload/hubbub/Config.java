package uk.ac.ceh.gateway.catalogue.upload.hubbub;

import com.google.common.collect.Maps;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import lombok.SneakyThrows;
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
    private final String hubbubLocation;
    private final String jiraUsername;
    private final String jiraPassword;
    private final String jiraAddress;
    private final freemarker.template.Configuration config;
    private final HubbubService hubbubService;

    public Config(
        @Value("${hubbub.location}") String hubbubLocation,
        @Value("${jira.username}") String jiraUsername,
        @Value("${jira.password}") String jiraPassword,
        @Value("${jira.address}") String jiraAddress,
        freemarker.template.Configuration config,
        HubbubService hubbubService
    ) {
        this.hubbubLocation = hubbubLocation;
        this.jiraUsername = jiraUsername;
        this.jiraPassword = jiraPassword;
        this.jiraAddress = jiraAddress;
        this.config = config;
        this.hubbubService = hubbubService;
    }

    @Bean
    public UploadDocumentService uploadDocumentService() {
        Map<String, File> folders = Maps.newHashMap();
        folders.put("documents", new File(hubbubLocation));
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
