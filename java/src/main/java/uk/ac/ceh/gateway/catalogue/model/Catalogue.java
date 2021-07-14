package uk.ac.ceh.gateway.catalogue.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import uk.ac.ceh.gateway.catalogue.vocabularies.KeywordVocabulary;

import java.util.List;

@Value
@Builder
public class Catalogue implements Comparable<Catalogue> {
    @NonNull private final String id, title, url;
    @Singular private final List<String> facetKeys;
    @Singular private final List<DocumentType> documentTypes;
    @Singular private final List<KeywordVocabulary> vocabularies;
    private final boolean fileUpload;

    @Override
    public int compareTo(Catalogue that) {
        return this.title.compareTo(that.title);
    }
    
    @Value
    @Builder
    public static class DocumentType {
        private final String type, title;
    }
}
