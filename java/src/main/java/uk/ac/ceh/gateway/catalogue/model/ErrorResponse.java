package uk.ac.ceh.gateway.catalogue.model;

import lombok.Value;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;

@Value
@ConvertUsing({
    @Template(called="html/error.ftlh", whenRequestedAs="text/*"),
    @Template(called="xml/error.ftlx",   whenRequestedAs="application/*+xml"),
    @Template(called="xml/error.ftlx",   whenRequestedAs=MediaType.APPLICATION_XML_VALUE)
})
public class ErrorResponse {
    String message;
}
