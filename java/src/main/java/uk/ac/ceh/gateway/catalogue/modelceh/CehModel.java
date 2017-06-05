package uk.ac.ceh.gateway.catalogue.modelceh;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @Template(called="html/ceh-model.html.tpl", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
public class CehModel extends AbstractMetadataDocument {
    private String 
        primaryPurpose,
        website,
        seniorResponsibleOfficer, 
        seniorResponsibleOfficerEmail,
        licenseType,
        codeRepositoryUrl,
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
        documentation,
        releaseDate;
    
    private List<String>
        organisations,
        keyInputVariables,
        keyOutputVariables;
    
    private List<Keyword> keywords;
    
    private List<Reference> references;
    
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
        
    private List<ProjectUsage> projectUsages;   

    @Override
    @JsonIgnore
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
}
