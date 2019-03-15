package uk.ac.ceh.gateway.catalogue.erammp;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;
import uk.ac.ceh.gateway.catalogue.indexing.WellKnownText;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.DataTypeSchema;
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
  @Template(called="html/erammp/erammp_datacube.ftl", whenRequestedAs= MediaType.TEXT_HTML_VALUE)
})
public class ErammpDatacube extends AbstractMetadataDocument implements WellKnownText{
  private String version, ipr, dataFormat, dataSource, spatialResolution, spatialRepresentationType, constraints;
  private List<Keyword> keywords;
  private List<String> locations;
  private List<ResponsibleParty> provider;
  private List<DataTypeSchema> schema;
  private List<Link> resourceLocators;
  private List<BoundingBox> boundingBoxes;

  @Override
  public List<String> getWKTs() {
    return Optional.ofNullable(boundingBoxes)
        .orElse(Collections.emptyList())
        .stream()
        .map(BoundingBox::getWkt)
        .collect(Collectors.toList());
  }

}


