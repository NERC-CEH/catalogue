package uk.ac.ceh.gateway.catalogue.monitoring;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.geometry.BoundingBox;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.TimePeriod;
import uk.ac.ceh.gateway.catalogue.indexing.solr.WellKnownText;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.model.Supplemental;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.RDF_TTL_VALUE;

@Data
@EqualsAndHashCode(callSuper = true)
@ConvertUsing({
    @Template(called="html/monitoring/activity.ftlh", whenRequestedAs=MediaType.TEXT_HTML_VALUE),
    @Template(called="rdf/monitoring/activity.ftl", whenRequestedAs=RDF_TTL_VALUE)
})
public class MonitoringActivity extends AbstractMetadataDocument implements WellKnownText {
    private List<String> alternateTitles;
    private String objectives, operationalStatus;
    private BoundingBox boundingBox;
    private List<ResponsibleParty> pointsOfContact;
    private List<TimePeriod> operatingPeriod;
    private List<Keyword> environmentalDomain, purposeOfCollection, keywordsParameters;
    private List<Supplemental> linksData, linksOther;
    private List<Link> relUseNetworkOrFacility;
    private List<Link> relSetupForProgramme;

    @Override
    public List<String> getWKTs() {
        List<String> toReturn = new ArrayList<>();
        if (boundingBox != null) {
            toReturn.add(boundingBox.getWkt());
        }
        return toReturn;
    }

    public void populateFromJenaService(JenaLookupService jenaService) {
        final String uri = this.getUri();
        this.setRelUseNetworkOrFacility(jenaService.relationships(uri, "https://digital.ceh.ac.uk/ontology/doo/uses"));
        this.setRelSetupForProgramme(jenaService.inverseRelationships(uri, "https://digital.ceh.ac.uk/ontology/doo/triggers"));
    }


    public List<Link> getRelUseNetworkOrFacility() {
        return Optional.ofNullable(relUseNetworkOrFacility)
            .orElseGet(Collections::emptyList);
    }

    public List<Link> getRelSetupForProgramme() {
        return Optional.ofNullable(relSetupForProgramme)
            .orElseGet(Collections::emptyList);
    }
}
