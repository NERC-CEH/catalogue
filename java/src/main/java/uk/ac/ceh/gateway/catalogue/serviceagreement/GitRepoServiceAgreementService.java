package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import uk.ac.ceh.components.datastore.DataDocument;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.git.GitFileNotFoundException;
import uk.ac.ceh.gateway.catalogue.config.ServiceAgreementPublicationConfig;
import uk.ac.ceh.gateway.catalogue.document.DocumentInfoMapper;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataConflictException;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.publication.StateResource;
import uk.ac.ceh.gateway.catalogue.publication.Workflow;
import uk.ac.ceh.gateway.catalogue.repository.CachedDataRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.JiraService;

import java.time.Clock;
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
    private final CachedDataRepository cachedDataRepository;
    private final DocumentInfoMapper<MetadataInfo> metadataInfoMapper;
    private final DocumentInfoMapper<ServiceAgreement> serviceAgreementMapper;
    private final DocumentRepository documentRepository;
    private final JiraService jiraService;
    private final ServiceAgreementPublicationService publicationService;
    private final Workflow workflow;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    public static final String PUBLISHED = "published";
    public static final String FOLDER = "service-agreement/";
    public static final String DRAFT = "draft";
    private static final String SUBMITTED = "submitted";
    private static final String PENDING_PUBLICATION = "pending publication";
    private static final String CEH_DOMAIN = "@ceh.ac.uk";
    /** The workflow state an agreement is in once agreed; see {@link ServiceAgreementPublicationConfig}. */
    private static final String AGREED = "agreed";
    /**
     * Suffix of the snapshot of the agreed file listing, beside the agreement's {@code .meta} and
     * {@code .raw}. Only those two make a document to the listing and indexing code, so this one is
     * never listed or indexed.
     */
    private static final String AGREED_FILES_SUFFIX = ".agreed.json";

    /**
     * How far back {@link #getHistory} looks: five years, in seconds.
     *
     * <p>Named rather than inline because the test used to restate the whole
     * expression, which made its assertion a restatement of the code under test
     * rather than a check on it.
     */
    static final long HISTORY_WINDOW_SECONDS = 157_680_000L;

    /**
     * Annotated because there are two constructors and Spring will not choose
     * between them: without it the context fails to start with "no default
     * constructor found", which the production-context tests catch.
     */
    @Autowired
    public GitRepoServiceAgreementService(
            @Value("${documents.baseUri}") String baseUri,
            DataRepository<CatalogueUser> repo,
            CachedDataRepository cachedDataRepository,
            DocumentInfoMapper<MetadataInfo> metadataInfoMapper,
            DocumentInfoMapper<ServiceAgreement> serviceAgreementMapper,
            DocumentRepository documentRepository,
            JiraService jiraService,
            @Lazy ServiceAgreementPublicationService publicationService,
            @Qualifier("service-agreement") Workflow workflow,
            ObjectMapper objectMapper) {
        this(baseUri, repo, cachedDataRepository, metadataInfoMapper, serviceAgreementMapper,
                documentRepository, jiraService, publicationService, workflow, objectMapper, Clock.systemUTC());
    }

    /** Package-private, so a test can fix the instant the history window is measured back from. */
    GitRepoServiceAgreementService(
            String baseUri,
            DataRepository<CatalogueUser> repo,
            CachedDataRepository cachedDataRepository,
            DocumentInfoMapper<MetadataInfo> metadataInfoMapper,
            DocumentInfoMapper<ServiceAgreement> serviceAgreementMapper,
            DocumentRepository documentRepository,
            JiraService jiraService,
            ServiceAgreementPublicationService publicationService,
            Workflow workflow,
            ObjectMapper objectMapper,
            Clock clock) {
        this.baseUri = baseUri;
        this.repo = repo;
        this.cachedDataRepository = cachedDataRepository;
        this.metadataInfoMapper = metadataInfoMapper;
        this.serviceAgreementMapper = serviceAgreementMapper;
        this.documentRepository = documentRepository;
        this.jiraService = jiraService;
        this.publicationService = publicationService;
        this.workflow = workflow;
        this.objectMapper = objectMapper;
        this.clock = clock;
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

    @Override
    @SneakyThrows
    public void doTransitionAction(CatalogueUser user, String id, String transitionId) {
        switch (transitionId) {
            case ServiceAgreementPublicationConfig.draftToSubmittedId -> submitServiceAgreement(user, id);
            case ServiceAgreementPublicationConfig.submittedToDraftId, ServiceAgreementPublicationConfig.underReviewToDraftId -> {
                ServiceAgreement serviceAgreement = get(user, id);
                addPermissionsForDepositor(user, id, serviceAgreement.getMetadata(), serviceAgreement);
            }
            case ServiceAgreementPublicationConfig.readyForAgreementToAgreedId -> publishServiceAgreement(user, id);
            case ServiceAgreementPublicationConfig.readyForAgreementToDraftId, ServiceAgreementPublicationConfig.agreedToDraftId -> {
                ServiceAgreement serviceAgreement = get(user, id);
                addPermissionsForDepositor(user, id, serviceAgreement.getMetadata(), serviceAgreement);
                sendJiraComment(serviceAgreement, user, "sent back to draft status for re-edit");
            }
            default -> {}
        }
    }

    @SneakyThrows
    @Override
    public ServiceAgreement create(CatalogueUser user, String id, String catalogue, ServiceAgreement serviceAgreement) {
        serviceAgreement.setId(id);
        val metadataInfo = createMetadataInfoWithDefaultPermissions(
                user,
                catalogue
        );
        addPermissionsForDepositor(user, id, metadataInfo, serviceAgreement);
        repo
            .submitData(FOLDER + id + ".meta", o -> metadataInfoMapper.writeInfo(metadataInfo, o))
            .submitData(FOLDER + id + ".raw", o -> serviceAgreementMapper.writeInfo(serviceAgreement, o))
            .commit(user, "creating service agreement " + id);
        cachedDataRepository.evictAfterDirectWrite(FOLDER + id);
        return get(user, id);
    }

    @SneakyThrows
    @Override
    public ServiceAgreement update(CatalogueUser user, String id, ServiceAgreement serviceAgreement) {
        return update(user, id, serviceAgreement, null);
    }

    @SneakyThrows
    @Override
    public ServiceAgreement update(CatalogueUser user, String id, ServiceAgreement serviceAgreement, String expectedRevision) {
        serviceAgreement.setId(id);
        // Guard read-check-write as one unit, as GitRepoWrapper.save does for metadata documents: two
        // concurrent saves must not both pass the check before either commits.
        synchronized (this) {
            checkRevision(id, expectedRevision, serviceAgreement);
            val fromDatastore = repo.getData(FOLDER + id + ".meta");
            val metadataInfo = metadataInfoMapper.readInfo(fromDatastore.getInputStream());
            // A stub may get its depositor's email on any save. Only in draft, though: submitting removes the
            // depositor's edit access, and a publisher's save during review must not hand it back.
            if (DRAFT.equals(metadataInfo.getState())) {
                grantDepositorAccess(id, metadataInfo, serviceAgreement);
            }
            repo
                .submitData(FOLDER + id + ".meta", o -> metadataInfoMapper.writeInfo(metadataInfo, o))
                .submitData(FOLDER + id + ".raw", o -> serviceAgreementMapper.writeInfo(serviceAgreement, o))
                .commit(user, "updating service agreement " + id);
        }
        cachedDataRepository.evictAfterDirectWrite(FOLDER + id);
        return get(user, id);
    }

    @SneakyThrows
    @Override
    public void updateMetadata(CatalogueUser user, String id, MetadataInfo metadataInfo) {
        updateMetadata(user, id, metadataInfo, null);
    }

    @SneakyThrows
    @Override
    public void updateMetadata(CatalogueUser user, String id, MetadataInfo metadataInfo, String expectedRevision) {
        synchronized (this) {
            checkRevision(id, expectedRevision, null);
            repo
                .submitData(FOLDER + id + ".meta", o -> metadataInfoMapper.writeInfo(metadataInfo, o))
                .commit(user, "updating service agreement metadata " + id);
        }
        cachedDataRepository.evictAfterDirectWrite(FOLDER + id);
    }

    @SneakyThrows
    @Override
    public String getRevisionToken(String id) {
        return cachedDataRepository.getDocumentRevisionToken(FOLDER + id);
    }

    /**
     * Compare-then-commit half of the optimistic lock. Reads the token fresh from the datastore rather
     * than via {@link CachedDataRepository#getDocumentRevisionToken} — checking against a cached value
     * would be no check at all. Must be called inside the {@code synchronized} block that also performs
     * the commit.
     */
    private void checkRevision(String id, String expectedRevision, ServiceAgreement submittedForEcho)
        throws DataRepositoryException {
        if (expectedRevision == null) {
            return;
        }
        val current = CachedDataRepository.revisionToken(repo, FOLDER + id);
        if (!expectedRevision.equals(current)) {
            throw new MetadataConflictException(
                "This service agreement, %s, was changed by another user since you opened it.".formatted(id), submittedForEcho);
        }
    }

    @SneakyThrows
    public void delete(CatalogueUser user, String id) {
        var deletion = repo
            .deleteData(FOLDER + id + ".meta")
            .deleteData(FOLDER + id + ".raw");
        // Only an agreement that has been agreed has a snapshot
        if (agreedFilesDocument(id).isPresent()) {
            deletion = deletion.deleteData(FOLDER + id + AGREED_FILES_SUFFIX);
        }
        deletion.commit(user, "delete document: " + id);
        cachedDataRepository.evictAfterDirectWrite(FOLDER + id);
    }

    private void addPermissionsForDepositor(CatalogueUser user, String id, MetadataInfo metadataInfo, ServiceAgreement serviceAgreement) {
        if (grantDepositorAccess(id, metadataInfo, serviceAgreement)) {
            updateMetadata(user, id, metadataInfo);
        }
    }

    /**
     * Grants the depositor EDIT/VIEW in {@code metadataInfo}, without writing it. A stub service agreement may
     * not have a depositor's email yet; then nobody is granted anything, and the quality check stops it being
     * submitted until one is added.
     *
     * @return whether a depositor was granted access
     */
    private boolean grantDepositorAccess(String id, MetadataInfo metadataInfo, ServiceAgreement serviceAgreement) {
        val rawEmail = serviceAgreement.getDepositorContactDetails();
        if (rawEmail == null || rawEmail.isBlank()) {
            return false;
        }
        val identity = normaliseIdentity(rawEmail);
        log.info(
            "Service Agreement {}: granting EDIT/VIEW to depositor identity '{}' (from contact details '{}')",
            id, identity, rawEmail
        );
        metadataInfo.addPermission(EDIT, identity);
        metadataInfo.addPermission(VIEW, identity);
        return true;
    }

    private String normaliseIdentity(String rawEmail) {
        val email = rawEmail.toLowerCase();
        return email.endsWith(CEH_DOMAIN)
            ? email.substring(0, email.length() - CEH_DOMAIN.length())
            : email;
    }

    private void removeEditPermissions(CatalogueUser user, String id, ServiceAgreement serviceAgreement) {
        val metadataInfo = serviceAgreement.getMetadata();
        Optional.ofNullable(serviceAgreement.getDepositorContactDetails()).ifPresent(rawEmail -> {
            val identity = normaliseIdentity(rawEmail);
            log.info("Service Agreement {}: removing EDIT/UPLOAD from depositor identity '{}'", id, identity);
            metadataInfo.removePermission(EDIT, identity);
            metadataInfo.removePermission(UPLOAD, identity);
        });
        metadataInfo.removePermission(EDIT, user.getUsername().toLowerCase());
        metadataInfo.removePermission(UPLOAD, user.getUsername().toLowerCase());
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

    private void sendJiraComment(ServiceAgreement serviceAgreement, CatalogueUser user, String comment) {
        try {
            Optional.ofNullable(serviceAgreement.getDepositReference()).ifPresent(depositReference ->
                jiraService.comment(
                    depositReference,
                    format(
                        "Service Agreement (%s): %s was %s by %s",
                        serviceAgreement.getId(),
                        serviceAgreement.getTitle(),
                        comment,
                        user.getUsername()
                    )
                )
            );
        } catch (RestClientResponseException ex) {
            throw new ServiceAgreementException("Unable to comment on Jira issue");
        }
    }

    @SneakyThrows
    public void submitServiceAgreement(CatalogueUser user, String id) {
        ServiceAgreement serviceAgreement = get(user, id);
        String metadataRecordState = serviceAgreement.getState();
        if (metadataRecordState.equals(SUBMITTED)) {
            removeEditPermissions(user, id, serviceAgreement);
            sendJiraComment(serviceAgreement, user, "submitted for review");
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
        val serviceAgreement = get(user, id);
        // Before the dataset record's check: the workflow has already committed the agreed state, so the
        // snapshot must follow every agreement, including a re-agreement after the record was published.
        // Only when agreed, though: anyone who can edit can also reach this through POST {id}/publish, and
        // must not be able to snapshot content nobody agreed.
        if (isAgreed(serviceAgreement.getMetadata())) {
            saveAgreedFiles(user, id, serviceAgreement);
        }
        val gemini = (GeminiDocument) documentRepository.read(id);
        val metadataRecordState = gemini.getState();
        val serviceAgreementState = serviceAgreement.getState();
        log.debug("gemini: {}, service agreement: {}", metadataRecordState, serviceAgreementState);
        if (metadataRecordState.equals(DRAFT)) {
            log.info("Gemini document populated from Service Agreement: {}", id);
            gemini.populateFromServiceAgreement(serviceAgreement);
            documentRepository.save(
                    user,
                    gemini,
                    "populated from service agreement"
            );
            log.info("Publishing Service Agreement: {}", id);
            removeEditPermissions(user, id, serviceAgreement);
            sendJiraComment(serviceAgreement, user, "agreed upon and published");
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
            sendJiraComment(serviceAgreement, user, "sent back for further changes");
            addPermissionsForDepositor(user, id, metadata, serviceAgreement);
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
            val timeLimitInSecond = clock.instant().getEpochSecond() - HISTORY_WINDOW_SECONDS;
            val dataRevisions = repo.getRevisions(timeLimitInSecond, "(creating|updating) service agreement " + id);
            return new History(baseUri, id, dataRevisions);
        } catch (DataRepositoryException ex) {
            // The datastore's message carries internal detail, and this one is shown to the user
            log.error("Could not read history of service agreement {}", id, ex);
            throw new ServiceAgreementException(format("Could not read history of Service Agreement: %s", id));
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

    /**
     * Keeps the file listing as agreed, so the upload page can check against it after the agreement is sent
     * back to draft and edited. Its own commit, as the agreement's commits are made by the workflow and
     * {@link #removeEditPermissions}; its message must not match the {@link #getHistory} pattern, or it
     * would show as a version of the agreement.
     */
    @SneakyThrows
    private void saveAgreedFiles(CatalogueUser user, String id, ServiceAgreement serviceAgreement) {
        val snapshot = AgreedFiles.of(serviceAgreement, false);
        repo
            .submitData(FOLDER + id + AGREED_FILES_SUFFIX, o -> objectMapper.writeValue(o, snapshot))
            .commit(user, "saving agreed files of service agreement " + id);
        cachedDataRepository.evictAfterDirectWrite(FOLDER + id);
    }

    /**
     * Reads at HEAD only, a blob lookup per file: never the history, which is an unbounded walk of the
     * whole datastore.
     */
    @Override
    @SneakyThrows
    public Optional<AgreedFiles> agreedFiles(String id) {
        final MetadataInfo metadataInfo;
        try {
            metadataInfo = metadataInfoMapper.readInfo(repo.getData(FOLDER + id + ".meta").getInputStream());
        } catch (GitFileNotFoundException noAgreement) {
            return Optional.empty();
        } catch (DataRepositoryException ex) {
            log.warn("{}: service agreement unreadable, so no agreed files", id, ex);
            return Optional.empty();
        }
        val agreed = isAgreed(metadataInfo);
        val snapshot = readAgreedFilesSnapshot(id);
        if (snapshot.isPresent()) {
            return Optional.of(snapshot.get().withRevising(!agreed));
        }
        if (agreed) {
            // Agreed before snapshots were kept: what it holds now is what was agreed
            val current = serviceAgreementMapper.readInfo(repo.getData(FOLDER + id + ".raw").getInputStream());
            return Optional.of(AgreedFiles.of(current, false));
        }
        // Sent back to draft without a snapshot: what was agreed is no longer known
        return Optional.empty();
    }

    private boolean isAgreed(MetadataInfo metadataInfo) {
        return AGREED.equals(workflow.currentState(metadataInfo).getId());
    }

    @SneakyThrows
    private Optional<AgreedFiles> readAgreedFilesSnapshot(String id) {
        val document = agreedFilesDocument(id);
        if (document.isEmpty()) {
            return Optional.empty();
        }
        try (val in = document.get().getInputStream()) {
            return Optional.of(objectMapper.readValue(in, AgreedFiles.class));
        }
    }

    private Optional<DataDocument> agreedFilesDocument(String id) {
        try {
            return Optional.ofNullable(repo.getData(FOLDER + id + AGREED_FILES_SUFFIX));
        } catch (GitFileNotFoundException noSnapshot) {
            return Optional.empty();
        } catch (DataRepositoryException ex) {
            log.warn("{}: agreed files snapshot unreadable, so treated as absent", id, ex);
            return Optional.empty();
        }
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
}
