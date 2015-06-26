package uk.ac.ceh.gateway.catalogue.imp;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class Model extends ImpDocument {
    private String version, contact, license, operatingRequirements, applicationType,
            smallestAndLargestApplication, geographicalRestrictions, temporalResolution,
            keyOutputs, outputData, calibrationRequired, modelStructure, dataInput;
    private List<String> keyReferences;
    private Link documentation;
}
