package uk.ac.ceh.gateway.catalogue.controllers;

import java.io.IOException;
import java.util.*;
import org.apache.solr.client.solrj.*;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uk.ac.ceh.gateway.catalogue.model.SolrVocabularyIndex;
import uk.ac.ceh.gateway.catalogue.semanticSearch.SearchResults;
import uk.ac.ceh.gateway.catalogue.semanticSearch.SearchResults.VocabularySearchResult;
import uk.ac.ceh.gateway.catalogue.semanticSearch.Link;

@Controller
public class VocabSolrController {
    private SolrClient solrClient;

    public VocabSolrController(SolrClient solrClient) {
        this.solrClient = solrClient;
    }

    @RequestMapping(value = "vocab", method = RequestMethod.GET)
    public @ResponseBody SearchResults<VocabularySearchResult> searchKeyword (
            @RequestParam(value = "term", required = false) String term,
            @RequestParam(value = "start", defaultValue = "0") int start,
            @RequestParam(value = "rows", defaultValue = "20") int rows
            ) throws SolrServerException, IOException {

        SolrQuery query = new SolrQuery()
                .setQuery(term)
                .setParam("defType", "edismax")
                .setParam("fq", "type:keyword")
                .setParam("q.alt", "*:*")
                .setParam("qf", "term uri")
                .setStart(start)
                .setRows(rows)
                .setHighlight(true)
                .setHighlightSimplePre("<em>")
                .setHighlightSimplePost("</em>");

        return performQuery(query);
    }

    @RequestMapping(method = RequestMethod.GET, value = "codelist/{codelist}")
    public @ResponseBody List<Link> getCodelist (@PathVariable("codelist") String codelistName) throws SolrServerException, IOException {

        SolrQuery query = new SolrQuery()
                .setQuery("*:*")
                .setParam("defType", "edismax")
                .setParam("fq", "type:codelist")
                .setParam("fq", "vocab:" + codelistName)
                .setParam("qf", "term uri")
                .setStart(0)
                .setRows(30);

        return performCodelistQuery(query);
    }

    private SearchResults<VocabularySearchResult> performQuery(SolrQuery query) throws SolrServerException, IOException {
        QueryResponse response = solrClient.query(query, METHOD.POST);
        Map<String, Map<String, List<String>>> highlighting = response.getHighlighting();

        List<VocabularySearchResult> searchResults = new ArrayList<>();
        for(SolrVocabularyIndex result : response.getBeans(SolrVocabularyIndex.class)) {
            Map<String, List<String>> highlightedResults = highlighting.get(result.getUri());
            List<String> highlightedTerm = highlightedResults.get("term");

            searchResults.add(new VocabularySearchResult()
                                    .setUri(result.getUri())
                                    .setTerm(result.getTerm())
                                    .setVocab(result.getVocab())
                                    .setHighlightedTerm(highlightedTerm != null ? highlightedTerm.get(0) : null));
        }

        return new SearchResults<VocabularySearchResult>()
                .setHeader(new SearchResults.Header()
                    .setRows(query.getRows())
                    .setStart(query.getStart())
                    .setNumFound(response.getResults().getNumFound())
                )
                .setResults(searchResults);
    }

    private List<Link> performCodelistQuery(SolrQuery query) throws SolrServerException, IOException {
        QueryResponse response = solrClient.query(query, METHOD.POST);

        List<Link> searchResults = new ArrayList<>();
        for(SolrVocabularyIndex result : response.getBeans(SolrVocabularyIndex.class)) {
            searchResults.add(
                new Link()
                    .setHref(result.getUri())
                    .setTitle(result.getTerm())
            );
        }
        return searchResults;
    }
}
