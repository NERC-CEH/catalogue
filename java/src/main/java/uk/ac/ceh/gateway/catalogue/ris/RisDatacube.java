package uk.ac.ceh.gateway.catalogue.ris;

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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
@ConvertUsing({
  @Template(called="html/ris/ris_datacube.ftlh", whenRequestedAs= MediaType.TEXT_HTML_VALUE)
})
public class RisDatacube extends AbstractMetadataDocument {
  
  private String infrastructureClass, infrastructureCategory, purpose, capabilities, lifecycle,	uniqueness,	partners,	locationText,	access,	userCosts, fundingSources,	scienceArea ;

  private List<ResponsibleParty> owners;

  private List<String> users;

}


