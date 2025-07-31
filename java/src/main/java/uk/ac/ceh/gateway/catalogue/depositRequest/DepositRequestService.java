package uk.ac.ceh.gateway.catalogue.depositRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static uk.ac.ceh.gateway.catalogue.util.Headers.withBasicAuth;

@Slf4j
@Service
public class DepositRequestService {

    private final RestTemplate restTemplate;
    private final String jiraEndpoint;
    private final String username;
    private final String password;
    private final String projectKey;
    private final ObjectMapper mapper = new ObjectMapper();

    public DepositRequestService(
        @Qualifier("normal") RestTemplate restTemplate,
        @Value("${jira.username}") String username,
        @Value("${jira.password}") String password,
        @Value("${jira.address}") String jiraAddress,
        @Value("${jira.depositRequest.project}") String projectKey
    ) {
        this.restTemplate = restTemplate;
        this.jiraEndpoint = jiraAddress;
        this.username = username;
        this.password = password;
        this.projectKey = projectKey;
        log.info("Creating DepositRequestService");
    }

    public ObjectNode handleSubmission(DepositRequestModel form) {
        log.info("Handling deposit request for {}", form.name());

        val url = UriComponentsBuilder
            .fromUriString(jiraEndpoint)
            .path("/issue")
            .build()
            .toUri();

        try {
            String jsonPayload = buildJiraPayload(form);

            val headers = withBasicAuth(username, password);
            headers.setContentType(APPLICATION_JSON);
            val request = new HttpEntity<>(jsonPayload, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                String.class
            );

            log.info("JIRA responded with: {}", response.getStatusCode());
            log.debug("JIRA response body: {}", response.getBody());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode responseNode = mapper.readTree(response.getBody());
            ObjectNode modifiedResponse = mapper.createObjectNode();
            modifiedResponse.setAll((ObjectNode) responseNode);
            modifiedResponse.put("componentName", getJiraComponentName(form));

            return modifiedResponse;
        } catch (RestClientResponseException ex) {
            log.error("Error submitting to JIRA: {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error during JIRA submission", ex);
            throw new RuntimeException("Failed to submit deposit request to JIRA", ex);
        }
    }

    public String getJiraComponentName(DepositRequestModel form) {
        String componentName = "Deposit Request";

        if (form.funder().equals("NERC")
            && form.eidcRemit().equals("Yes")
            && form.alternativeData().equals("No")
            && form.hasSupportingDocs()
            && form.isSupportingDocsReady()
            && !form.replaceExisting()
        ) {
            boolean dataResourcePass = true;
            for (DataResourceModel dataResource : form.dataResources()) {
                if (List.of("Interview/survey", "Images", "Other").contains(dataResource.resourceType())
                    || (dataResource.easilyRecreated() != null && dataResource.easilyRecreated())
                    || dataResource.resourceFormat().equals("Other")
                    || dataResource.largeData()
                ) {
                    dataResourcePass = false;
                    break;
                }
            }
            if (dataResourcePass) {
                componentName = "Deposit Request";
                //temporarily change this ti disable the logic componentName = "Ingestion Management";
            }
        }

        return componentName;
    }

    private String buildJiraPayload(DepositRequestModel form) throws Exception {
        val payload = mapper.createObjectNode();
        val fields = payload.putObject("fields");

        fields.putObject("project").put("key", projectKey);
        fields.put("summary", "Deposit request for " + form.name() + " " + LocalDate.now());
        fields.put("description", buildDescription(form));
        fields.putArray("components").addObject().put("name", getJiraComponentName(form));
        fields.putObject("issuetype").put("name", "Job");

        val fundingRefs = mapper.createArrayNode();
        for (String ref : splitToList(form.fundingRef())) fundingRefs.add(ref);
        fields.set("customfield_12053", fundingRefs);

        val funders = mapper.createArrayNode();
        for (String f : splitToList(resolveOtherField(form.funder(), form.funderOther()))) funders.add(f);
        fields.set("customfield_13868", funders);

        fields.put("customfield_11950", form.name());
        fields.put("customfield_11951", form.email());

        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
        log.debug("Built JIRA JSON payload:\n{}", json);
        return json;
    }

    private String buildDescription(DepositRequestModel form) {
        val sb = new StringBuilder();

        sb.append("*Name:* ").append(form.name()).append("  \n  \n")
            .append("*Email:* ").append(form.email()).append("  \n  \n")
            .append("*Affiliation:* ").append(form.affiliation()).append("  \n  \n")
            .append("*Funder:* ").append(resolveOtherField(form.funder(), form.funderOther())).append("  \n  \n");

        if (form.fundingRef() != null && !form.fundingRef().isEmpty()) {
            sb.append("*Grant reference:* ").append(form.fundingRef()).append("  \n  \n");
        }

        sb.append("*Are data in EIDC's remit?:* ").append(form.eidcRemit()).append("  \n  \n")
            .append("*Is any resource omics, social or model code?:* ").append(form.alternativeData()).append("  \n  \n")
            .append("*Has supporting documentation?:* ").append(booleanToYesNo(form.hasSupportingDocs())).append("  \n  \n")
            .append("*Supporting docs and data correct?:* ").append(booleanToYesNo(form.isSupportingDocsReady())).append("  \n  \n")
            .append("*Are these data replacing data already held by the EIDC?:* ").append(booleanToYesNo(form.replaceExisting())).append("  \n  \n")
            .append("*Are these resource(s) related to those already held by the EIDC?:* ").append(booleanToYesNo(form.relatedToExisting())).append("  \n  \n")
            .append("  \n  \n");

        if (form.dataResources() != null && !form.dataResources().isEmpty()) {
            for (int i = 0; i < form.dataResources().size(); i++) {
                val r = form.dataResources().get(i);
                sb.append("{panel:borderWidth=2|borderColor=#0484a4|titleBGColor=#0484a4|titleColor=white|title=Dataset ").append(i + 1).append("}")
                    .append("*Name:* ").append(r.title()).append("  \n  \n")
                    .append("*Description:* ").append(r.description()).append("  \n  \n")
                    .append("*Type:* ").append(resolveOtherField(r.resourceType(), r.resourceTypeOther())).append("  \n  \n");

                if ("Model output".equals(r.resourceType())) {
                    sb.append("*Could it be easily re-generated?:* ").append(booleanToYesNo(r.easilyRecreated())).append("  \n  \n");
                    }

                sb.append("*Format:* ").append(resolveOtherField(r.resourceFormat(), r.resourceFormatOther())).append("  \n  \n")
                    .append("*Size:* ").append(r.size()).append("  \n  \n")
                    .append("*Larger than 1Tb?:* ").append(booleanToYesNo(r.largeData())).append("  \n")
                    .append("{panel}  \n");
            }
        }

        if (form.additionalInfo() != null && !form.additionalInfo().isEmpty()) {
            sb.append("  \n  \n h1. Additional Information  \n")
                .append(form.additionalInfo());
        }

        return sb.toString();
    }

    private List<String> splitToList(String input) {
        if (input == null || input.isBlank()) return List.of();
        return Arrays.stream(input.split("[,\\s]+"))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();
    }

    private String resolveOtherField(String mainValue, String otherValue) {
        return "Other".equalsIgnoreCase(mainValue) && otherValue != null && !otherValue.isBlank()
            ? otherValue
            : mainValue;
    }

    private String booleanToYesNo(Boolean value) {
        return Boolean.TRUE.equals(value) ? "Yes" : "No";
    }
}
