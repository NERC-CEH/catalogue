package uk.ac.ceh.gateway.catalogue.modelceh;

import java.util.List;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;


import org.springframework.http.MediaType;

import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.indexing.WellKnownText;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.OnlineLink;
import uk.ac.ceh.gateway.catalogue.model.Supplemental;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.model.DataTypeSchema;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;

@Data
@EqualsAndHashCode(callSuper = true)
@ConvertUsing({
    @Template(called="html/ceh-model.ftl", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
public class CehModel extends AbstractMetadataDocument implements WellKnownText {
    private String 
        primaryPurpose,
        licenseType,
        modelType,
        currentModelVersion,
        modelCalibration,
        spatialDomain,
        spatialResolution,
        temporalResolutionMin,
        temporalResolutionMax,
        language,
        compiler,
        operatingSystem,
        systemMemory,
        releaseDate,
        seniorResponsibleOfficer, // replaced by responsibleParties - when data is updated this can be deleted
        seniorResponsibleOfficerEmail,  // replaced by responsibleParties - when data is updated this can be deleted
        website,  //replaced by onlineResources - when data is updated this can be deleted 
        codeRepositoryUrl, //replaced by onlineResources - when data is updated this can be deleted
        documentation;  //replaced by onlineResources - when data is updated this can be deleted
    
    private List<BoundingBox> boundingBoxes;

    private List<DataTypeSchema>
        inputVariables,
        outputVariables;

     //when data is updated these can be deleted
    private List<String>
        organisations, //replaced by responsibleParties
        keyInputVariables, //replaced by inputVariables
        keyOutputVariables; //replaced by outputVariables
    
    private List<OnlineLink> onlineResources; 

    private List<Reference> references; //replaced by supplemental - when data is updated this can be deleted
    private List<Supplemental> supplemental;
    
    private QualityAssurance
        developerTesting,
        internalPeerReview,
        externalPeerReview,
        internalModelAudit,
        externalModelAudit,
        qaGuidelinesAndChecklists,
        governance,
        transparency,
        periodicReview;
    
    private List<VersionHistory> versionHistories;
    private List<ResponsibleParty> responsibleParties;
    private List<ProjectUsage> projectUsages;
    
    @Data
    public static class Reference {
        private String
            citation,
            doi,
            nora;
    }
    
    @Data
    public static class QualityAssurance {
        private String
            done,
            modelVersion,
            owner,
            note,
            date;
    }
    
    @Data
    public static class VersionHistory {
        private String
            version,
            note,
            date;
    }
    
    @Data
    public static class ProjectUsage {
        private String
            project,
            version,
            date;
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