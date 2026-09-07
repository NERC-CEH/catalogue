package uk.ac.ceh.gateway.catalogue.vocabularies;

import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.request.SolrQuery;
import org.apache.solr.client.solrj.request.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.RemoteSolrException;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.params.CommonParams;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.apache.solr.client.solrj.SolrRequest.METHOD.POST;

@Slf4j
@ToString
@Service
@AllArgsConstructor
public class KeywordVocabularySolrQueryService {
    private static final String COLLECTION = "keywords";

    /**
     * Caches {@link #resolveExactLabel}. Every RDF render of a record asks once per
     * keyword that carries no URI of its own, so without this a record with twenty
     * free-text keywords costs twenty Solr round trips per render. The vocabularies
     * behind it are re-indexed weekly, so entries can be held for a long time.
     */
    public static final String EXACT_LABEL_CACHE = "keyword-exact-label";

    /**
     * How many candidates to pull back before filtering for an exact label match. The
     * query is an analysed match, so it also returns labels merely beginning with the
     * text; exact matches sort highest, and no plausible label has hundreds of
     * near-duplicates in the index.
     */
    private static final int EXACT_MATCH_CANDIDATES = 100;

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

        } catch (IOException | SolrServerException | RemoteSolrException ex) {
            throw new SolrServerException(ex);
        }
    }

    /**
     * Resolves free text to the one indexed vocabulary concept whose canonical label
     * it exactly matches.
     *
     * <p>Used when rendering RDF: a keyword with no {@code uri} of its own used to be
     * emitted as a bare {@code dcterms:subject} string even where a concept with that
     * exact label was already known, which put 42% of production's subject triples
     * beyond the reach of any vocabulary-aware query (dri-one #321).
     *
     * <p>The candidates come from this Solr core, which
     * {@link SparqlKeywordVocabulary} and {@link LocalKeywordVocabulary} fill from the
     * vocabulary providers themselves (GEMET, GeoNames, NVS, ...). It is deliberately
     * <em>not</em> a query over our own triplestore's {@code skos:prefLabel}s: those are
     * accumulated from record text, so a label there is whatever some depositor typed
     * (dri-one #320) and matching against it would launder one record's typo into every
     * other record's subject.
     *
     * <p>Exactness has to be decided here rather than by the query, because the core
     * indexes {@code label} as a lower-casing, edge-ngrammed text field — Solr can only
     * narrow the field to candidates, never confirm a match. Anything short of one
     * concept is left alone: zero matches and an ambiguous label (the same text in two
     * vocabularies) both fall back to the literal, since guessing between two concepts
     * would assert something the record does not say.
     *
     * @param label the keyword text as it appears on the record
     * @return the single concept that text names, or empty if none or more than one does
     */
    @Cacheable(cacheNames = EXACT_LABEL_CACHE, key = "#label", condition = "#label != null && !#label.isBlank()")
    public Optional<Keyword> resolveExactLabel(String label) throws SolrServerException {
        if (label == null || label.isBlank()) {
            return Optional.empty();
        }
        try {
            SolrQuery query = new SolrQuery();
            query.setQuery(ClientUtils.escapeQueryChars(label));
            query.setParam(CommonParams.DF, "label");
            query.setSort("score", ORDER.desc);
            query.setRows(EXACT_MATCH_CANDIDATES);
            log.debug("Resolving keyword label: {}", label);

            val exact = solrClient.query(COLLECTION, query, POST)
                .getBeans(Keyword.class)
                .stream()
                .filter(candidate -> label.equals(candidate.getLabel()))
                .filter(candidate -> candidate.getUrl() != null && !candidate.getUrl().isBlank())
                .toList();

            val distinctConcepts = exact.stream().map(Keyword::getUrl).distinct().count();
            if (distinctConcepts == 1) {
                return Optional.of(exact.getFirst());
            }
            if (distinctConcepts > 1) {
                log.debug("Not promoting ambiguous keyword label {}, {} concepts share it", label, distinctConcepts);
            }
            return Optional.empty();

        } catch (IOException | SolrServerException | RemoteSolrException ex) {
            throw new SolrServerException(ex);
        }
    }

    private String generateVocabQuery(List<String> vocabIds) {

        if(vocabIds.isEmpty())
            return "vocabId:__NO_MATCH__";

        StringBuilder toReturn = new StringBuilder("vocabId:(" + vocabIds.getFirst());
        if(vocabIds.size() > 1) {
            vocabIds
                    .stream()
                    .skip(1)
                    .forEach(v -> toReturn
                            .append(" OR ")
                            .append(v));
        }
        return toReturn.append(")").toString();
    }

}

