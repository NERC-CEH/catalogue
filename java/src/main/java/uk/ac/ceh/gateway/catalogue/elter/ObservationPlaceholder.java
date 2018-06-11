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
public class ObservationPlaceholder extends AbstractMetadataDocument {
    private List<Input> routedTo;
    private List<Input> usedBy;
    private List<TemporalProcedure> visibleThrough;
    private List<Input> controlsFrequencyOf;
    private Feature hasFeatureOfInterest;
    private ObservableProperty observedProperty;
    private Stimulus wasOriginatedBy;
    private Sensor madeBy;
    private TemporalProcedure usedProcedure;
}