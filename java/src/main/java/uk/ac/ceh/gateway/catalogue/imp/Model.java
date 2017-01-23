package uk.ac.ceh.gateway.catalogue.imp;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.model.Link;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper=true)
@ConvertUsing({
    @Template(called="html/imp-model.html.tpl", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
public class Model extends ImpDocument {
    private String version, license, operatingRequirements, applicationType,
            smallestAndLargestApplication, geographicalRestrictions, temporalResolution,
            keyOutputs, calibrationRequired, modelStructure, dataInput, releaseDate,
            userInterface, supportAvailable, applicationScale, spatialResolution,
            primaryPurpose, keyInputVariables, modelParameterisation, 
            inputDataAvailableOnCatalogue;
    private List<String> keyReferences, inputData, outputData;
    private Link documentation;
    private Boolean developerTesting, internalPeerReview, externalPeerReview,
        useOfVersionControl,internalModelAudit, qualityAssuranceGuidelinesAndChecklists,
        externalModelAudit, governance, transparency, periodicReview;
}
