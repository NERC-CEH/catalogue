package uk.ac.ceh.gateway.catalogue.monitoring;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.RDF_TTL_VALUE;

@Data
@EqualsAndHashCode(callSuper = true)
@ConvertUsing({
    @Template(called="html/monitoring/monitoringNetwork.ftlh", whenRequestedAs= MediaType.TEXT_HTML_VALUE),
    @Template(called="rdf/monitoring/network.ftl", whenRequestedAs=RDF_TTL_VALUE)
})
public class MonitoringNetwork extends AbstractMetadataDocument{


}
