package uk.ac.ceh.gateway.catalogue.depositRequest;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
            throw new AccessDeniedException("Login required");
        }
        return "html/deposit_request/deposit_form";
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> depositForm(
        @ActiveUser CatalogueUser user,
        @Valid @RequestBody DepositRequestModel depositRequest,
        BindingResult bindingResult
    ) {
        if (user == null || user.isPublic()) {
            throw new AccessDeniedException("Login required");
        }

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("Invalid request");
        }

        depositRequestService.handleSubmission(depositRequest);
        return ResponseEntity.status(201)
                .header("Location", "/deposit-request/success")
                .build();
    }

    @GetMapping(value = "/success", produces = MediaType.TEXT_HTML_VALUE)
    public String depositSuccess(Model model) {
        model.addAttribute("referenceNumber", "177266356");
        return "html/deposit_request/deposit_success";
    }

}
