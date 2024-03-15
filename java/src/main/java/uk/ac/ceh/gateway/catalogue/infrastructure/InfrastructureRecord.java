package uk.ac.ceh.gateway.catalogue.infrastructure;

import lombok.NonNull;
import lombok.val;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.OnlineResource;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.indexing.solr.WellKnownText;
import uk.ac.ceh.gateway.catalogue.gemini.Geometry;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
@ConvertUsing({
    @Template(called="html/infrastructure/infrastructurerecord.ftlh", whenRequestedAs= MediaType.TEXT_HTML_VALUE)
})
public class InfrastructureRecord extends AbstractMetadataDocument implements WellKnownText {

    private String capabilities, lifecycle, uniqueness, partners, locationText, access, userCosts, fundingSources, scienceArea, infrastructureScale ;
    private Geometry geometry;
    private InfrastructureCategory infrastructureCategory;
    private List<Keyword> infrastructureChallenge;
    private List<ResponsibleParty> owners;
    private List<String> users;
    private List<OnlineResource> onlineResources;

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


