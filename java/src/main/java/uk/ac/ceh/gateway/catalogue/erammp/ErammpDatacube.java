package uk.ac.ceh.gateway.catalogue.erammp;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;
import uk.ac.ceh.gateway.catalogue.gemini.SpatialReferenceSystem;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceConstraint;
import uk.ac.ceh.gateway.catalogue.indexing.WellKnownText;
import uk.ac.ceh.gateway.catalogue.model.Link;
import uk.ac.ceh.gateway.catalogue.model.DataTypeSchema;
import uk.ac.ceh.gateway.catalogue.model.Provenance;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;


import java.util.*;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
@ConvertUsing({
  @Template(called="html/erammp/erammp_datacube.ftl", whenRequestedAs= MediaType.TEXT_HTML_VALUE)
})
public class ErammpDatacube extends AbstractMetadataDocument implements WellKnownText{
  private String version, dataFormat, spatialResolution, spatialRepresentationType, condition;
  private List<BoundingBox> boundingBoxes;
  private List<String> dataLocations;
  private List<ResponsibleParty> provider;
  private List<Link> resourceLocators;
  private List<SpatialReferenceSystem> spatialReferenceSystems;
  private List<ResourceConstraint> useConstraints, accessConstraints;
  private List<DataTypeSchema> schema;
  private List<Provenance> provenance;
  private List<Keyword> keywords;

  @Override
  public List<String> getWKTs() {
    return Optional.ofNullable(boundingBoxes)
        .orElse(Collections.emptyList())
        .stream()
        .map(BoundingBox::getWkt)
        .collect(Collectors.toList());
  }

  public String getCondition() {
    return condition;
  }
}


