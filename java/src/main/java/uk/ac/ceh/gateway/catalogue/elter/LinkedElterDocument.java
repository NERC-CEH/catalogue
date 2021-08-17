package uk.ac.ceh.gateway.catalogue.elter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import lombok.NonNull;

@Value
public class LinkedElterDocument {
    private final @NonNull String linkedDocumentUri;
    private final String linkedDocumentType;

    @JsonCreator
    public LinkedElterDocument(
        @JsonProperty("linkedDocumentUri") String linkedDocumentUri,
        @JsonProperty("linkedDocumentType") String linkedDocumentType) {
        this.linkedDocumentUri = linkedDocumentUri;
        this.linkedDocumentType = linkedDocumentType;
    }
}
