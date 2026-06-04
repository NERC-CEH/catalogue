package uk.ac.ceh.gateway.catalogue.indexing.datacite;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import uk.ac.ceh.gateway.catalogue.datacite.DataciteService;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingService;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;

import java.util.List;

/**
 * The following DataciteIndexingService detects when a GeminiDocument has been
 * updated and submits an update to the datacite rest api if it has been determined
 * that the change in the GeminiDocument adjusts the datacite metadata request.
 */
@Slf4j
@ToString
public class DataciteIndexingService implements DocumentIndexingService {
    private final BundledReaderService<MetadataDocument> bundleReader;
    private final DataciteService datacite;

    public DataciteIndexingService(
            BundledReaderService<MetadataDocument> bundleReader,
            DataciteService datacite
    ) {
        this.bundleReader = bundleReader;
        this.datacite = datacite;
        log.info("Creating");
    }

    /**
     * Loop around all the documents ids in the toIndex. Read these and if a
     * GeminiDocument was detected then submit this to #indexDocument(GeminiDocument)
     * @param toIndex list of ids to index
     * @param revision the revision to index at
     */
    @Async
    @Override
    public void indexDocuments(List<String> toIndex, String revision) {
        for(String metadataId: toIndex) {
            try {
                MetadataDocument document = bundleReader.readBundle(metadataId, revision);
                if (document instanceof GeminiDocument geminiDocument) {
                    indexDocument(geminiDocument);
                }
            }
            catch(Exception ex) {
                log.warn("Failed to read metadata document {}: {}", metadataId, ex.getMessage());
            }
        }
    }

    /**
     * Generate a new datacite metadata request based upon the new GeminiDocument
     * If this request differs to the one which datacite already have then we can
     * submit an update.
     * @param document to check if updates are required
     */
    private void indexDocument(GeminiDocument document) throws JacksonException {
        if(datacite.isDatacited(document)) {
            JsonMapper mapper = JsonMapper.builder()
                  .changeDefaultPropertyInclusion(v -> v.withValueInclusion(JsonInclude.Include.NON_EMPTY))
                  .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                  .build();
            String lastRequest = mapper.writeValueAsString(datacite.getDoiMetadata(document));
            String newRequest = mapper.writeValueAsString(datacite.getNewDataciteRequest(document));
            if(!newRequest.equals(lastRequest)) {
                log.info("Submitting datacite update: {}", document.getId());
                datacite.updateDoiMetadata(document);
            }
        }
    }

    /**
     * Datacite repository is assumed to never be empty
     * @return false
     */
    @Override
    public boolean isIndexEmpty() throws DocumentIndexingException {
        return false;
    }

    // Do nothing here
    @Override
    public void rebuildIndex() {}

    // Do nothing here
    @Override
    public void unindexDocuments(List<String> unIndex) throws DocumentIndexingException {}

    // Do nothing here
    @Override
    public void attemptIndexing() {}
}
