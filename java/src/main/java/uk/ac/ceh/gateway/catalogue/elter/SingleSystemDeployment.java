package uk.ac.ceh.gateway.catalogue.elter;

import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
public class SingleSystemDeployment extends AbstractMetadataDocument {
    private Set<SingleSystemDeployment> powers;
    private Set<SingleSystemDeployment> poweredBy;
    private Set<DeploymentRelatedProcessDuration> deploymentInstallation;
    private Set<DeploymentRelatedProcessDuration> deploymentRemoval;
    private Set<DeploymentRelatedProcessDuration> deploymentCleaning;
    private Set<DeploymentRelatedProcessDuration> deploymentMaintenance;
    private Set<DeploymentRelatedProcessDuration> deploymentProgramUpdate;
    private Sensor deployedSystem;
}