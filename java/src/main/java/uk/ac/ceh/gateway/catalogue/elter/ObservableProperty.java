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
public class ObservableProperty extends AbstractMetadataDocument {

    private List<Sensor> observedBy;
    private List<Feature> isPropertyOf;
    private String cpmStatisticalMeasure;
    private String cpmMatrix;
    private String cpmConstraint;
    private String cpmUnitOfMeasure;
    private String cpmProperty;
    private String cpmObjectOfInterest;

}