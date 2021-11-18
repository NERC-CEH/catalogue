package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;

@Profile("service-agreement")
@Configuration
@AllArgsConstructor
public class FreemarkerConfig {
    private final freemarker.template.Configuration freemarkerConfiguration;
    private final ServiceAgreementQualityService serviceAgreementQualityService;

    @SneakyThrows
    @PostConstruct
    public void configureFreemarkerSharedVariables() {
        freemarkerConfiguration.setSharedVariable("serviceAgreementQuality", serviceAgreementQualityService);
    }
}
