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
public class Sensor extends AbstractMetadataDocument {

    private String shortName;
    private String serialNumber;
    private List<String> documentation;
    private ProcessType processType;
    private Manufacturer manufacturer;
    private List<String> defaultParameters;
    private List<ObservableProperty> observes;
    private List<Stimulus> detects;
    private List<SystemCapability> hasSystemCapability;
    private List<OperatingRange> hasOperatingRange;
    private List<TemporalProcedure> implementsTP;
    private List<ObservationPlaceholder> madeObservation;
}