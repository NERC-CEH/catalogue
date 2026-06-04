package uk.ac.ceh.gateway.catalogue;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@OpenAPIDefinition(info = @Info(
    title = "Environmental Information Data Centre (EIDC) Catalogue API",
    version = "1.0",
    description = "REST API for the UKCEH EIDC metadata catalogue",
    contact = @Contact(name = "UKCEH", email = "enquiries@ceh.ac.uk")
))
@EnableAsync
@EnableCaching
@EnableScheduling
@SpringBootApplication
public class CatalogueApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogueApplication.class, args);
    }

}
