package uk.ac.ceh.gateway.catalogue.serviceagreement;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Profile("service-agreement")
@Slf4j
@Service
public class SolrServiceAgreementSearch implements ServiceAgreementSearch {
    @Override
    public List<ServiceAgreementSolrIndex> query(String query) {
        log.debug(query);
        return Collections.emptyList();
    }
}
