package uk.ac.ceh.gateway.catalogue.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import uk.ac.ceh.gateway.catalogue.CatalogueApplication;

import java.io.InputStream;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards the shipped log levels: third-party libraries at WARN, the catalogue's own code at INFO.
 * Root alone at WARN hid every catalogue INFO message in production, including the index rebuild
 * lines needed to diagnose an empty Solr or Jena index (dri-one #446).
 *
 * <p>The package is taken from {@link CatalogueApplication} rather than written out, so a rename
 * fails the assertion instead of leaving a property that matches nothing and says nothing.
 */
@DisplayName("Shipped logging levels")
class LoggingLevelsTest {

    private static final String CATALOGUE_PACKAGE = CatalogueApplication.class.getPackageName();

    private String shipped(String key) throws Exception {
        Properties properties = new Properties();
        try (InputStream in = new ClassPathResource("application.properties").getInputStream()) {
            properties.load(in);
        }
        return properties.getProperty(key);
    }

    @Test
    @DisplayName("third-party libraries stay at WARN")
    void rootIsWarn() throws Exception {
        assertThat(shipped("logging.level.root")).isEqualTo("warn");
    }

    @Test
    @DisplayName("the catalogue's own code logs at INFO")
    void catalogueIsInfo() throws Exception {
        assertThat(shipped("logging.level." + CATALOGUE_PACKAGE)).isEqualTo("info");
    }
}
