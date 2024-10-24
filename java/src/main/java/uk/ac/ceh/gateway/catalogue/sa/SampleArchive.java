package uk.ac.ceh.gateway.catalogue.sa;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;
import uk.ac.ceh.gateway.catalogue.gemini.Funding;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.TimePeriod;
import uk.ac.ceh.gateway.catalogue.indexing.solr.WellKnownText;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
@ConvertUsing({
        @Template(called="html/sample_archive/sample_archive.ftlh", whenRequestedAs= MediaType.TEXT_HTML_VALUE)
})
public class SampleArchive extends AbstractMetadataDocument implements WellKnownText {
    private String lineage, availability, accessRestrictions, storage, healthSafety, website;
    private List<Keyword>  taxa, tissues, specimenTypes, physicalStates, keywords;
    private TimePeriod temporalExtent;
    private List<BoundingBox> boundingBoxes;
    private List<ResponsibleParty> archiveLocations, archiveContacts, metadataContacts;
    private List<OnlineResource> onlineResources;
    private List<Funding> funding;

    @Override
    public List<String> getWKTs() {
        return Optional.ofNullable(boundingBoxes)
                .orElse(Collections.emptyList())
                .stream()
                .map(BoundingBox::getWkt)
                .collect(Collectors.toList());
    }
}
