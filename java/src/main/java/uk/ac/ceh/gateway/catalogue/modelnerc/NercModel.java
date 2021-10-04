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
    @Template(called="html/nercmodels/model.ftlh", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
public class NercModel extends AbstractMetadataDocument implements WellKnownText {
    private String
        purpose,
        licenseType,
        modelType,
        version,
        modelCalibration,
        spatialDomain,
        //spatialResolution,
        //temporalResolutionMin,
        //temporalResolutionMax,
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
    private List<ModelResolution> resolution;

    @Data
    public static class QA {
        private String
            category,
            notes,
            date;
    }
    @Data
    public static class ModelResolution {
        private String
            category,
            min,
            max;
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
