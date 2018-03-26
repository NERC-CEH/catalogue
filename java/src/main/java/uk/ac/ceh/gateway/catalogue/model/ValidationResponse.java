package uk.ac.ceh.gateway.catalogue.model;

import java.util.List;
import lombok.Data;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.validation.ValidatorResult;

@ConvertUsing({
    @Template(called="html/validation.ftl", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
@Data
public class ValidationResponse {
    private final List<ValidatorResult> validatorResults;
    private final List<String> failed;
}
