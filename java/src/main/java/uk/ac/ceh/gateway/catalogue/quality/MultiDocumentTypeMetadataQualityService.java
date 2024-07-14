package uk.ac.ceh.gateway.catalogue.quality;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.document.reading.DocumentReader;

import java.util.Collections;

import static uk.ac.ceh.gateway.catalogue.DocumentTypes.*;

@Slf4j
@ToString
@Primary
@Service
public class MultiDocumentTypeMetadataQualityService implements MetadataQualityService {
    private final DocumentReader documentReader;
    private final Configuration config;
    private final MetadataQualityService geminiMetadataQualityService;
    private final MetadataQualityService monitoringQualityService;

    public MultiDocumentTypeMetadataQualityService(
            @NonNull DocumentReader documentReader,
            @NonNull ObjectMapper objectMapper,
            @NonNull GeminiMetadataQualityService geminiMetadataQualityService,
            @NonNull MonitoringQualityService monitoringQualityService
    ) {
        this.documentReader = documentReader;
        this.config = Configuration.defaultConfiguration()
            .jsonProvider(new JacksonJsonProvider(objectMapper))
            .mappingProvider(new JacksonMappingProvider(objectMapper))
            .addOptions(
                    Option.DEFAULT_PATH_LEAF_TO_NULL,
                    Option.SUPPRESS_EXCEPTIONS
                    );
        this.geminiMetadataQualityService = geminiMetadataQualityService;
        this.monitoringQualityService = monitoringQualityService;
        log.info("Creating");
    }

    @Override
    @SneakyThrows
    public Results check(String id) {
        log.debug("Checking {}", id);

        try {
            val parsedMeta = JsonPath.parse(
                    documentReader.read(id, "meta"),
                    config
                    );

            return switch (parsedMeta.read("$.documentType", String.class)) {
                case GEMINI -> geminiMetadataQualityService.check(id);
                case MONITORING_ACTIVITY, MONITORING_FACILITY, MONITORING_NETWORK, MONITORING_PROGRAMME ->
                    monitoringQualityService.check(id);
                default ->
                    new Results(Collections.emptyList(), id, "Not a qualifying document type");
            };

        } catch (Exception ex) {
            log.error("Error - could not check " + id, ex);
            return new Results(Collections.emptyList(), id, "Error - could not check this document");
        }
    }
}
