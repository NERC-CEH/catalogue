package uk.ac.ceh.gateway.catalogue.config;

import org.apache.jena.query.Dataset;
import org.apache.jena.tdb.TDBFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author cjohn
 */
@Configuration
public class TDBJenaConfig {
    @Value("${jena.location}") String location;
    
    @Bean(destroyMethod="close")
    public Dataset tdbModel() {
        return TDBFactory.createDataset(location);
    }
}