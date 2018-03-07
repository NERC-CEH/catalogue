package uk.ac.ceh.gateway.catalogue.elter;

import java.util.Set;

import org.springframework.http.MediaType;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
@ConvertUsing({
    @Template(called="html/elter/single-system-deployment.html.tpl", whenRequestedAs = MediaType.TEXT_HTML_VALUE)
})
public class SingleSystemDeploymentDocument extends AbstractMetadataDocument {
    private Set<String> powers;
    private String powersName;
    private Set<String> deploymentInstallation;
    private String deploymentInstallationName;
    private Set<String> deploymentRemoval;
    private String deploymentRemovalName;
    private Set<String> deploymentCleaning;
    private String deploymentCleaningName;
    private Set<String> deploymentMaintenence;
    private String deploymentMaintenenceName;
    private Set<String> deploymentProgramUpdate;
    private String deploymentProgramUpdateName;

}