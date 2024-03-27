package uk.ac.ceh.gateway.catalogue.monitoring;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.val;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.gemini.Geometry;
import uk.ac.ceh.gateway.catalogue.gemini.TimePeriod;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.indexing.solr.WellKnownText;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;

import java.util.ArrayList;
import java.util.List;

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.RDF_TTL_VALUE;

@Data
@EqualsAndHashCode(callSuper = true)
@ConvertUsing({
    @Template(called="html/monitoring/facility.ftlh", whenRequestedAs= MediaType.TEXT_HTML_VALUE),
    @Template(called="rdf/monitoring/facility.ftl", whenRequestedAs=RDF_TTL_VALUE)
})
public class MonitoringFacility extends AbstractMetadataDocument implements WellKnownText {
    private List<String> alternateTitles;
    private Keyword facilityType;
    private Geometry geometry;
    private boolean geometryRepresentative, mobile;
    private List<ResponsibleParty> responsibleParties;
    private List<TimePeriod> operatingPeriod;
    private List<Keyword> environmentalDomain, keywordsParameters;

    @Override
    public @NonNull List<String> getWKTs() {
        List<String> toReturn = new ArrayList<>();
        if(geometry != null) {
            val possibleWkt = geometry.getWkt();
            possibleWkt.ifPresent(toReturn::add);
        }
        return toReturn;
    }
}
