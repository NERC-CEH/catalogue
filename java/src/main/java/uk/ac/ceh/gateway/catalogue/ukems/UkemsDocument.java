package uk.ac.ceh.gateway.catalogue.ukems;

import java.util.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.ToString;
import org.springframework.http.MediaType;

import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.gemini.DatasetReferenceDate;
import uk.ac.ceh.gateway.catalogue.gemini.DistributionInfo;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.SpatialReferenceSystem;
import uk.ac.ceh.gateway.catalogue.gemini.SpatialResolution;
import uk.ac.ceh.gateway.catalogue.model.Supplemental;
import uk.ac.ceh.gateway.catalogue.gemini.TimePeriod;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;

@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
@Accessors(chain=true)
@ConvertUsing({
    @Template(called="html/ukems.ftlh", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
public class UkemsDocument extends AbstractMetadataDocument {
    private Keyword resourceType;
    private List<TimePeriod> temporalExtents;
    private DatasetReferenceDate datasetReferenceDate;
    private List<SpatialReferenceSystem> spatialReferenceSystems;
    private List<String> spatialRepresentationTypes;
    private List<SpatialResolution> spatialResolutions;
    private List<DistributionInfo> distributionFormats;
    // identifier corresponding to ERDDAP's datasetID
    private String ident;
    private EmissionComponent emissionComponent;
    // supplemental documentation including QA/QC log
    private List<Supplemental> supplemental;
    private String units;
    private String provenance;
    // degree to which dataset conforms to DUKEMS QA/QC guidance
    private Conformance conformance;
    // comment explaining reason for PARTIALLY_CONFORMS or DOES_NOT_CONFORM
    private String conformanceComment;
    // text on what QA/QC checks have been undertaken
    private String productChecks;
    private Reliability reliabilityRating;


    @ToString
    public enum Conformance {
        CONFORMS,
        PARTIALLY_CONFORMS,
        DOES_NOT_CONFORM;
    }

    @ToString
    public enum Reliability {
        LEVEL1,
        LEVEL2,
        LEVEL3,
        LEVEL4,
        LEVEL5;
    }

    @ToString
    public enum EmissionComponent {
        EMISSION {
            @Override
            public String format() {
                return "emission";
            }
        },
        ACTIVITY {
            @Override
            public String format() {
                return "activity";
            }
        },
        EMISSION_FACTOR {
            @Override
            public String format() {
                return "emission factor";
            }
        };
        public abstract String format();
    }

    @Override
    public String getType() {
        return Optional.ofNullable(resourceType)
            .map(Keyword::getValue)
            .orElse("");
    }
}
