package uk.ac.ceh.gateway.catalogue.templateHelpers;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.vocabularies.KeywordVocabularySolrQueryService;

/**
 * Decides which RDF node a keyword is, for {@code templates/rdf/_macros.ftl}.
 *
 * <p>A keyword arrives on a record as {@code {value, uri}}, and nothing ever looked the
 * {@code value} up: with a {@code uri} the templates emitted a concept IRI, and without
 * one they emitted the text as a bare {@code dcterms:subject} literal. A production audit
 * (dri-one #321) found 42% of {@code dcterms:subject} triples storing a keyword as plain
 * text when a concept with that exact label was already known, so the same subject was
 * both a queryable IRI and an opaque string depending on which record you looked at.
 *
 * <p>Precedence:
 * <ol>
 *   <li>the record's own {@code uri}, canonicalised by {@link UriNormaliser}</li>
 *   <li>the concept whose canonical label the keyword's text exactly names, if exactly
 *       one vocabulary concept does — see
 *       {@link KeywordVocabularySolrQueryService#resolveExactLabel}</li>
 *   <li>nothing, which the templates render as the literal they always did</li>
 * </ol>
 *
 * <p>The lookup goes to the vocabulary index, which is built from the vocabulary
 * providers themselves, and never to our own triplestore's {@code skos:prefLabel}s,
 * which are accumulated from record text (dri-one #320).
 *
 * <p>Promotion changes only which node the keyword <em>is</em>. It asserts nothing about
 * that node beyond {@code a skos:Concept}, exactly as a keyword carrying its own URI
 * does, so a promoted keyword cannot write record text onto shared vocabulary data.
 */
@Slf4j
@Service
@ToString
@RequiredArgsConstructor
public class KeywordUri {

    private final UriNormaliser uriNormaliser;
    private final KeywordVocabularySolrQueryService vocabulary;

    /**
     * @param keyword a keyword as supplied by a metadata record
     * @return the canonical concept IRI to use as its node, or an empty string if the
     *         keyword can only be a literal
     */
    public String identify(Keyword keyword) {
        val ownUri = uriNormaliser.normalise(keyword.getUri());
        return ownUri.isEmpty() ? resolve(keyword.getValue()) : ownUri;
    }

    private String resolve(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        try {
            return vocabulary.resolveExactLabel(value.trim())
                .map(concept -> uriNormaliser.normalise(concept.getUrl()))
                .orElse("");
        } catch (Exception ex) {
            // A vocabulary that is unreachable, or a keywords core that is mid-reindex,
            // must cost this keyword its promotion and nothing more — a record still has
            // to render. RemoteSolrException is a sibling of SolrServerException rather
            // than a subclass, so the checked type alone would not cover Solr's own 4xx.
            log.warn("Could not resolve keyword '{}' against the vocabularies: {}", value, ex.getMessage());
            return "";
        }
    }
}
