package uk.ac.ceh.gateway.catalogue.serviceagreement;

import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.publication.StateResource;

public interface ServiceAgreementService {

    ServiceAgreement get(CatalogueUser user, String id);

    ServiceAgreement create(CatalogueUser user, String id, String catalogue, ServiceAgreement serviceAgreement);

    ServiceAgreement update(CatalogueUser user, String id, ServiceAgreement serviceAgreement);

    void updateMetadata(CatalogueUser user, String id, MetadataInfo metadataInfo);

    void delete(CatalogueUser user, String id);

    boolean metadataRecordExists(String id);

    void submitServiceAgreement(CatalogueUser user, String id);

    void publishServiceAgreement(CatalogueUser user, String id);

    void giveDepositorEditPermission(CatalogueUser user, String id);

    History getHistory(String id);

    ServiceAgreement getPreviousVersion(String id, String version);

    StateResource transitState(CatalogueUser user, String id, String toState);

    void doTransitionAction(CatalogueUser user, String id, String transitionId);
}
