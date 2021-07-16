package uk.ac.ceh.gateway.catalogue.vocabularies;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@Slf4j
@ToString
public class KeywordVocabularyController {

    private final KeywordVocabularySolrQueryService keywordService;

    public KeywordVocabularyController(KeywordVocabularySolrQueryService keywordService) {
        this.keywordService = keywordService;
    }

    @GetMapping(value = "vocabulary/keywords")
    @ResponseBody
    public List<Keyword> getKeywords(
            @RequestParam(value = "query", defaultValue = "*") String query,
            @RequestParam(value = "vocab") List<String> vocab

    ) throws SolrServerException {
        return keywordService.query(query, vocab);
    }
}
