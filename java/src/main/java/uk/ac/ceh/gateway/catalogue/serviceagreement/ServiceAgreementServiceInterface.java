package uk.ac.ceh.gateway.catalogue.serviceagreement;

import org.springframework.http.ResponseEntity;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

public interface ServiceAgreementServiceInterface {

    boolean metadataRecordExists(String id);

    ServiceAgreement get(String id);

    void save(CatalogueUser user, String id, String catalogue, ServiceAgreement serviceAgreementDocument);

    void delete(CatalogueUser user, String id);
}
