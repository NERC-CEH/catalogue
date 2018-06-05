package uk.ac.ceh.gateway.catalogue.elter;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
public class TemporalProcedure extends AbstractMetadataDocument {
  private List<TemporalProcedure> replacedBy;
  private String commitCode;
  private InterpolationType interpolationType;
  private int intendedObservationSpacing;
  private int maximumGap;
  private int anchorTime;
  private SampleMedium sampleMedium;
  private List<ObservationPlaceholder> makeVisible;
  private String loggerSensorName;
  private String historicSensorName;
  private String loggerName;
  private String historicFeatureName;
  private List<Input> hasInput;
  private List<ObservationPlaceholder> usedProcedure;
  private Sensor implementedBy;
  private TemporalConstraint temporalConstraint;
  private int temporalLimit;
}