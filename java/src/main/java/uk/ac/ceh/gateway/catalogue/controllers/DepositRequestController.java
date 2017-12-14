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
        // Map<String, Object> model = new HashMap<>();
        // model.put("depositRequest {}", depositRequest);
        // return new ModelAndView("/html/deposit-request-filled.html.tpl", model);
        return new RedirectView("/deposit-request/guid");
    }

    @RequestMapping(value = "deposit-request/guid", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView documentsUploadView() {
        Map<String, Object> model = new HashMap<>();
        val depositRequest = new DepositRequest();
        depositRequest.setDatasetTitle("My Dataset Title");
        model.put("depositRequest {}", depositRequest);
        return new ModelAndView("/html/deposit-request-filled.html.tpl", model);
    }

}
