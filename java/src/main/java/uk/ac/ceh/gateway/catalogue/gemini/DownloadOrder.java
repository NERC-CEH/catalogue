package uk.ac.ceh.gateway.catalogue.gemini;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import lombok.Builder;
import static com.google.common.base.Strings.nullToEmpty;

@Value
public class DownloadOrder {
    private final String orderUrl, supportingDocumentsUrl;
    
    @Builder
    @JsonCreator
    private DownloadOrder(
        @JsonProperty("orderUrl") String orderUrl,
        @JsonProperty("supportingDocumentsUrl") String supportingDocumentsUrl
    ) {
        this.orderUrl = nullToEmpty(orderUrl);
        this.supportingDocumentsUrl = nullToEmpty(supportingDocumentsUrl);
    }
}