package uk.ac.ceh.gateway.catalogue.model;

import lombok.Data;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.validation.ValidatorResult;

import java.util.List;

@ConvertUsing({
    @Template(called="html/validation.ftlh", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
@Data
public class ValidationResponse {
    private final List<ValidatorResult> validatorResults;
    private final List<String> failed;
}
