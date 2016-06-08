package uk.ac.ceh.gateway.catalogue.search;

import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import lombok.Value;
import org.springframework.util.StringUtils;

@Value
public final class FacetFilter {
    private static final String DELIMITER = "|";
    private static final Escaper escaper = UrlEscapers.urlFormParameterEscaper();
    private final String field;
    private final String value;
    
    public FacetFilter(String filter) {
        if(StringUtils.countOccurrencesOf(filter, DELIMITER) == 1){
            String[] facetFilterParts = filter.split("\\" + DELIMITER);
            this.field = facetFilterParts[0];
            this.value = facetFilterParts[1];
        } else {
            throw new IllegalArgumentException(String.format("This is an invalid facet filter: %s. It should contain one argument delimiter of the type '|'", filter));
        }
    }
    
    public FacetFilter(String field, String value) {
        this.field = field;
        this.value = value;
    }
    
    public String asFormContent() {
        return new StringBuilder(field).append("|").append(value).toString();
    }
    
    public String asURIContent() {
        return new StringBuilder(field).append("|").append(escaper.escape(value)).toString();
    }
    
    public String asSolrFilterQuery() {
        return new StringBuilder("{!term f=").append(field).append("}").append(value).toString();
    }

}