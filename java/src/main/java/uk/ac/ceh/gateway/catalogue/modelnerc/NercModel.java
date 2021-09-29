package uk.ac.ceh.gateway.catalogue.modelnerc;

import java.util.List;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;

import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.indexing.solr.WellKnownText;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.OnlineLink;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.model.DataTypeSchema;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;
import uk.ac.ceh.gateway.catalogue.gemini.Supplemental;

@Data
@EqualsAndHashCode(callSuper = true)
@ConvertUsing({
    @Template(called="html/nerc-model.ftlh", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
public class NercModel extends AbstractMetadataDocument implements WellKnownText {
    private String
        purpose,
        licenseType,
        modelType,
        version,
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
    private List<Supplemental> references;
    private List<ResponsibleParty> responsibleParties;
    private List<QA> qa;

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
    public static class QA {
        private String
            type,
            notes,
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
