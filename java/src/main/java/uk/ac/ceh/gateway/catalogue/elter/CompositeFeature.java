package uk.ac.ceh.gateway.catalogue.elter;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
public class CompositeFeature extends Feature {
  private ObservationPlaceholder observationStream;
  private List<ObservableProperty> hasPropery;
  private List<ObservationPlaceholder> isFeatureOfInterestTo;
}