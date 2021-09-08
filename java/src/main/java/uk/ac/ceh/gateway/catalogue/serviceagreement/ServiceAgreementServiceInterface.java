package uk.ac.ceh.gateway.catalogue.serviceagreement;

import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

public interface ServiceAgreementServiceInterface {

    boolean metadataRecordExists(String id);

    ServiceAgreement get(String id);

    void save(CatalogueUser user, String id, String catalogue, ServiceAgreement serviceAgreementDocument);

    DataRevision<CatalogueUser> delete(CatalogueUser user, String id);
}
