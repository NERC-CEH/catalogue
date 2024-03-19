package uk.ac.ceh.gateway.catalogue.monitoring;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.TimePeriod;
import uk.ac.ceh.gateway.catalogue.indexing.solr.WellKnownText;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;

import java.util.ArrayList;
import java.util.List;

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.RDF_TTL_VALUE;

@Data
@EqualsAndHashCode(callSuper = true)
@ConvertUsing({
    @Template(called="html/monitoring/monitoringActivity.ftlh", whenRequestedAs=MediaType.TEXT_HTML_VALUE),
    @Template(called="rdf/monitoring/activity.ftl", whenRequestedAs=RDF_TTL_VALUE)
})
public class MonitoringActivity extends AbstractMetadataDocument implements WellKnownText {
    private TimePeriod temporalExtent;
    private List<Keyword> parametersMeasured;
    private BoundingBox boundingBox;

    @Override
    public List<String> getWKTs() {
        List<String> toReturn = new ArrayList<>();
        if (boundingBox != null) {
            toReturn.add(boundingBox.getWkt());
        }
        return toReturn;
    }
}
