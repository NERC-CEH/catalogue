package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.gateway.catalogue.document.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Profile("service-agreement")
@Slf4j
@ToString
@Service
public class GitRepoServiceAgreementService implements ServiceAgreementService {
    private final DataRepository<CatalogueUser> repo;
    private final DocumentInfoMapper<MetadataInfo> metadataInfoMapper;
    private final DocumentInfoMapper<ServiceAgreement> serviceAgreementMapper;
    private static final String FOLDER = "service-agreements/";

    public GitRepoServiceAgreementService(
            DataRepository<CatalogueUser> repo,
            DocumentInfoMapper<MetadataInfo> metadataInfoMapper,
            DocumentInfoMapper<ServiceAgreement> serviceAgreementMapper
    ) {
        this.repo = repo;
        this.metadataInfoMapper = metadataInfoMapper;
        this.serviceAgreementMapper = serviceAgreementMapper;
        log.info("Creating");
    }


    @SneakyThrows
    public boolean metadataRecordExists(String id) {
        try {
            repo.getData(id + ".meta");
        } catch (DataRepositoryException e) {
            return false;
        }
        return true;
    }

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
    public void save(CatalogueUser user, String id, String catalogue, ServiceAgreement serviceAgreement) {
        val metadataInfo = createMetadataInfoWithDefaultPermissions(user, catalogue);
        repo.submitData(FOLDER + id + ".meta", (o) -> metadataInfoMapper.writeInfo(metadataInfo, o))
                .submitData(FOLDER + id + ".raw", (o) -> serviceAgreementMapper.writeInfo(serviceAgreement, o))
                .commit(user, catalogue);
    }

    @SneakyThrows
    public void delete(CatalogueUser user, String id) {
        repo.deleteData(FOLDER + id + ".meta")
                .deleteData(FOLDER + id + ".raw")
                .commit(user, "delete document: " + id);
    }

    private MetadataInfo createMetadataInfoWithDefaultPermissions(CatalogueUser user, String catalogue) {
        MetadataInfo toReturn = MetadataInfo.builder()
                .rawType(APPLICATION_JSON_VALUE)
                .documentType("service-agreement")
                .catalogue(catalogue)
                .build();
        String username = user.getUsername();
        toReturn.addPermission(Permission.VIEW, username);
        toReturn.addPermission(Permission.EDIT, username);
        toReturn.addPermission(Permission.DELETE, username);
        return toReturn;
    }
}
