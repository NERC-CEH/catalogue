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

import static java.lang.String.format;
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
            query.setSort("label", ORDER.asc);
            query.setRows(100);

            StringBuilder vocabs = new StringBuilder();

//            for (int i = 0; i < vocabIds.size(); i++) {
//
//                if (i == vocabIds.size() - 1) {
//                    vocabs.append(vocabIds.get(i));
//                } else {
//                    vocabs.append(vocabIds.get(i)).append("OR ");
//                }
//            }

            query.addFilterQuery(generateVocabQuery(vocabIds));

            return solrClient.query(COLLECTION, query, POST).getBeans(Keyword.class);

        } catch (IOException | SolrServerException | BaseHttpSolrClient.RemoteSolrException ex) {
            throw new SolrServerException(ex);
        }
    }

    private String generateVocabQuery(List<String> vocabIds) {
        StringBuilder toReturn = new StringBuilder("vocabId:(vocab1");

        if(vocabIds.size() > 0) {
            toReturn.append(" OR ");
            vocabIds
                    .stream()
                    .forEach(v -> {
                        toReturn
                                .append(" OR ")
                                .append(v);
                    });
        }
        return toReturn.append(")").toString();
    }

}

