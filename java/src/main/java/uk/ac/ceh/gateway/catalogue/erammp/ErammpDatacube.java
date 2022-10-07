package uk.ac.ceh.gateway.catalogue.erammp;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.gemini.BoundingBox;
import uk.ac.ceh.gateway.catalogue.gemini.ResourceConstraint;
import uk.ac.ceh.gateway.catalogue.gemini.SpatialReferenceSystem;
import uk.ac.ceh.gateway.catalogue.indexing.solr.GeoJson;
import uk.ac.ceh.gateway.catalogue.indexing.solr.WellKnownText;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.DataTypeSchema;
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
  @Template(called="html/erammp/erammp_datacube.ftlh", whenRequestedAs= MediaType.TEXT_HTML_VALUE)
})
public class ErammpDatacube extends AbstractMetadataDocument implements WellKnownText, GeoJson {
  private String version, dataFormat, spatialResolution, spatialRepresentationType, condition;
  private List<BoundingBox> boundingBoxes;
  private List<DataLocation> dataLocations;
  private List<ResponsibleParty> provider;
  private List<OnlineLink> onlineResources;
  private List<SpatialReferenceSystem> spatialReferenceSystems;
  private List<ResourceConstraint> useConstraints, accessConstraints;
  private List<DataTypeSchema> schema;
  private List<ProcessingStep> processingSteps;


  @Override
  public @NonNull List<String> getWKTs() {
    return Optional.ofNullable(boundingBoxes)
        .orElse(Collections.emptyList())
        .stream()
        .map(BoundingBox::getWkt)
        .collect(Collectors.toList());
  }

  @Override
  public @NonNull List<String> getGeoJson() {
    return Optional.ofNullable(boundingBoxes)
            .orElse(Collections.emptyList())
            .stream()
            .map(BoundingBox::getGeoJson)
            .collect(Collectors.toList());
  }
}


