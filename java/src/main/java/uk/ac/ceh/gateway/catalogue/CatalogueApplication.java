package uk.ac.ceh.gateway.catalogue;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@OpenAPIDefinition(info = @Info(
    title = "Environmental Information Data Centre (EIDC) Catalogue API",
    version = "1.0",
    description = "REST API for the UKCEH EIDC metadata catalogue",
    contact = @Contact(name = "UKCEH", email = "enquiries@ceh.ac.uk")
))
@EnableAsync
@EnableCaching
// @EnableScheduling is deliberately not here: it lives on SchedulingConfig.SchedulingEnabled, behind
// catalogue.scheduling.enabled, so the test suite can stop @Scheduled methods being registered at
// all. On this class it applied to every @SpringBootTest in the repository. See dri-one #356.
//
// DataSourceAutoConfiguration is excluded here, globally, and is not re-imported anywhere.
// spring-boot-starter-jdbc puts it on the auto-configuration candidate list for every profile, but the
// only datasource in this application is the metrics one. Left enabled, the contexts without the
// "metrics" profile (DataLabs, NonEidc, most tests) would have the PostgreSQL driver on the classpath,
// no spring.datasource.url and no embedded database to fall back on, and would fail to start with
// "Failed to determine a suitable driver class". The exclusion cannot be undone for one profile --
// re-importing it with @ImportAutoConfiguration does not bring it back -- so MetricsDatabaseConfig,
// which is @Profile("metrics"), builds the datasource by hand. Do not drop this exclusion without
// reading the javadoc there.
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class CatalogueApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogueApplication.class, args);
    }

}
