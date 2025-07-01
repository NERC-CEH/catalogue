package uk.ac.ceh.gateway.catalogue.depositRequest;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ConvertUsing({
    @Template(called = "html/deposit_request/deposit_form.ftlh", whenRequestedAs = MediaType.TEXT_HTML_VALUE),
})
public class DepositRequestModel extends RepresentationModel<DepositRequestModel> {
    @NotBlank(message = "Name is required.")
    private String name;

    @NotBlank(message = "Email is required.")
    @Email(message = "Invalid email.")
    @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Invalid email format.")
    private String email;

    @NotBlank(message = "Affiliation is required.")
    private String affiliation;

    @AssertTrue(message = "You must agree to the terms and conditions.")
    @JsonProperty("isAgreed")
    private Boolean isAgreed;

    @NotBlank(message = "Funder is required.")
    private String funder;

    private String funderOther;
    private String fundingRef;
    private String eidcRemit;
    private String alternativeData;

    @NotNull(message = "Selection is required.")
    private Boolean hasSupportingDocs;

    private Boolean isSupportingDocsReady;

    @NotNull(message = "Selection is required.")
    private Boolean replaceExisting;

    @NotNull(message = "Selection is required.")
    private Boolean relatedToExisting;

    @Valid
    @NotNull(message = "At least one data resource is required.")
    @Size(min = 1, message = "At least one data resource is required.")
    private List<@NotNull DataResourceModel> dataResources;

    private String additionalInfo;

    @AssertTrue(message = "Funder(s) not specified.")
    private boolean isValidFunderOther() {
        return !"Other".equals(funder) ||
            (funderOther != null && !funderOther.trim().isEmpty());
    }

    @AssertTrue(message = "At least one data resource is required.")
    private boolean isValidDataResources() {
        return dataResources != null && !dataResources.isEmpty();
    }
}
