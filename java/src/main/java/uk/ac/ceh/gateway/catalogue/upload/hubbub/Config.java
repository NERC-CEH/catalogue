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

    @Bean
    public UploadDocumentService uploadDocumentService(
        @Value("${hubbub.location}") String hubbubLocation,
        HubbubService hubbubService
    ) {
        Map<String, File> folders = Maps.newHashMap();
        folders.put("documents", new File(hubbubLocation));
        return new UploadDocumentService(hubbubService, folders,  Executors.newCachedThreadPool());
    }

    @Bean
    public JiraService jiraService(
        @Value("${jira.username}") String jiraUsername,
        @Value("${jira.password}") String jiraPassword,
        @Value("${jira.address}") String jiraAddress
    ) {
        Client client = Client.create();
        client.addFilter(new HTTPBasicAuthFilter(jiraUsername, jiraPassword));
        WebResource jira = client.resource(jiraAddress);
        return new JiraService(jira);
    }

    @PostConstruct
    @SneakyThrows
    public void configureFreemarker(
        freemarker.template.Configuration config,
        JiraService jiraService
    ) {
        config.setSharedVariable("jira", jiraService);
    }
}
