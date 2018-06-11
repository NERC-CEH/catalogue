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
public class MonitoringFeature extends Feature {
  private double latitude;
  private double longitude;
  private double z;
  private List<RangedAttribute> rangedAttributes;
  private List<SingleValueAttribute> singleValueAttributes;
  private List<ObservableProperty> hasProperty;
  private List<ObservationPlaceholder> isFeatureOfInterestTo; 
}