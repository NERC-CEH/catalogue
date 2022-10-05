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
import uk.ac.ceh.gateway.catalogue.indexing.solr.GeoJson;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.OnlineLink;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.model.DataTypeSchema;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;

@Data
@EqualsAndHashCode(callSuper = true)
@ConvertUsing({
    @Template(called="html/ceh-model.ftlh", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
public class CehModel extends AbstractMetadataDocument implements GeoJson {
    private String
        primaryPurpose,
        seniorResponsibleOfficer,
        seniorResponsibleOfficerEmail,
        licenseType,
        website,
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

    private List<BoundingBox> boundingBoxes;

    private List<DataTypeSchema>
        inputVariables,
        outputVariables;

    private List<String>
        organisations,
        keyInputVariables,
        keyOutputVariables;

    private List<OnlineLink> onlineResources;

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
    public List<String> getGeoJson() {
        return Optional.ofNullable(boundingBoxes)
            .orElse(Collections.emptyList())
            .stream()
            .map(BoundingBox::getGeoJson)
            .collect(Collectors.toList());
    }
}
