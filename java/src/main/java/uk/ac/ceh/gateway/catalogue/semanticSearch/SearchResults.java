package uk.ac.ceh.gateway.catalogue.semanticSearch;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 *
 * @author Christopher Johnson
 */
@Data
@Accessors(chain = true)
public class SearchResults<T> {
    private Header header;
    private List<T> results;

    @Data
    @Accessors(chain = true)
    public static class Header {
        private long numFound, start, rows;
    }

    @Data
    @Accessors(chain = true)
    public static class DocumentSearchResult {
        private UUID id;
        private String title, type, highlightedDescription, highlightedTitle;
        private URI href;
        private List<String> highlightedKeywords, highlightedKeywordLinks, highlightedOrganisations, highlightedAlternativeTitles;
    }

    @Data
    @Accessors(chain = true)
    public static class VocabularySearchResult {
        private String uri, term, vocab, highlightedTerm;
    }

    @Data
    @Accessors(chain = true)
    public static class LeadOrganisationFacetedSearchResult {
        private String organisationName;
        private long documentCount;
    }
}
