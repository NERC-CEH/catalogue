package uk.ac.ceh.gateway.catalogue.datapreviewer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.UploadService;

@Configuration
@EnableConfigurationProperties(DataPreviewProperties.class)
public class DataPreviewConfig {

    /**
     * The listing reads Hubbub through an {@link UploadService} of its own, built on a client with
     * the preview's timeouts. It is held privately by the listing and never registered as a bean,
     * so the upload page's {@code UploadService}, whose client has no timeouts because hashing and
     * moving files can run long, stays the only one that can be injected.
     */
    @Bean
    PreviewListing previewListing(
        DataPreviewProperties properties,
        @Value("${hubbub.url}") String address,
        @Value("${hubbub.token}") String token,
        @Value("${hubbub.location}") String uploadLocation
    ) {
        UploadService hubbub = new UploadService(hubbubRestTemplate(properties), address, token, uploadLocation);
        return new PreviewListing(hubbub, properties);
    }

    /**
     * The same client as the shared {@code normal} template, which follows redirects in the same
     * way, but with connect and read timeouts.
     */
    static RestTemplate hubbubRestTemplate(DataPreviewProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.hubbubConnectTimeout());
        requestFactory.setReadTimeout(properties.hubbubReadTimeout());
        return new RestTemplate(requestFactory);
    }
}
