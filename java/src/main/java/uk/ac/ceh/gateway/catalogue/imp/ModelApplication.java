package uk.ac.ceh.gateway.catalogue.imp;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper=true)
@ConvertUsing({
    @Template(called="html/imp-application.ftlh", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
public class ModelApplication extends ImpDocument {
    private String date, studySite, studyScale, objective, funderDetails,
        relevanceToCaMMP, multipleModelsUsed, multipleModelLinkages,
        sensitivity, uncertainty, validation, modelEasyToUse, userManualUseful,
        dataObtainable, modelUnderstandable;
    private List<String> inputData;
    private List<Model> models;

    @Data
    public static class Model {
        private String name, version, primaryPurpose, applicationScale,
            keyOutputVariables, keyInputVariables, temporalResolution,
            spatialResolution, inputDataAvailableInDataCatalogue;
    }
}
