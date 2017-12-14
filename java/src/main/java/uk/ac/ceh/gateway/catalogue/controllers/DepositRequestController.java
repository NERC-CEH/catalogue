package uk.ac.ceh.gateway.catalogue.controllers;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.google.common.collect.Lists;

import lombok.val;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.DepositRequest;

@Slf4j
@Controller
public class DepositRequestController {

    @Autowired
    public DepositRequestController() {}

    @RequestMapping(value = "deposit-request", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView documentsUpload(@ActiveUser CatalogueUser user) {
        Map<String, Object> model = new HashMap<>();
        model.put("name", user.getUsername());
        model.put("email", user.getEmail());
        return new ModelAndView("/html/deposit-request.html.tpl", model);
    }

    @RequestMapping(value = "deposit-request/form", method = RequestMethod.POST)
    @ResponseBody
    public RedirectView documentsUploadForm(@ModelAttribute DepositRequest depositRequest) {
        log.error("deposit request {}", depositRequest);
        return new RedirectView("/deposit-request/guid");
    }

    @RequestMapping(value = "deposit-request/guid", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView documentsUploadView() {
        Map<String, Object> model = new HashMap<>();
        val depositRequest = new DepositRequest();
        depositRequest.setDatasetTitle("datasetTitle");
        depositRequest.setDepositorName("depositorName");
        depositRequest.setDepositorEmail("depositorEmail");
        depositRequest.setDepositorOtherContact("line 1\nline 2");
        depositRequest.setProjectName("projectName");
        depositRequest.setPlanningDocs("other");
        depositRequest.setPlanningDocsOther("line 1\nline 2\nline3");
        depositRequest.setNercFunded(DepositRequest.Funded.yes);
        depositRequest.setPublicFunded(DepositRequest.Funded.yes);
        depositRequest.setScienceDomain("other");
        depositRequest.setScienceDomainOther("scienceDomainOther");
        depositRequest.setUniqueDeposit(true);
        depositRequest.setModelOutput(false);
        depositRequest.setPublishedPaper(true);
        depositRequest.setReusable(false);

        val dataset0 = new DepositRequest.DatasetOffered();
        val dataset1 = new DepositRequest.DatasetOffered();
        dataset1.setName("name");
        dataset1.setDocument("document");

        depositRequest.setDatasetsOffered(Lists.newArrayList(dataset0, dataset1));

        depositRequest.setHasRelatedDatasets(true);
        depositRequest.setRelatedDatasets(Lists.newArrayList("doi0", "doi1"));

        model.put("depositRequest", depositRequest);

        model.put("depositorOtherContactRows", countLines(depositRequest.getDepositorOtherContact()));
        model.put("planningDocsRows", countLines(depositRequest.getPlanningDocs()));

        model.put("status", "Awaiting Approval");
        
        return new ModelAndView("/html/deposit-request-filled.html.tpl", model);
    }

    private static int countLines(String str){
        return str.split("\r\n|\r|\n").length;
     }

}


