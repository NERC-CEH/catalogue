package uk.ac.ceh.gateway.catalogue.gemini.elements;

import lombok.Value;
import lombok.experimental.Builder;
import static com.google.common.base.Strings.nullToEmpty;

@Value
public class DownloadOrder implements Empty {
    private static final String oglUrl = "http://eidchub.ceh.ac.uk/administration-folder/tools/ceh-standard-licence-texts/ceh-open-government-licence/plain";
    private final String orderUrl, supportingDocumentsUrl, licenseUrl;
    
    @Builder
    private DownloadOrder(String orderUrl, String supportingDocumentsUrl, String licenseUrl) {
        this.orderUrl = nullToEmpty(orderUrl);
        this.supportingDocumentsUrl = nullToEmpty(supportingDocumentsUrl);
        this.licenseUrl = nullToEmpty(licenseUrl);
    }
    
    public boolean isOgl() {
        return oglUrl.equals(licenseUrl);
    }
    
    @Override
    public boolean isEmpty() {
        return orderUrl.isEmpty() && supportingDocumentsUrl.isEmpty() && licenseUrl.isEmpty();
    }
}