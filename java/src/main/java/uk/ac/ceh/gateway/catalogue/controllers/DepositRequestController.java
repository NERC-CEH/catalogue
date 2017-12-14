package uk.ac.ceh.gateway.catalogue.controllers;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import lombok.val;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.DepositRequestDocument;
import uk.ac.ceh.gateway.catalogue.services.DepositRequestService;
import uk.ac.ceh.gateway.catalogue.services.PermissionService;

@Controller
public class DepositRequestController {

    private final DepositRequestService depositRequestService;
    private final PermissionService permissionService;

    @Autowired
    public DepositRequestController(DepositRequestService depositRequestService, PermissionService permissionService) {
        this.depositRequestService = depositRequestService;
        this.permissionService = permissionService;
    }

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
    public RedirectView documentsUploadForm(@ActiveUser CatalogueUser user, @ModelAttribute DepositRequestDocument depositRequest) {
        depositRequestService.save(user, depositRequest);
        return new RedirectView(String.format("/deposit-request/%s", depositRequest.getId()));
    }

    @PreAuthorize("@permission.userCanView(#guid)")
    @RequestMapping(value = "deposit-request/{guid}", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView documentsUploadView(@PathVariable("guid") String guid) {
        Map<String, Object> model = new HashMap<>();
        
        val depositRequest = depositRequestService.get(guid);
        model.put("depositRequest", depositRequest);

        model.put("depositorOtherContactRows", countLines(depositRequest.getDepositorOtherContact()));
        model.put("planningDocsRows", countLines(depositRequest.getPlanningDocs()));

        model.put("status", "Awaiting Approval");

        boolean userCanView = permissionService.userCanView(guid);
        model.put("userCanView", userCanView);
        
        return new ModelAndView("/html/deposit-request-filled.html.tpl", model);
    }

    private static int countLines(String str){
        return str.split("\r\n|\r|\n").length;
     }

}


