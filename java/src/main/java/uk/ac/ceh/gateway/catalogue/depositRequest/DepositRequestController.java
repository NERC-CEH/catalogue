package uk.ac.ceh.gateway.catalogue.depositRequest;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

@Slf4j
@Controller
@RequestMapping("/deposit-request")
public class DepositRequestController {

    DepositRequestService depositRequestService;

    public DepositRequestController(DepositRequestService depositRequestService) {
        this.depositRequestService = depositRequestService;
        log.info("Creating");
    }

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public String depositForm(
        @ActiveUser CatalogueUser user
    ) {
        if (user == null || user.isPublic()) {
            return "html/deposit_request/deposit_login";
        }
        return "html/deposit_request/deposit_form";
    }

    @PreAuthorize("@permission.hasLogin(#user)")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> depositForm(
        @ActiveUser CatalogueUser user,
        @Valid @RequestBody DepositRequestModel depositRequest,
        BindingResult bindingResult,
        HttpSession session
    ) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("Invalid request");
        }

        ObjectNode jiraResponse = depositRequestService.handleSubmission(depositRequest);
        session.setAttribute("referenceNumber", jiraResponse.get("key").asText());
        session.setAttribute("componentName", jiraResponse.get("componentName").asText());

        return ResponseEntity.status(201)
                .header("Location", "/deposit-request/success")
                .build();
    }

    @PreAuthorize("@permission.hasLogin(#user)")
    @GetMapping(value = "/success", produces = MediaType.TEXT_HTML_VALUE)
    public String depositSuccess(
        @ActiveUser CatalogueUser user,
        HttpSession session,
        Model model
    ) {
        model.addAttribute("referenceNumber", session.getAttribute("referenceNumber") != null ? session.getAttribute("referenceNumber").toString() : "");
        model.addAttribute("componentName", session.getAttribute("componentName") != null ? session.getAttribute("componentName").toString() : "");
        session.removeAttribute("referenceNumber");
        session.removeAttribute("componentName");

        return "html/deposit_request/deposit_success";
    }

}
