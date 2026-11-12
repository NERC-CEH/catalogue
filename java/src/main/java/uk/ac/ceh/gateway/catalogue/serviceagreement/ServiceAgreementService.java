package uk.ac.ceh.gateway.catalogue.serviceagreement;

import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.publication.StateResource;

import java.util.Optional;

public interface ServiceAgreementService {

    ServiceAgreement get(CatalogueUser user, String id);

    ServiceAgreement create(CatalogueUser user, String id, String catalogue, ServiceAgreement serviceAgreement);

    ServiceAgreement update(CatalogueUser user, String id, ServiceAgreement serviceAgreement);

    /**
     * As {@link #update}, but rejects the write with a
     * {@link uk.ac.ceh.gateway.catalogue.model.MetadataConflictException} if the service agreement has
     * changed since {@code expectedRevision} was issued. A null {@code expectedRevision} skips the check,
     * for the server-side workflow paths that have no editing session to protect.
     */
    ServiceAgreement update(CatalogueUser user, String id, ServiceAgreement serviceAgreement, String expectedRevision);

    void updateMetadata(CatalogueUser user, String id, MetadataInfo metadataInfo);

    /** As {@link #updateMetadata}, with the optimistic-lock check of {@link #update(CatalogueUser, String, ServiceAgreement, String)}. */
    void updateMetadata(CatalogueUser user, String id, MetadataInfo metadataInfo, String expectedRevision);

    /**
     * The current per-document revision token for this service agreement, as served in the {@code ETag}
     * of a GET and required back in the {@code If-Match} of a PUT. Null if it has no history yet.
     */
    String getRevisionToken(String id);

    void delete(CatalogueUser user, String id);

    boolean metadataRecordExists(String id);

    void submitServiceAgreement(CatalogueUser user, String id);

    void publishServiceAgreement(CatalogueUser user, String id);

    void giveDepositorEditPermission(CatalogueUser user, String id);

    History getHistory(String id);

    ServiceAgreement getPreviousVersion(String id, String version);

    StateResource transitState(CatalogueUser user, String id, String toState);

    void doTransitionAction(CatalogueUser user, String id, String transitionId);

    /**
     * The file listing of the service agreement as last agreed, for the Hubbub upload page. Empty when the
     * agreement does not exist, has never been agreed, or was agreed before snapshots were kept and has since
     * been sent back to draft.
     *
     * <p>Takes no user: it reads past the agreement's permissions, which change at publication, so the
     * caller must have checked access already.</p>
     */
    Optional<AgreedFiles> agreedFiles(String id);
}
