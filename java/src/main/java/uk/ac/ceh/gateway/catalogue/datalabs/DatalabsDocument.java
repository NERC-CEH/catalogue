                            package uk.ac.ceh.gateway.catalogue.datalabs;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.indexing.solr.WellKnownText;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.gemini.TimePeriod;
import uk.ac.ceh.gateway.catalogue.gemini.InspireTheme;
import uk.ac.ceh.gateway.catalogue.gemini.DatasetReferenceDate;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceConstraint;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
@Accessors(chain=true)
@ConvertUsing({
    @Template(called="html/datalabs/datalabs-document.ftlh", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
public class DatalabsDocument extends AbstractMetadataDocument {
    private String assetType, version, masterUrl, primaryLanguage, secondaryLanguage;
    private List<String> packages, inputs, outputs;
    private List<ResponsibleParty> owners;
    private List<ResourceConstraint> useConstraints;
    private DatasetReferenceDate referenceDate;
    private List<InspireTheme> inspireThemes;
    private List<BoundingBox> boundingBoxes;
    private List<TimePeriod> temporalExtents;

}
