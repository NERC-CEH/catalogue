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

import lombok.extern.slf4j.Slf4j;
import uk.ac.ceh.gateway.catalogue.model.DepositRequest;

@Slf4j
@Controller
public class DepositRequestController {

    @Autowired
    public DepositRequestController() {}

    @RequestMapping(value = "deposit-request", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView documentsUpload() {
        Map<String, Object> model = new HashMap<>();
        return new ModelAndView("/html/deposit-request.html.tpl", model);
    }

    @RequestMapping(value = "deposit-request/form", method = RequestMethod.POST)
    @ResponseBody
    public ModelAndView documentsUploadForm(@ModelAttribute DepositRequest depositRequest) {
        Map<String, Object> model = new HashMap<>();
        model.put("depositRequest {}", depositRequest);
        return new ModelAndView("/html/deposit-request-filled.html.tpl", model);
    }

}
