package uk.ac.ceh.gateway.catalogue.elter;

import java.util.Date;
import java.util.List;
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
public class DeploymentRelatedProcessDuration extends AbstractMetadataDocument {
  private Set<Person> carriedOutBy;
  private List<SingleSystemDeployment> changeProgramDeployment;
  private List<SingleSystemDeployment> maintainedDeployment;
  private List<SingleSystemDeployment> installedDeployment;
  private List<SingleSystemDeployment> removedDeployment;
  private Date startTime;
  private Date endTime;
}