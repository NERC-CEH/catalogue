package uk.ac.ceh.gateway.catalogue.imp;

import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;

@Accessors(chain = true)
@EqualsAndHashCode(callSuper=true)
@ConvertUsing({
    @Template(called="html/imp-casestudy.html.tpl", whenRequestedAs=MediaType.TEXT_HTML_VALUE)
})
public class CaseStudy extends ImpDocument {

}
