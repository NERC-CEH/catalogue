package uk.ac.ceh.gateway.catalogue.datapreviewer;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DataPreviewProperties.class)
public class DataPreviewConfig {}
