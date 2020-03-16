package uk.ac.ceh.gateway.catalogue.modelceh;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.indexing.WellKnownText;
import uk.ac.ceh.gateway.catalogue.model.Relationship;
import uk.ac.ceh.gateway.catalogue.model.Supplemental;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.gemini.Funding;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;
import uk.ac.ceh.gateway.catalogue.gemini.TimePeriod;
import uk.ac.ceh.gateway.catalogue.model.DataTypeSchema;


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

public class CehModelApplication extends AbstractMetadataDocument implements WellKnownText {
    private String
        projectCode,
        projectObjectives,
        projectCompletionDate,
        projectWebsite, //replaced by onlineResources - when data is updated this can be deleted
        funderDetails,  //replaced by Funding - when data is updated this can be deleted
        contactName, //replaced by responsibleParties - when data is updated this can be deleted
        contactEmail, //replaced by responsibleParties - when data is updated this can be deleted
        spatialResolution,
        temporalResolution,
        multipleModelsUsed,
        multipleModelLinkages,
        sensitivityAnalysis,
        uncertaintyAnalysis,
        validation;

    private List<ResponsibleParty> responsibleParties;
    private List<OnlineResource> onlineResources;    
    private List<BoundingBox> boundingBoxes;    
    private List<Funding> funding;
    private List<ModelInfo> modelInfos;
    private List<TimePeriod> temporalExtents;
    private List<DataTypeSchema>
        inputVariables,
        outputVariables;
    private List<DataInfo> //replaced by inputVariables/outputVariables - when data is updated could this be deleted ???
        inputData,
        outputData;
    private List<CehModel.Reference> references; //replaced by supplemental - when data is updated this can be deleted
    private List<Supplemental> supplemental;
        
        
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
            spatialExtentOfApplication, // may be removed
            availableSpatialData, // may be removed
            spatialResolutionOfApplication, // may be removed
            temporalExtentOfApplicationStartDate, // may be removed
            temporalExtentOfApplicationEndDate, // may be removed
            temporalResolutionOfApplication, // may be removed
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
    
    @Override
    public List<String> getWKTs() {
        return Optional.ofNullable(boundingBoxes)
            .orElse(Collections.emptyList())
            .stream()
            .map(BoundingBox::getWkt)
            .collect(Collectors.toList());
    }

}
