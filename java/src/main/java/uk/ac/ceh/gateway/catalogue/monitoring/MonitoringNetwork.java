package uk.ac.ceh.gateway.catalogue.monitoring;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.gemini.TimePeriod;
import uk.ac.ceh.gateway.catalogue.gemini.Keyword;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;
import uk.ac.ceh.gateway.catalogue.model.Supplemental;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ConvertUsing({
    @Template(called="html/monitoring/monitoringNetwork.ftlh", whenRequestedAs= MediaType.TEXT_HTML_VALUE)
})
public class MonitoringNetwork extends AbstractMetadataDocument{
    private List<String> alternateTitles;
    private String objectives;
    private List<ResponsibleParty> responsibleParties;
    private TimePeriod operatingPeriod;
    private List<Keyword> keywordsOther;
    private List<Supplemental> linksData, linksOther;

}
