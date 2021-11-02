package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.gateway.catalogue.document.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;

@Profile("service-agreement")
@Slf4j
@ToString
@Service
public class GitRepoServiceAgreementService implements ServiceAgreementService {
    private final DataRepository<CatalogueUser> repo;
    private final DocumentInfoMapper<MetadataInfo> metadataInfoMapper;
    private final DocumentInfoMapper<ServiceAgreement> serviceAgreementMapper;
    private final DocumentRepository documentRepository;
    private static final String FOLDER = "service-agreements/";

    public GitRepoServiceAgreementService(
            DataRepository<CatalogueUser> repo,
            DocumentInfoMapper<MetadataInfo> metadataInfoMapper,
            DocumentInfoMapper<ServiceAgreement> serviceAgreementMapper,
            DocumentRepository documentRepository
    ) {
        this.repo = repo;
        this.metadataInfoMapper = metadataInfoMapper;
        this.serviceAgreementMapper = serviceAgreementMapper;
        this.documentRepository = documentRepository;
        log.info("Creating");
    }

    @Override
    @SneakyThrows
    public void populateGeminiDocument(CatalogueUser user, String id) {
        val gemini = (GeminiDocument) documentRepository.read(id);
        val metadataRecordState = gemini.getMetadata().getState();
        if (metadataRecordState.equals("draft")) {
            val serviceAgreement = get(id);
            log.info("Gemini document populated from Service Agreement: {}", id);
            gemini.populateFromServiceAgreement(serviceAgreement);
            documentRepository.save(
                user,
                gemini,
                "populated from service agreement"
            );
        } else {
            log.info("Cannot populate Gemini document: {}. It is not in Draft state", id);
        }
    }

    @Override
    @SneakyThrows
    public boolean metadataRecordExists(String id) {
        try {
            repo.getData(id + ".meta");
        } catch (DataRepositoryException e) {
            return false;
        }
        return true;
    }

    @Override
    @SneakyThrows
    public ServiceAgreement get(String id) {
        val metadataDoc = repo.getData(FOLDER + id + ".meta");
        val metadataInfo = metadataInfoMapper.readInfo(metadataDoc.getInputStream());
        log.debug("metadataInfo = {}", metadataInfo);

        val dataDoc = repo.getData(FOLDER + id + ".raw");
        val serviceAgreement = serviceAgreementMapper.readInfo(dataDoc.getInputStream());
        serviceAgreement.setMetadata(metadataInfo);
        log.debug("Service Agreement: {}", serviceAgreement);
        return serviceAgreement;
    }

    @SneakyThrows
    @Override
    public ServiceAgreement create(CatalogueUser user, String id, String catalogue, ServiceAgreement serviceAgreement) {
        serviceAgreement.setId(id);
        val metadataInfo = createMetadataInfoWithDefaultPermissions(
            user,
            catalogue
        );
        repo.submitData(FOLDER + id + ".meta", (o) -> metadataInfoMapper.writeInfo(metadataInfo, o))
            .submitData(FOLDER + id + ".raw", (o) -> serviceAgreementMapper.writeInfo(serviceAgreement, o))
            .commit(user, "creating service agreement " + id);
        return get(id);
    }

    @SneakyThrows
    @Override
    public ServiceAgreement update(CatalogueUser user, String id, ServiceAgreement serviceAgreement) {
        serviceAgreement.setId(id);
        repo.submitData(FOLDER + id + ".raw", (o) -> serviceAgreementMapper.writeInfo(serviceAgreement, o))
            .commit(user, "updating service agreement " + id);
        return get(id);
    }

    @SneakyThrows
    public void delete(CatalogueUser user, String id) {
        repo.deleteData(FOLDER + id + ".meta")
            .deleteData(FOLDER + id + ".raw")
            .commit(user, "delete document: " + id);
    }

    private MetadataInfo createMetadataInfoWithDefaultPermissions(CatalogueUser user, String catalogue) {
        val metadataInfo = MetadataInfo.builder()
            .rawType(MediaType.APPLICATION_JSON_VALUE)
            .documentType("service-agreement")
            .catalogue(catalogue)
            .build();
        String username = user.getUsername();
        metadataInfo.addPermission(Permission.VIEW, username);
        metadataInfo.addPermission(Permission.EDIT, username);
        metadataInfo.addPermission(Permission.DELETE, username);
        return metadataInfo;
    }
}
