package uk.ac.ceh.gateway.catalogue.depositRequest;

import tools.jackson.databind.node.ObjectNode;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;

import java.util.List;
import java.util.regex.Pattern;

import static uk.ac.ceh.gateway.catalogue.depositRequest.DepositRequestService.DEPOSIT_REQUEST_COMPONENT;
import static uk.ac.ceh.gateway.catalogue.depositRequest.DepositRequestService.INGESTION_MANAGEMENT_COMPONENT;

@Slf4j
@Controller
@RequestMapping("/deposit-request")
public class DepositRequestController {

    private static final String SUCCESS_PATH = "/deposit-request/success";

    /**
     * A JIRA issue key, as returned by the JIRA API — a project key, a hyphen, and an issue number.
     */
    private static final Pattern JIRA_ISSUE_KEY = Pattern.compile("^[A-Z][A-Z0-9_]{1,20}-[0-9]{1,10}$");

    private static final List<String> KNOWN_COMPONENTS =
        List.of(DEPOSIT_REQUEST_COMPONENT, INGESTION_MANAGEMENT_COMPONENT);

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

    /**
     * The outcome is handed to the success page in the redirect itself rather than through the
     * {@code HttpSession} it used to be stashed in. The session was the only piece of server-side state
     * in the whole application, and it made the reference number silently vanish whenever anything
     * between the browser and here disturbed the session cookie — the JIRA issue was still raised, but
     * the success page rendered an empty reference. Nothing between the two requests has to be trusted
     * to carry state now, and the page stays correct on a refresh or a bookmark.
     */
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

        ObjectNode jiraResponse = depositRequestService.handleSubmission(depositRequest);

        val location = UriComponentsBuilder.fromPath(SUCCESS_PATH)
            .queryParam("reference", jiraResponse.get("key").asString())
            .queryParam("component", jiraResponse.get("componentName").asString())
            .build()
            .encode()
            .toUriString();

        return ResponseEntity.status(201)
                .header("Location", location)
                .build();
    }

    /**
     * Both parameters arrive from the URL, so neither is trusted: the reference has to look like a JIRA
     * issue key and the component has to be one this application actually raises requests against.
     * Anything else falls back to a page that simply says thank you. Templates are {@code .ftlh}, so
     * these are HTML-escaped on the way out as well.
     */
    @PreAuthorize("@permission.hasLogin(#user)")
    @GetMapping(value = "/success", produces = MediaType.TEXT_HTML_VALUE)
    public String depositSuccess(
        @ActiveUser CatalogueUser user,
        @RequestParam(defaultValue = "") String reference,
        @RequestParam(defaultValue = "") String component,
        Model model
    ) {
        model.addAttribute(
            "referenceNumber",
            JIRA_ISSUE_KEY.matcher(reference).matches() ? reference : "");
        model.addAttribute(
            "componentName",
            KNOWN_COMPONENTS.contains(component) ? component : DEPOSIT_REQUEST_COMPONENT);

        return "html/deposit_request/deposit_success";
    }

}
