package uk.ac.ceh.gateway.catalogue.serviceagreement;

import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;

public interface ServiceAgreementService {

    boolean metadataRecordExists(String id);

    boolean serviceAgreementExists(String id);

    ServiceAgreement get(String id);

    void save(CatalogueUser user, String id, String catalogue, ServiceAgreement serviceAgreement, MetadataInfo metadataInfo);

    void delete(CatalogueUser user, String id);

    void populateGeminiDocument(CatalogueUser user, String id);

    MetadataInfo getMetadataInfo(String id);

}
