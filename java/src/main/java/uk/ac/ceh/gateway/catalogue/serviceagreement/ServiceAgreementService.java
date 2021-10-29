package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.SneakyThrows;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

public interface ServiceAgreementService {

    boolean metadataRecordExists(String id);

    boolean serviceAgreementExists(String id);

    ServiceAgreement get(String id);

    void save(CatalogueUser user, String id, String catalogue, ServiceAgreement serviceAgreement);

    void delete(CatalogueUser user, String id);

    void populateGeminiDocument(CatalogueUser user, String id, String catalogue);
}
