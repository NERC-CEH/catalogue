package uk.ac.ceh.gateway.catalogue.organisations;

import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.request.SolrQuery;
import org.apache.solr.client.solrj.request.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.RemoteSolrException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

import static org.apache.solr.client.solrj.SolrRequest.METHOD.POST;

@Slf4j
@ToString
@Service
@AllArgsConstructor
public class OrganisationSolrQueryService {
    private static final String COLLECTION = "organisations";
    private final SolrClient solrClient;

    public List<Organisation> query(String term) throws SolrServerException {
        try {
            SolrQuery query = new SolrQuery();
            query.setQuery(term);
            query.setParam("defType", "edismax");
            query.setParam("qf", "name^50 acronyms^20 aliases^20");
            query.setSort("score", ORDER.desc);
            query.setRows(10);
            log.debug(query.getQuery());

            return solrClient.query(COLLECTION, query, POST).getBeans(Organisation.class);

        } catch (IOException | SolrServerException | RemoteSolrException ex) {
            throw new SolrServerException(ex);
        }
    }

    /**
     * The organisation with a ROR identifier, as the only item of the list, or an empty list for one
     * not in ROR. The editor describes the organisation a record already has with it.
     */
    public List<Organisation> get(String id) throws SolrServerException {
        try {
            SolrQuery query = new SolrQuery("*:*");
            // The term parser matches the identifier as it is, so its ":" and "/" need no escaping
            query.addFilterQuery("{!term f=id}" + id);
            query.setRows(1);

            return solrClient.query(COLLECTION, query, POST).getBeans(Organisation.class);

        } catch (IOException | SolrServerException | RemoteSolrException ex) {
            throw new SolrServerException(ex);
        }
    }
}

