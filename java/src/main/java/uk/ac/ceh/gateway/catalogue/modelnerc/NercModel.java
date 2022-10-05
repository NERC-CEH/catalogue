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
import uk.ac.ceh.gateway.catalogue.indexing.solr.GeoJson;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.OnlineLink;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.model.DataTypeSchema;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;
import uk.ac.ceh.gateway.catalogue.gemini.Supplemental;
import uk.ac.ceh.gateway.catalogue.gemini.Funding;

@Data
@EqualsAndHashCode(callSuper = true)
@ConvertUsing({
    @Template(called="html/nercmodels/model.ftlh", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
public class NercModel extends AbstractMetadataDocument implements GeoJson {
    private String
        purpose,
        licenseType,
        modelType,
        version,
        calibration,
        spatialDomain,
        language,
        compiler,
        operatingSystem,
        systemMemory,
        documentation,
        releaseDate,
        configuration;

    private List<BoundingBox> boundingBoxes;

    private List<Funding> funding;

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
    public List<String> getGeoJson() {
        return Optional.ofNullable(boundingBoxes)
            .orElse(Collections.emptyList())
            .stream()
            .map(BoundingBox::getGeoJson)
            .collect(Collectors.toList());
    }
}
