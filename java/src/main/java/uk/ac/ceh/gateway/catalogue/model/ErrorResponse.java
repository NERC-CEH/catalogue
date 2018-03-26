package uk.ac.ceh.gateway.catalogue.model;

import lombok.Data;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;

@Data
@ConvertUsing({
    @Template(called="html/error.ftl", whenRequestedAs="text/*"),
    @Template(called="xml/error.xml.tpl",   whenRequestedAs="application/*+xml"),
    @Template(called="xml/error.xml.tpl",   whenRequestedAs=MediaType.APPLICATION_XML_VALUE)
})
public class ErrorResponse {
    private final String message;
}
