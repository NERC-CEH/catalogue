package uk.ac.ceh.gateway.catalogue.depositRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;

import java.util.List;

@ConvertUsing({
    @Template(called = "html/deposit_request/deposit_form.ftlh",
        whenRequestedAs = MediaType.TEXT_HTML_VALUE)
})
public record DepositRequestModel(
    @NotBlank(message = "Name is required.")
    String name,

    @NotBlank(message = "Email is required.")
    @Email(message = "Invalid email.")
    @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Invalid email format.")
    String email,

    @NotBlank(message = "Affiliation is required.")
    String affiliation,

    @AssertTrue(message = "You must agree to the terms and conditions.")
    Boolean isAgreed,

    @NotBlank(message = "Funder is required.")
    String funder,

    String funderOther,

    String fundingRef,

    String eidcRemit,

    String alternativeData,

    @NotNull(message = "Selection is required.")
    Boolean hasSupportingDocs,

    Boolean isSupportingDocsReady,

    @NotNull(message = "Selection is required.")
    Boolean replaceExisting,

    @NotNull(message = "Selection is required.")
    Boolean relatedToExisting,

    @Valid
    @NotNull(message = "At least one data resource is required.")
    @Size(min = 1, message = "At least one data resource is required.")
    List<DataResourceModel> dataResources,

    String additionalInfo
) {
    @AssertTrue(message = "Funder(s) not specified.")
    public boolean isValidFunderOther() {
        return !"Other".equals(funder) || (funderOther != null && !funderOther.trim().isEmpty());
    }

    @AssertTrue(message = "At least one data resource is required.")
    public boolean isValidDataResources() {
        return dataResources != null && !dataResources.isEmpty();
    }
}
