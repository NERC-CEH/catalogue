package uk.ac.ceh.gateway.catalogue.esb;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;
import uk.ac.ceh.gateway.catalogue.gemini.DescriptiveKeywords;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.TimePeriod;
import uk.ac.ceh.gateway.catalogue.indexing.WellKnownText;
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
    @Template(called="html/specimen_bank/specimen_bank.ftl", whenRequestedAs= MediaType.TEXT_HTML_VALUE)
})
public class SpecimenBank extends AbstractMetadataDocument implements WellKnownText {
  private String lineage, availability, accessRestrictions, storage, healthSafety, website;
  private List<DescriptiveKeywords> descriptiveKeywords;
  private TimePeriod temporalExtent;
  private List<BoundingBox> boundingBoxes;
  private List<ResponsibleParty> archiveLocations, archiveContacts, metadataContacts;
  private List<OnlineLink> onlineResources;

  @Override
  public List<String> getWKTs() {
    return Optional.ofNullable(boundingBoxes)
        .orElse(Collections.emptyList())
        .stream()
        .map(BoundingBox::getWkt)
        .collect(Collectors.toList());
  }

  @Override
  public List<Keyword> getAllKeywords() {
      return Optional.ofNullable(descriptiveKeywords)
          .orElse(Collections.emptyList())
          .stream()
          .flatMap(dk -> dk.getKeywords().stream())
          .collect(Collectors.toList());
  }
  
  public List<Keyword> getTaxa() {
      return Optional.ofNullable(descriptiveKeywords)
          .orElse(Collections.emptyList())
          .stream()
          .filter((dk) -> dk.getType().equalsIgnoreCase("taxon"))
          .flatMap(dk -> dk.getKeywords().stream())
          .collect(Collectors.toList());
  }

  public List<Keyword> getPhysicalState() {
      return Optional.ofNullable(descriptiveKeywords)
          .orElse(Collections.emptyList())
          .stream()
          .filter((dk) -> dk.getType().equalsIgnoreCase("physicalState"))
          .flatMap(dk -> dk.getKeywords().stream())
          .collect(Collectors.toList());
  }

  public List<Keyword> getSampleType() {
      return Optional.ofNullable(descriptiveKeywords)
          .orElse(Collections.emptyList())
          .stream()
          .filter((dk) -> dk.getType().equalsIgnoreCase("sampleType"))
          .flatMap(dk -> dk.getKeywords().stream())
          .collect(Collectors.toList());
  }

}

