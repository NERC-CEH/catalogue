package uk.ac.ceh.gateway.catalogue.config;

import io.swagger.v3.oas.models.servers.Server;
import jakarta.annotation.PostConstruct;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${documents.baseUri}")
    private String baseUri;

    @PostConstruct
    public void configureSpringDoc() {
        SpringDocUtils.getConfig()
            .addAnnotationsToIgnore(ActiveUser.class)
            .addRequestWrapperToIgnore(CatalogueUser.class);
    }

    @Bean
    public OpenApiCustomizer serverCustomizer() {
        return openApi -> openApi.servers(List.of(new Server().url(baseUri)));
    }
}
