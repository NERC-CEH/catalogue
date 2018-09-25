package uk.ac.ceh.gateway.catalogue.modelceh;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.Relationship;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
@ConvertUsing({
    @Template(called="html/ceh-model-application.ftl", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
public class CehModelApplication extends AbstractMetadataDocument {
    private String
        projectCode,
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
    
    private List<CehModel.Reference> references;
    
    private List<ModelInfo> modelInfos;
    
    private List<DataInfo>
        inputData,
        outputData;
    
    @JsonIgnore
    @Override
    public Set<Relationship> getRelationships() {
        return Optional.ofNullable(modelInfos)
            .orElse(Collections.emptyList())
            .stream()
            .filter(mi -> mi.getId() != null && !mi.getId().isEmpty())
            .map((mi) -> new Relationship("http://purl.org/dc/terms/references", mi.id))
            .collect(Collectors.toSet());
    }
    
    @JsonIgnore
    @Override
    public AbstractMetadataDocument setRelationships(Set<Relationship> relationships) {
        return this; // None added, relationships come from modelInfos
    }
    
    @Data
    public static class ModelInfo {
        private String
            name,
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
