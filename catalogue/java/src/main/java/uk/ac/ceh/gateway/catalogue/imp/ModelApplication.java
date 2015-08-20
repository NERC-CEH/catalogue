package uk.ac.ceh.gateway.catalogue.imp;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;

@Data
@EqualsAndHashCode(callSuper=true)
@ConvertUsing({
    @Template(called="html/imp-application.html.tpl", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
public class ModelApplication extends ImpDocument {
    private String date, studySite, studyScale, objective, funderDetails;
    private List<String> inputData;
    private Contact modellerDetails;
    private List<AssociationResource> associatedResources;
}
