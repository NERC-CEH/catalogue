package uk.ac.ceh.gateway.catalogue.monitoring;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;
import uk.ac.ceh.gateway.catalogue.gemini.TimePeriod;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.indexing.solr.WellKnownText;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.model.Supplemental;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.RDF_TTL_VALUE;

@Data
@EqualsAndHashCode(callSuper = true)
@ConvertUsing({
    @Template(called="html/monitoring/network.ftlh", whenRequestedAs= MediaType.TEXT_HTML_VALUE),
    @Template(called="rdf/monitoring/network.ftl", whenRequestedAs=RDF_TTL_VALUE)
})
public class MonitoringNetwork extends AbstractMetadataDocument implements WellKnownText {
    private List<String> alternateTitles;
    private String objectives, operationalStatus;
    private List<ResponsibleParty> responsibleParties;
    private TimePeriod operatingPeriod;
    private List<Keyword> environmentalDomain, keywordsParameters;
    private List<Supplemental> linksData, linksOther;
    private BoundingBox boundingBox;

    @Override
    public List<String> getWKTs() {
        return Stream.ofNullable(boundingBox).map(BoundingBox::getWkt).toList();
    }
}

