package uk.ac.ceh.gateway.catalogue.vocabularies;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
@ToString
public class KeywordVocabularyController {

    private final KeywordVocabularySolrQueryService keywordService;

    public KeywordVocabularyController(KeywordVocabularySolrQueryService keywordService) {
        this.keywordService = keywordService;
        log.info("Creating");
    }

    @GetMapping(value = "vocabulary/keywords")
    public List<Keyword> getKeywords(
            @RequestParam(value = "query", defaultValue = "*") String query,
            @RequestParam(value = "vocab") Optional<List<String>> possibleVocabs

    ) throws SolrServerException {
        val vocabs = possibleVocabs.orElseGet(Collections::emptyList);
        return keywordService.query(query, vocabs);
    }
}
