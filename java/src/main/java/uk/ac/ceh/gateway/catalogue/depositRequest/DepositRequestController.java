package uk.ac.ceh.gateway.catalogue.depositRequest;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
        @ActiveUser CatalogueUser user,
        @Value("${documents.baseUri}") String baseUri,
        Model model
    ) {
        if (user == null || user.isPublic()) {
            model.addAttribute("baseUri", baseUri);
            return "html/deposit_request/deposit_login";
        }
        return "html/deposit_request/deposit_form";
    }

    @PreAuthorize("@permission.hasLogin(#user)")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> depositForm(
        @ActiveUser CatalogueUser user,
        @Valid @RequestBody DepositRequestModel depositRequest,
        BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("Invalid request");
        }

        depositRequestService.handleSubmission(depositRequest);
        return ResponseEntity.status(201)
                .header("Location", "/deposit-request/success")
                .build();
    }

    @PreAuthorize("@permission.hasLogin(#user)")
    @GetMapping(value = "/success", produces = MediaType.TEXT_HTML_VALUE)
    public String depositSuccess(
        @ActiveUser CatalogueUser user,
        Model model
    ) {
        model.addAttribute("referenceNumber", "-");
        return "html/deposit_request/deposit_success";
    }

}
