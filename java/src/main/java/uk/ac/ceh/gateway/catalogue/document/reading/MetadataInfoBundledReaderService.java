package uk.ac.ceh.gateway.catalogue.document.reading;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.document.DocumentIdentifierService;
import uk.ac.ceh.gateway.catalogue.document.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingService;

@Slf4j
@ToString
@Service
public class MetadataInfoBundledReaderService implements BundledReaderService<MetadataDocument> {
    private final DataRepository<CatalogueUser> repo;
    private final DocumentReadingService documentReader;
    private final DocumentInfoMapper<MetadataInfo> documentInfoMapper;
    private final DocumentTypeLookupService representationService;
    private final PostProcessingService<MetadataDocument> postProcessingService;
    private final DocumentIdentifierService documentIdentifierService;

    public MetadataInfoBundledReaderService(
        DataRepository<CatalogueUser> repo,
        DocumentReadingService documentReader,
        DocumentInfoMapper<MetadataInfo> documentInfoMapper,
        DocumentTypeLookupService representationService,
        PostProcessingService<MetadataDocument> postProcessingService,
        DocumentIdentifierService documentIdentifierService
    ) {
        this.repo = repo;
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
        return readBundle(file, repo.getLatestRevision().getRevisionID(), false);
    }

    @Override
    @SneakyThrows
    public MetadataDocument readBundle(String file, String revision) {
        return readBundle(file, revision, true);
    }

    @SneakyThrows
    private MetadataDocument readBundle(String file, String revision, boolean history) {
        val metadataDoc = repo.getData(revision, file + ".meta");
        val metadataInfo = documentInfoMapper.readInfo(metadataDoc.getInputStream());

        val dataDoc = repo.getData(revision, file + ".raw");
        val type = representationService.getType(metadataInfo.getDocumentType());
        val document = documentReader.read(
            dataDoc.getInputStream(),
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
}
