package uk.ac.ceh.gateway.catalogue.infrastructure;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.indexing.solr.WellKnownText;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.OnlineLink;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.gemini.RelatedRecord;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
@ConvertUsing({
    @Template(called="html/infrastructure/infrastructurerecord.ftlh", whenRequestedAs= MediaType.TEXT_HTML_VALUE)
})
public class InfrastructureRecord extends AbstractMetadataDocument {

    private String capabilities, lifecycle, uniqueness, partners, locationText, access, userCosts, fundingSources, scienceArea, infrastructureScale ;

    private InfrastructureCategory infrastructureCategory;
    private List<Keyword> infrastructureChallenge;
    private List<ResponsibleParty> owners;
    private List<String> users;
    private List<BoundingBox> boundingBoxes;
    private List<OnlineResource> onlineResources;
    private List<RelatedRecord> relatedRecords;
}


