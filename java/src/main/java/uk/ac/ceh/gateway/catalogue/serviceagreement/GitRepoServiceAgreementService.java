package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import uk.ac.ceh.components.datastore.DataDocument;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.gateway.catalogue.document.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.publication.ServiceAgreementPublicationService;
import uk.ac.ceh.gateway.catalogue.publication.StateResource;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.JiraService;

import java.util.Optional;

import static java.lang.String.format;
import static uk.ac.ceh.gateway.catalogue.model.Permission.*;

@Profile("service-agreement")
@Slf4j
@ToString
@Service
public class GitRepoServiceAgreementService implements ServiceAgreementService {
    private final String baseUri;
    private final DataRepository<CatalogueUser> repo;
    private final DocumentInfoMapper<MetadataInfo> metadataInfoMapper;
    private final DocumentInfoMapper<ServiceAgreement> serviceAgreementMapper;
    private final DocumentRepository documentRepository;
    private final JiraService jiraService;
    private final ServiceAgreementPublicationService publicationService;
    private final String PUBLISHED = "published";
    public static final String FOLDER = "service-agreement/";
    private static final String DRAFT = "draft";
    private static final String PENDING_PUBLICATION = "pending publication";

    public GitRepoServiceAgreementService(
            @Value("${documents.baseUri}") String baseUri,
            DataRepository<CatalogueUser> repo,
            DocumentInfoMapper<MetadataInfo> metadataInfoMapper,
            DocumentInfoMapper<ServiceAgreement> serviceAgreementMapper,
            DocumentRepository documentRepository,
            JiraService jiraService,
            @Lazy ServiceAgreementPublicationService publicationService) {
        this.baseUri = baseUri;
        this.repo = repo;
        this.metadataInfoMapper = metadataInfoMapper;
        this.serviceAgreementMapper = serviceAgreementMapper;
        this.documentRepository = documentRepository;
        this.jiraService = jiraService;
        this.publicationService = publicationService;
        log.info("Creating");
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
    public ServiceAgreement get(CatalogueUser user, String id) {
        log.debug("GETting service agreement: {}", id);
        val serviceAgreement = dataDocumentToServiceAgreement(
            repo.getData(FOLDER + id + ".raw"),
            repo.getData(FOLDER + id + ".meta")
        );
        serviceAgreement.setHistorical(false);
        serviceAgreement.setCurrentStateResource(publicationService.current(user, serviceAgreement));
        return serviceAgreement;
    }

    @Override
    @SneakyThrows
    public StateResource transitState(CatalogueUser user, String id, String toState) {
        return publicationService.transition(user, id, toState);
    }

    @SneakyThrows
    @Override
    public ServiceAgreement create(CatalogueUser user, String id, String catalogue, ServiceAgreement serviceAgreement) {
        serviceAgreement.setId(id);
        val metadataInfo = createMetadataInfoWithDefaultPermissions(
                user,
                catalogue
        );
        addPermissionsForDepositor(metadataInfo, serviceAgreement);
        repo
            .submitData(FOLDER + id + ".meta", (o) -> metadataInfoMapper.writeInfo(metadataInfo, o))
            .submitData(FOLDER + id + ".raw", (o) -> serviceAgreementMapper.writeInfo(serviceAgreement, o))
            .commit(user, "creating service agreement " + id);
        return get(user, id);
    }

    @SneakyThrows
    @Override
    public ServiceAgreement update(CatalogueUser user, String id, ServiceAgreement serviceAgreement) {
        serviceAgreement.setId(id);
        val fromDatastore = repo.getData(FOLDER + id + ".meta");
        val metadataInfo = metadataInfoMapper.readInfo(fromDatastore.getInputStream());
        addPermissionsForDepositor(metadataInfo, serviceAgreement);
        repo
            .submitData(FOLDER + id + ".meta", (o) -> metadataInfoMapper.writeInfo(metadataInfo, o))
            .submitData(FOLDER + id + ".raw", (o) -> serviceAgreementMapper.writeInfo(serviceAgreement, o))
            .commit(user, "updating service agreement " + id);
        return get(user, id);
    }

    @SneakyThrows
    @Override
    public void updateMetadata(CatalogueUser user, String id, MetadataInfo metadataInfo) {
        repo
            .submitData(FOLDER + id + ".meta", (o) -> metadataInfoMapper.writeInfo(metadataInfo, o))
            .commit(user, "updating service agreement metadata " + id);
    }

    @SneakyThrows
    public void delete(CatalogueUser user, String id) {
        repo
            .deleteData(FOLDER + id + ".meta")
            .deleteData(FOLDER + id + ".raw")
            .commit(user, "delete document: " + id);
    }

    private void addPermissionsForDepositor(MetadataInfo metadataInfo, ServiceAgreement serviceAgreement) {
        val possibleEmail = Optional.ofNullable(serviceAgreement.getDepositorContactDetails());
        if (possibleEmail.isPresent()) {
            val rawEmail = possibleEmail.get();
            val email = (rawEmail.endsWith("@ceh.ac.uk")) ?
                    rawEmail.replace("@ceh.ac.uk", "") :
                    rawEmail;
            metadataInfo.addPermission(EDIT, email);
            metadataInfo.addPermission(VIEW, email);
        } else {
            val message = format(
                    "No depositor contact details present, cannot add permissions for Service Agreement: %s",
                    serviceAgreement.getId()
            );
            throw new ServiceAgreementException(message);
        }
    }

    private void removeEditPermissions(CatalogueUser user, String id, ServiceAgreement serviceAgreement) {
        val metadataInfo = serviceAgreement.getMetadata();
        val possibleEmail = Optional.ofNullable(serviceAgreement.getDepositorContactDetails());
        if (possibleEmail.isPresent()) {
            val email = possibleEmail.get();
            metadataInfo.removePermission(EDIT, email);
            metadataInfo.removePermission(EDIT, user.getUsername());
            metadataInfo.removePermission(UPLOAD, email);
            metadataInfo.removePermission(UPLOAD, user.getUsername());
        } else {
            metadataInfo.removePermission(EDIT, user.getUsername());
            metadataInfo.removePermission(UPLOAD, user.getUsername());
        }
        updateMetadata(user, id, metadataInfo);
    }

    private MetadataInfo createMetadataInfoWithDefaultPermissions(CatalogueUser user, String catalogue) {
        val metadataInfo = MetadataInfo.builder()
                .rawType(MediaType.APPLICATION_JSON_VALUE)
                .documentType("service-agreement")
                .catalogue(catalogue)
                .build();
        String username = user.getUsername();
        metadataInfo.addPermission(VIEW, username);
        metadataInfo.addPermission(EDIT, username);
        metadataInfo.addPermission(DELETE, username);
        return metadataInfo;
    }

    public void submitServiceAgreement(CatalogueUser user, String id) {
        ServiceAgreement serviceAgreement = get(user, id);
        String metadataRecordState = serviceAgreement.getState();
        if (metadataRecordState.equals(DRAFT)) {
            try {
                Optional.ofNullable(serviceAgreement.getDepositReference())
                        .ifPresent(depositReference ->
                                jiraService.comment(
                                        depositReference,
                                        format(
                                                "Service Agreement (%s): %s submitted for review",
                                                serviceAgreement.getId(),
                                                serviceAgreement.getTitle()
                                        )
                                )
                        );
            } catch (RestClientResponseException ex) {
                throw new ServiceAgreementException("Unable to comment on Jira issue");
            }
            removeEditPermissions(user, id, serviceAgreement);
            updateState(user, id, serviceAgreement, PENDING_PUBLICATION);
        } else {
            val message = format(
                    "Cannot submit ServiceAgreement %s as state is %s",
                    id,
                    metadataRecordState
            );
            throw new ServiceAgreementException(message);
        }
    }

    @SneakyThrows
    public void publishServiceAgreement(CatalogueUser user, String id) {
        val gemini = (GeminiDocument) documentRepository.read(id);
        val metadataRecordState = gemini.getState();
        val serviceAgreement = get(user, id);
        val serviceAgreementState = serviceAgreement.getState();
        log.debug("gemini: {}, service agreement: {}", metadataRecordState, serviceAgreementState);
        if (metadataRecordState.equals(DRAFT) && serviceAgreementState.equals(PENDING_PUBLICATION)) {
            log.info("Gemini document populated from Service Agreement: {}", id);
            gemini.populateFromServiceAgreement(serviceAgreement);
            documentRepository.save(
                    user,
                    gemini,
                    "populated from service agreement"
            );
            log.info("Publishing Service Agreement: {}", id);
            try {
                Optional.ofNullable(serviceAgreement.getDepositReference())
                        .ifPresent(depositReference ->
                                jiraService.comment(
                                        depositReference,
                                        format(
                                                "Service Agreement (%s): %s has been agreed upon and published",
                                                serviceAgreement.getId(),
                                                serviceAgreement.getTitle()
                                        )
                                )
                        );
            } catch (RestClientResponseException ex) {
                throw new ServiceAgreementException("Unable to comment on Jira issue");
            }
            updateState(user, id, serviceAgreement, PUBLISHED);
        } else {
            val message = format(
                    "Cannot publish Service Agreement %s as state is %s and GeminiDocument state is %s",
                    id,
                    serviceAgreementState,
                    metadataRecordState
            );
            throw new ServiceAgreementException(message);
        }
    }

    @SneakyThrows
    public void giveDepositorEditPermission(CatalogueUser user, String id) {
        val serviceAgreement = get(user, id);
        val serviceAgreementState = serviceAgreement.getState();
        val metadata = serviceAgreement.getMetadata();
        val gemini = (GeminiDocument) documentRepository.read(id);
        val metadataRecordState = gemini.getState();
        if (metadataRecordState.equals(DRAFT) && serviceAgreementState.equals(PENDING_PUBLICATION)) {
            try {
                Optional.ofNullable(serviceAgreement.getDepositReference())
                        .ifPresent(depositReference ->
                                jiraService.comment(
                                        depositReference,
                                        format(
                                                "Service Agreement (%s): %s has been sent back for further changes",
                                                serviceAgreement.getId(),
                                                serviceAgreement.getTitle()
                                        )
                                )
                        );
            } catch (RestClientResponseException ex) {
                throw new ServiceAgreementException("Unable to comment on Jira issue");
            }
            addPermissionsForDepositor(metadata, serviceAgreement);
            updateState(user, id, serviceAgreement, DRAFT);
        } else {
            val message = format(
                    "Cannot return edit permission for Service Agreement %s as state is %s and GeminiDocument state is %s",
                    id,
                    serviceAgreementState,
                    metadataRecordState
            );
            throw new ServiceAgreementException(message);
        }
    }

    @SneakyThrows
    public History getHistory(String id) {
        try {
            val dataRevisions = repo.getRevisions(FOLDER + id + ".raw");
            return new History(baseUri, id, dataRevisions);
        } catch (DataRepositoryException ex) {
            throw new ServiceAgreementException((ex.getMessage()));
        }
    }

    @SneakyThrows
    public ServiceAgreement getPreviousVersion(
            String id,
            String version
    ) {
        log.debug("Previous version: {} of service agreement: {}", version, id);
        val serviceAgreement = dataDocumentToServiceAgreement(
            repo.getData(version, FOLDER + id + ".raw"),
            repo.getData(version, FOLDER + id + ".meta")
        );
        serviceAgreement.setHistorical(true);
        return serviceAgreement;
    }

    @SneakyThrows
    private ServiceAgreement dataDocumentToServiceAgreement(DataDocument dataDoc, DataDocument metadataDoc) {
        val metadataInfo = metadataInfoMapper.readInfo(metadataDoc.getInputStream());
        log.debug("metadataInfo = {}", metadataInfo);

        val serviceAgreement = serviceAgreementMapper.readInfo(dataDoc.getInputStream());
        serviceAgreement.setMetadata(metadataInfo);
        log.debug("Service Agreement: {}", serviceAgreement);

        return serviceAgreement;
    }

    private void updateState(CatalogueUser user, String id, ServiceAgreement serviceAgreement, String state) {
        val metadataInfo = serviceAgreement.getMetadata();
        updateMetadata(user, id, metadataInfo.withState(state));
    }
}
