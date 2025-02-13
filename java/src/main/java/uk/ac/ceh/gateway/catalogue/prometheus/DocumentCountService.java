package uk.ac.ceh.gateway.catalogue.prometheus;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.document.DocumentListingService;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;

import java.util.List;

@Service
@Slf4j
public class DocumentCountService
{
    private final DocumentListingService listingService;
    private final BundledReaderService<MetadataDocument> documentBundleReader;

    public DocumentCountService(DataRepository<CatalogueUser> repo, DocumentListingService listingService, BundledReaderService<MetadataDocument> documentBundleReader) {
        this.listingService = listingService;
        this.documentBundleReader = documentBundleReader;
        Metrics.gauge("catalogue_documents", List.of(Tag.of("viewable", "public")), repo, this::countDocuments);
        log.info("Constructed {}", this);
    }

    @SneakyThrows
    private long countDocuments(DataRepository<?> repo) {
        String currentRevision = repo.getLatestRevision().getRevisionID();
        return listingService.filterFilenames(repo.getFiles(currentRevision))
            .stream()
            .filter(file -> {
                try {
                    MetadataInfo metadata = documentBundleReader.readBundle(file, currentRevision).getMetadata();
                    return metadata.isPubliclyViewable(Permission.VIEW) && metadata.getState().equals("published");
                }
                catch (Exception ex) {
                    log.error("Failed to read {} @ {}: {}", file, currentRevision, ex);
                    return false;
                }
            })
            .count();
    }
}
