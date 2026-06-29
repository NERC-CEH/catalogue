package uk.ac.ceh.gateway.catalogue.document.reading;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.document.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingService;
import uk.ac.ceh.gateway.catalogue.repository.CachedDataRepository;

import java.io.ByteArrayInputStream;

@Slf4j
@ToString
@Service
public class MetadataInfoBundledReaderService implements BundledReaderService<MetadataDocument> {
    private final CachedDataRepository cachedRepo;
    private final DocumentReadingService documentReader;
    private final DocumentInfoMapper<MetadataInfo> documentInfoMapper;
    private final DocumentTypeLookupService representationService;
    private final PostProcessingService<MetadataDocument> postProcessingService;
    private final DocumentIdentifierService documentIdentifierService;

    public MetadataInfoBundledReaderService(
        CachedDataRepository cachedRepo,
        DocumentReadingService documentReader,
        DocumentInfoMapper<MetadataInfo> documentInfoMapper,
        DocumentTypeLookupService representationService,
        PostProcessingService<MetadataDocument> postProcessingService,
        DocumentIdentifierService documentIdentifierService
    ) {
        this.cachedRepo = cachedRepo;
        this.documentReader = documentReader;
        this.documentInfoMapper = documentInfoMapper;
        this.representationService = representationService;
        this.postProcessingService = postProcessingService;
        this.documentIdentifierService = documentIdentifierService;
        log.info("Creating");
    }

    @Override
    @SneakyThrows
    public MetadataDocument readBundle(String file) {
        return readBundle(file, cachedRepo.getLatestRevisionId(), false);
    }

    @Override
    @SneakyThrows
    public MetadataDocument readBundle(String file, String revision) {
        return readBundle(file, revision, true);
    }

    @SneakyThrows
    private MetadataDocument readBundle(String file, String revision, boolean history) {
        val metadataBytes = readBlob(revision, file + ".meta", history);
        val metadataInfo = documentInfoMapper.readInfo(new ByteArrayInputStream(metadataBytes));

        val rawBytes = readBlob(revision, file + ".raw", history);
        val type = representationService.getType(metadataInfo.getDocumentType());
        val document = documentReader.read(
            new ByteArrayInputStream(rawBytes),
            metadataInfo.getRawMediaType(),
            type
        );
        document.setMetadata(metadataInfo.withRawType(null));

        if (history) {
            document.setUri(documentIdentifierService.generateUri(file, revision));
        } else {
            document.setUri(documentIdentifierService.generateUri(file));
        }

        postProcessingService.postProcess(document);
        return document;
    }

    /**
     * Reads blob bytes through the cache: the latest read is keyed by name (and evicted on write),
     * the historical read is keyed by revision+name (immutable, never evicted).
     */
    private byte[] readBlob(String revision, String name, boolean history) {
        return history
            ? cachedRepo.readAtRevision(revision, name)
            : cachedRepo.readLatest(revision, name);
    }
}
