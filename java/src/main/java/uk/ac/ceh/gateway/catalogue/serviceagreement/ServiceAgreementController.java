package uk.ac.ceh.gateway.catalogue.serviceagreement;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

@Service
public class ServiceAgreementController {

    public List<ServiceAgreementSolrIndex> search(
        @RequestParam(value = "query", defaultValue = "*") String query
    ) {
        return Collections.emptyList();
    }
}
