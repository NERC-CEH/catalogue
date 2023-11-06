package uk.ac.ceh.gateway.catalogue.vocabularies;

import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.BaseHttpSolrClient;
import org.apache.solr.common.params.CommonParams;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

import static org.apache.solr.client.solrj.SolrRequest.METHOD.POST;

@Slf4j
@ToString
@Service
@AllArgsConstructor
public class KeywordVocabularySolrQueryService {
    private static final String COLLECTION = "keywords";
    private final SolrClient solrClient;

    public List<Keyword> query(String term, List<String> vocabIds) throws SolrServerException {
        try {
            SolrQuery query = new SolrQuery();
            query.setQuery(term);
            query.setParam(CommonParams.DF, "label");
            query.setSort("score", ORDER.desc);
            query.setRows(50);
            query.addFilterQuery(generateVocabQuery(vocabIds));
            log.debug(query.getQuery());
            log.debug("Filter queries: {}", (Object) query.getFilterQueries());

            return solrClient.query(COLLECTION, query, POST).getBeans(Keyword.class);

        } catch (IOException | SolrServerException | BaseHttpSolrClient.RemoteSolrException ex) {
            throw new SolrServerException(ex);
        }
    }

    private String generateVocabQuery(List<String> vocabIds) {

        if(vocabIds.isEmpty())
            return "";

        StringBuilder toReturn = new StringBuilder("vocabId:(" + vocabIds.get(0));
        if(vocabIds.size() > 1) {
            vocabIds
                    .stream()
                    .skip(1)
                    .forEach(v -> {
                        toReturn
                                .append(" OR ")
                                .append(v);
                    });
        }
        return toReturn.append(")").toString();
    }

}

