package uk.ac.ceh.gateway.catalogue.serviceagreement;

import org.springframework.http.ResponseEntity;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

public interface ServiceAgreementService {

    ServiceAgreement get(String id);

    ServiceAgreement create(CatalogueUser user, String id, String catalogue, ServiceAgreement serviceAgreement);

    ServiceAgreement update(CatalogueUser user, String id, ServiceAgreement serviceAgreement);

    void delete(CatalogueUser user, String id);

    boolean metadataRecordExists(String id);

    void populateGeminiDocument(CatalogueUser user, String id);

    ResponseEntity<Object> publishServiceAgreement(CatalogueUser user, String id);
}
