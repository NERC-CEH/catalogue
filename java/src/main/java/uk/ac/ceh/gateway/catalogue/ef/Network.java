package uk.ac.ceh.gateway.catalogue.ef;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;

import java.util.ArrayList;
import java.util.List;

import static uk.ac.ceh.gateway.catalogue.CatalogueMediaTypes.EF_INSPIRE_XML_VALUE;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@XmlRootElement
@XmlType(propOrder = {
    "legalBackground",
    "observingCapabilities",
    "supersedes",
    "supersededBy",
    "narrowerThan",
    "broaderThan",
    "involvedIn",
    "contains"
})
@ConvertUsing({
    @Template(called="html/ef.ftlh", whenRequestedAs=MediaType.TEXT_HTML_VALUE),
    @Template(called="xml/emn.ftlx",   whenRequestedAs=EF_INSPIRE_XML_VALUE)
})
public class Network extends BaseMonitoringType {
    private List<Link>
        legalBackground = new ArrayList<>(),
        involvedIn = new ArrayList<>(),
        supersedes = new ArrayList<>(),
        supersededBy = new ArrayList<>();

    private List<Link.TimedLink>
        broaderThan = new ArrayList<>(),
        narrowerThan = new ArrayList<>(),
        contains = new ArrayList<>();

    @XmlElement(name = "observingCapability")
    private List<ObservingCapability> observingCapabilities = new ArrayList<>();
}
