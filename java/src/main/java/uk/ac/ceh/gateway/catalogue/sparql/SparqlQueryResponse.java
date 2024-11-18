package uk.ac.ceh.gateway.catalogue.sparql;

import lombok.Data;
import java.util.List;

@Data
public class SparqlQueryResponse {
    private Results results;//just results needed as we don't use head

    @Data
    public static class Results {
        private List<Binding> bindings;
    }

    @Data
    public static class Binding {
        private Uri uri;
    }

    @Data
    public static class Uri {
        private String value;  // Only the value field, no need for type
    }
}
