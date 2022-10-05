package uk.ac.ceh.gateway.catalogue.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.gemini.TimePeriod;
import uk.ac.ceh.gateway.catalogue.gemini.InspireTheme;
import uk.ac.ceh.gateway.catalogue.gemini.DatasetReferenceDate;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceConstraint;
import uk.ac.ceh.gateway.catalogue.gemini.RelatedRecord;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
@Accessors(chain=true)
@ConvertUsing({
    @Template(called="html/code/code-document.ftlh", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
public class CodeDocument extends AbstractMetadataDocument {
    private String assetType, version, masterUrl, primaryLanguage, secondaryLanguage;
    private List<String> packages, inputs, outputs;
    private List<ResponsibleParty> responsibleParties;
    private List<ResourceConstraint> useConstraints;
    private DatasetReferenceDate referenceDate;
    private List<InspireTheme> inspireThemes;
    private List<BoundingBox> boundingBoxes;
    private List<TimePeriod> temporalExtents;
    private List<Review> review;
    private List<RelatedRecord> relatedRecords;


    @Data
    public static class Review {
        private String reviewDate, reviewProcess;
    }


}
