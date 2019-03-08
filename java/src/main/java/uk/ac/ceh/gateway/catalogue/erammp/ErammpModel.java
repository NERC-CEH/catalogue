package uk.ac.ceh.gateway.catalogue.erammp;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.indexing.WellKnownText;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.erammp.ErammpInput;
import uk.ac.ceh.gateway.catalogue.erammp.ErammpOutput;
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
  @Template(called="html/erammp/erammp_model.ftl", whenRequestedAs= MediaType.TEXT_HTML_VALUE)
})
public class ErammpModel extends AbstractMetadataDocument implements WellKnownText {
  private String modelApproach, version, ipr, spatialResolution, runtimeTotal, runtimeWales, runtimeOptimisation, calibrationEffort, futureRun, integrationExperience, integrationHistory;
  private List<Keyword> keywords;
  private List<BoundingBox> boundingBoxes;
  private List<String> outputFormats, sectors, programmingLanguages, operatingSystems, timeSteps;
  private List<ResponsibleParty> contacts;
  private List<Link> resourceLocators;
  private List<ErammpInput> inputs;
  private List<ErammpOutput> outputs;
  private boolean spatiallyExplicit, calibratedForWales;

  @Override
  public List<String> getWKTs() {
    return Optional.ofNullable(boundingBoxes)
        .orElse(Collections.emptyList())
        .stream()
        .map(BoundingBox::getWkt)
        .collect(Collectors.toList());
  }
}