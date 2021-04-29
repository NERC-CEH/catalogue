package uk.ac.ceh.gateway.catalogue.controllers;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ceh.gateway.catalogue.indexing.DEIMSSolrScheduledSiteService;
import uk.ac.ceh.gateway.catalogue.indexing.DIEMSSite;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;

@Slf4j
@ToString
@Controller
public class DEIMSSiteSearchController {

    private DEIMSSolrScheduledSiteService DEIMSService;


    @Autowired
    public void DEIMSSolrScheduledSiteService(DEIMSSolrScheduledSiteService DEIMSService) {
        this.DEIMSService = DEIMSService;
        log.info("Creating {}", this);
    }

    @RequestMapping(value = "/diemssites", method   = RequestMethod.GET)
    @ResponseBody
    public DIEMSSite[] getSites() throws DocumentIndexingException {
        return this.DEIMSService.fetchDEIMSSitesAndAddToSolr();
    }
}
