package uk.ac.ceh.gateway.catalogue.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import uk.ac.ceh.gateway.catalogue.CatalogueWebTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles({"auth:datalabs", "server:datalabs", "search:basic"})
@CatalogueWebTest
@DisplayName("DataLabs production context")
class DataLabsApplicationContextTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("authentication beans needed are present")
    void authenticationContext() {
        assertNotNull(applicationContext.getBean("dataLabsAuthenticationProvider"));
        assertNotNull(applicationContext.getBean("rememberMeServicesDataLabs"));
        assertNotNull(applicationContext.getBean("authenticationGroupStore"));
        assertNotNull(applicationContext.getBean("rememberMeAuthenticationFilter"));
    }

}



