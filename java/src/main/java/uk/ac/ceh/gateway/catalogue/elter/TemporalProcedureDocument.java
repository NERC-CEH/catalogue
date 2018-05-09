package uk.ac.ceh.gateway.catalogue.elter;

import java.util.Set;
import org.springframework.http.MediaType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
@ConvertUsing({
    @Template(called="html/elter/temporal-procedure.ftl", whenRequestedAs = MediaType.TEXT_HTML_VALUE)
})
public class TemporalProcedureDocument extends AbstractMetadataDocument {
    private Set<String> replacedBy;
    private String replacedByName;
    private String commitCode;
    private String interpolationType;
    private long intendedObservationSpacing;
    private long maximumGap;
    private long anchorTime;
    private String sampleMedium;
    private String loggerSensorName;
    private String historicSensorName;
    private String loggerName;
    private String historicFeatureName;
}