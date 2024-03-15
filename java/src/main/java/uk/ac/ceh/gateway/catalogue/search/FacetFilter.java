package uk.ac.ceh.gateway.catalogue.search;

import lombok.Value;
import org.springframework.util.StringUtils;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Value
public class FacetFilter {
    private static final String DELIMITER = "|";
    String field;
    String value;


    public FacetFilter(String filter) {
        filter = URLDecoder.decode(filter, StandardCharsets.UTF_8);
        if(StringUtils.countOccurrencesOf(filter, DELIMITER) == 1){
            String[] facetFilterParts = filter.split("\\" + DELIMITER);
            this.field = facetFilterParts[0];
            this.value = facetFilterParts[1];
        } else {
            throw new IllegalArgumentException(
                String.format(
                    "This is an invalid facet filter: %s. It should contain one argument delimiter of the type '|'",
                    filter
                )
            );
        }
    }

    public FacetFilter(String field, String value) {
        this.field = field;
        this.value = value;
    }

    public String asURIContent() {
        return URLEncoder.encode(
            new StringBuilder(field)
                .append(DELIMITER)
                .append(value)
                .toString(),
                StandardCharsets.UTF_8
        );
    }

    public String asSolrFilterQuery() {
        return new StringBuilder("{!term f=").append(field).append("}").append(value).toString();
    }

}
