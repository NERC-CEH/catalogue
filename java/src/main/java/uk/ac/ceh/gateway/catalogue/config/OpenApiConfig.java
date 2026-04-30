package uk.ac.ceh.gateway.catalogue.config;

import jakarta.annotation.PostConstruct;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Configuration;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

@Configuration
public class OpenApiConfig {

    @PostConstruct
    public void configureSpringDoc() {
        SpringDocUtils.getConfig()
            .addAnnotationsToIgnore(ActiveUser.class)
            .addRequestWrapperToIgnore(CatalogueUser.class);
    }
}
