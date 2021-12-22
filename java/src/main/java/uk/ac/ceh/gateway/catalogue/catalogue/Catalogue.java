package uk.ac.ceh.gateway.catalogue.catalogue;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import uk.ac.ceh.gateway.catalogue.vocabularies.KeywordVocabulary;

import java.util.List;

@Value
@Builder
public class Catalogue implements Comparable<Catalogue> {
    @NonNull String id, title, url, contactUrl, logo;
    @Singular List<String> facetKeys;
    @Singular List<DocumentType> documentTypes;
    List<KeywordVocabulary> vocabularies;
    boolean fileUpload;

    @Override
    public int compareTo(Catalogue that) {
        return this.title.compareTo(that.title);
    }

    @Value
    @Builder
    public static class DocumentType {
        String type, title;
    }
}
