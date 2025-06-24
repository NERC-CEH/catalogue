package uk.ac.ceh.gateway.catalogue.depositRequest;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

@Slf4j
@Controller
@RequestMapping("/{catalogue}/deposit-request")
public class DepositRequestController {

    public DepositRequestController() {
        log.info("Creating");
    }

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public String depositForm(
        @PathVariable("catalogue") String catalogue
    ) {
        return "html/deposit_request/deposit_form";
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> depositForm(
        @Valid @RequestBody DepositRequestModel depositRequest,
        BindingResult bindingResult
    ) {
        log.info("Received deposit request: {}", depositRequest);

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok("This is a successful test");
    }

    @GetMapping(value = "/success", produces = MediaType.TEXT_HTML_VALUE)
    public String depositSuccess(
        @PathVariable("catalogue") String catalogue,
        Model model
    ) {
        model.addAttribute("referenceNumber", "177266356");
        return "html/deposit_request/deposit_success";
    }

}
