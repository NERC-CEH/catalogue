package uk.ac.ceh.gateway.catalogue.imp;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;

@Data
@EqualsAndHashCode(callSuper=true)
@ConvertUsing({
    @Template(called="html/imp-model.html.tpl", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
public class Model extends ImpDocument {
    private String version, contact, license, operatingRequirements, applicationType,
            smallestAndLargestApplication, geographicalRestrictions, temporalResolution,
            keyOutputs, calibrationRequired, modelStructure, dataInput;
    private List<String> keyReferences, inputData, outputData;
    private Link documentation;
}
