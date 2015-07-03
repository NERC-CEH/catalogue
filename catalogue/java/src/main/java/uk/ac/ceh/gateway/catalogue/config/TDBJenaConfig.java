package uk.ac.ceh.gateway.catalogue.config;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author cjohn
 */
@Configuration
public class TDBJenaConfig {
    @Bean
    public Model tdbModel() {
        String directory = "tdb";
        Dataset dataset = TDBFactory.createDataset(directory);

        return dataset.getDefaultModel();
    }
}
