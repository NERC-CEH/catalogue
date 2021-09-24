package uk.ac.ceh.gateway.catalogue.serviceagreement;

import java.util.List;

public interface ServiceAgreementSearch {
    List<ServiceAgreementSolrIndex> query(String query);
}
