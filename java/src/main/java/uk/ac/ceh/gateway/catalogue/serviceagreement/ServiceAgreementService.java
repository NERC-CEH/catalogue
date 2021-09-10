package uk.ac.ceh.gateway.catalogue.serviceagreement;

import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

public interface ServiceAgreementService {

    boolean metadataRecordExists(String id);

    ServiceAgreement get(String id);

    void save(CatalogueUser user, String id, String catalogue, ServiceAgreement serviceAgreement);

    void delete(CatalogueUser user, String id);
}
