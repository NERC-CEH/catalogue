package uk.ac.ceh.gateway.catalogue.config;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "download.url")
@Getter
@Setter
public class DownloadUrlProperties {
    @NotNull
    private String regexOrder;
    @NotNull
    private String regexPackage;
    @NotNull
    private String regexDatastore;
    @NotNull
    private String regexCeda;
    @NotNull
    private String regexSupportingDocs;
    @NotNull
    private String regexOrderManDownload;
}
