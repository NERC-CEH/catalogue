package uk.ac.ceh.gateway.catalogue.imp;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class ModelApplication extends ImpDocument {
    private String date, studySite, studyScale, objective, funderDetails;
    private List<String> inputData;
    private Contact modellerDetails;
    private List<AssociationResource> associatedResources;
}
