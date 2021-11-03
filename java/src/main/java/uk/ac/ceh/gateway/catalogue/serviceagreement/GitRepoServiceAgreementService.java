package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.gateway.catalogue.document.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.document.reading.DocumentTypeLookupService;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.JiraService;

import static java.lang.String.format;

@Profile("service-agreement")
@Slf4j
@ToString
@Service
public class GitRepoServiceAgreementService implements ServiceAgreementService {
    private final DocumentTypeLookupService documentTypeLookupService;
    private final DataRepository<CatalogueUser> repo;
    private final DocumentInfoMapper<MetadataInfo> metadataInfoMapper;
    private final DocumentInfoMapper<ServiceAgreement> serviceAgreementMapper;
    private final DocumentRepository documentRepository;
    private final JiraService jiraService;
    private static final String FOLDER = "service-agreements/";
    private static final String PENDING_PUBLICATION = "Pending Publication";

    public GitRepoServiceAgreementService(
            DocumentTypeLookupService documentTypeLookupService,
            DataRepository<CatalogueUser> repo,
            DocumentInfoMapper<MetadataInfo> metadataInfoMapper,
            DocumentInfoMapper<ServiceAgreement> serviceAgreementMapper,
            DocumentRepository documentRepository,
            JiraService jiraService) {
        this.documentTypeLookupService = documentTypeLookupService;
        this.repo = repo;
        this.metadataInfoMapper = metadataInfoMapper;
        this.serviceAgreementMapper = serviceAgreementMapper;
        this.documentRepository = documentRepository;
        this.jiraService = jiraService;
        log.info("Creating");
    }

    @SneakyThrows
    public void populateGeminiDocument(CatalogueUser user, String id) {
        ServiceAgreement serviceAgreement = this.get(id);
        documentRepository.save(user, new GeminiDocument(serviceAgreement), "catalogue");
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

    @Override
    public boolean serviceAgreementExists(String id) {
        try {
            repo.getData(FOLDER + id + ".meta");
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
    public void save(CatalogueUser user, String id, String catalogue, ServiceAgreement serviceAgreement, MetadataInfo metadataInfo) {
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

    public MetadataInfo getMetadataInfo(String id) {
        try {
            return documentRepository.read(id).getMetadata();
        } catch (DocumentRepositoryException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ResponseEntity<Object> publishServiceAgreement(CatalogueUser user, String id) {
        ServiceAgreement serviceAgreement = get(id);
        jiraService.comment(serviceAgreement.getDepositReference(),
                format("Service agreement: " + serviceAgreement.getTitle() +
                " is now Pending Publication", user.getEmail()));
        serviceAgreement.setState(PENDING_PUBLICATION);
        MetadataInfo metadata = serviceAgreement.getMetadata();
        metadata.withState(PENDING_PUBLICATION);
        metadata.removePermission(Permission.EDIT, user.getUsername());
        this.save(user, id, serviceAgreement.getCatalogue(), serviceAgreement, metadata);
        return ResponseEntity.ok().build();
    }
}
