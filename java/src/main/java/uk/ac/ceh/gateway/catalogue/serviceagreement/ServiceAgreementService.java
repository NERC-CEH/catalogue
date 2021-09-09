package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.gateway.catalogue.document.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.document.reading.DocumentReadingService;
import uk.ac.ceh.gateway.catalogue.document.reading.DocumentTypeLookupService;
import uk.ac.ceh.gateway.catalogue.model.*;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingService;

@Slf4j
@ToString
@Service
public class ServiceAgreementService implements ServiceAgreementServiceInterface {
    private final DataRepository<CatalogueUser> repo;
    private final DocumentInfoMapper<MetadataInfo> documentMetadataInfoMapper;
    private final DocumentInfoMapper<ServiceAgreement> documentServiceAgreementMapper;
    private final DocumentTypeLookupService documentTypeLookupService;
    private final DocumentReadingService documentReader;
    private final PostProcessingService<ServiceAgreement> postProcessingService;
    private static final String FOLDER = "service-agreements/";

    public ServiceAgreementService(
            DocumentTypeLookupService documentTypeLookupService,
            DataRepository<CatalogueUser> repo,
            DocumentInfoMapper<MetadataInfo> documentMetadataInfoMapper,
            DocumentInfoMapper<ServiceAgreement> documentServiceAgreementMapper,
            PostProcessingService<ServiceAgreement> postProcessingService,
            DocumentReadingService documentReader) {
        this.documentTypeLookupService = documentTypeLookupService;
        this.repo = repo;
        this.documentMetadataInfoMapper = documentMetadataInfoMapper;
        this.documentServiceAgreementMapper = documentServiceAgreementMapper;
        this.documentReader = documentReader;
        this.postProcessingService = postProcessingService;
        log.info("Creating {}", this);
    }


    @SneakyThrows
    public boolean metadataRecordExists(String id) {
        try {
            repo.getData(FOLDER + id);
        } catch (DataRepositoryException e) {
            return false;
        }
        return true;
    }

    @SneakyThrows
    public ServiceAgreement get(String id) {
        log.debug("testing");
        return this.readBundle(FOLDER + id);
    }


    @SneakyThrows
    public void save(CatalogueUser user, String id, String catalogue, ServiceAgreement serviceAgreementDocument) {

        MetadataInfo metadataInfo = createMetadataInfoWithDefaultPermissions(serviceAgreementDocument, user, MediaType.APPLICATION_JSON, catalogue);

        repo.submitData(String.format("%s%s.meta", FOLDER, id), (o) -> documentMetadataInfoMapper.writeInfo(metadataInfo, o))
                .submitData(String.format("%s%s.raw", FOLDER, id), (o) -> documentServiceAgreementMapper.writeInfo(serviceAgreementDocument, o))
                .commit(user, catalogue);
    }

    @SneakyThrows
    public void delete(CatalogueUser user, String id) {
        repo.deleteData(String.format("%s%s.meta", FOLDER, id))
                .deleteData(String.format("%s%s.raw", FOLDER, id))
                .commit(user, String.format("delete document: %s", id));
    }

    private MetadataInfo createMetadataInfoWithDefaultPermissions(MetadataDocument document, CatalogueUser user, MediaType mediaType, String catalogue) {
        MetadataInfo toReturn = MetadataInfo.builder()
                .rawType(mediaType.toString())
                .documentType(documentTypeLookupService.getName(document.getClass()))
                .catalogue(catalogue)
                .build();
        String username = user.getUsername();
        toReturn.addPermission(Permission.VIEW, username);
        toReturn.addPermission(Permission.EDIT, username);
        toReturn.addPermission(Permission.DELETE, username);
        return toReturn;
    }

    @SneakyThrows
    private ServiceAgreement readBundle(String file) {
        val metadataDoc = repo.getData(file + ".meta");
        val metadataInfo = documentMetadataInfoMapper.readInfo(metadataDoc.getInputStream());

        log.debug("metadataDoc = {}", metadataDoc);
        log.debug("metadataInfo = {}", metadataInfo);

        val dataDoc = repo.getData(file + ".raw");

        log.debug("dataDoc = {}", dataDoc);

        ServiceAgreement document = documentReader.read(
                dataDoc.getInputStream(),
                MediaType.TEXT_XML,
                ServiceAgreement.class
        );
        document.setMetadata(metadataInfo.withRawType(null));
        postProcessingService.postProcess(document);

        return document;
    }

}
