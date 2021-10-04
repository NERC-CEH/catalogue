package uk.ac.ceh.gateway.catalogue.ukems;

import java.util.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.ToString;
import org.springframework.http.MediaType;

import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.gemini.DatasetReferenceDate;
import uk.ac.ceh.gateway.catalogue.gemini.DistributionInfo;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.TimePeriod;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;

@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
@Accessors(chain=true)
@ConvertUsing({
    @Template(called="html/ukems/ukems-document.ftlh", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
public class UkemsDocument extends AbstractMetadataDocument {
    private Keyword resourceType;
    private List<TimePeriod> temporalExtents;
    private DatasetReferenceDate datasetReferenceDate;
    private List<String> spatialRepresentationTypes;
    private List<DistributionInfo> distributionFormats;
    private String ident;
    private EmissionComponent emissionComponent;

    @ToString
    public enum EmissionComponent {
        EMISSION, ACTIVITY, EMISSION_FACTOR
    }

    @Override
    public String getType() {
        return Optional.ofNullable(resourceType)
            .map(Keyword::getValue)
            .orElse("");
    }
}
