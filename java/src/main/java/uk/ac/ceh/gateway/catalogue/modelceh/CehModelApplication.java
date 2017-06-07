package uk.ac.ceh.gateway.catalogue.modelceh;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;

@Data
@EqualsAndHashCode(callSuper = true)
@ConvertUsing({
    @Template(called="html/ceh-model-application.html.tpl", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
public class CehModelApplication extends AbstractMetadataDocument {
    private String
        projectObjectives,
        projectCompletionDate,
        projectWebsite,
        funderDetails,
        contactName,
        contactEmail,
        multipleModelsUsed,
        multipleModelLinkages,
        sensitivityAnalysis,
        uncertaintyAnalysis,
        validation;
    
    private List<Keyword> keywords;
    
    private List<CehModel.Reference> references;
    
    private List<ModelInfo> modelInfos;
    
    private List<DataInfo>
        inputData,
        outputData;

    @Override
    public List<Keyword> getAllKeywords() {
        return keywords;
    }

    @Override
    public MetadataDocument addAdditionalKeywords(List<Keyword> additionalKeywords) {
        keywords = Optional.ofNullable(keywords)
            .orElse(new ArrayList<>());
        
        keywords.addAll(additionalKeywords);
        return this;
    }
    
    @Data
    public static class ModelInfo {
        private String
            id,
            version,
            rationale,
            spatialExtentOfApplication,
            availableSpatialData,
            spatialResolutionOfApplication,
            temporalExtentOfApplicationStartDate,
            temporalExtentOfApplicationEndDate,
            temporalResolutionOfApplication,
            calibrationConditions;
    }
    
    @Data
    public static class DataInfo {
        private String
            variableName,
            units,
            fileFormat,
            url;
    }

}
