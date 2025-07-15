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
    @NotBlank(message = "Name is required")
    String name,

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email.")
    @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Invalid email format.")
    String email,

    @NotBlank(message = "Affiliation is required")
    String affiliation,

    @AssertTrue(message = "Please check to proceed")
    Boolean checklist1,

    @AssertTrue(message = "Please check to proceed")
    Boolean checklist2,

    @AssertTrue(message = "Please check to proceed")
    Boolean checklist3,

    @AssertTrue(message = "Please check to proceed")
    Boolean checklist4,

    @NotBlank(message = "Funder is required")
    String funder,

    String funderOther,

    String fundingRef,

    @NotNull(message = "Please make a choice")
    String eidcRemit,

    @NotNull(message = "Please make a choice")
    String alternativeData,

    @NotNull(message = "Please make a choice")
    Boolean hasSupportingDocs,

    @NotNull(message = "Please make a choice")
    Boolean isSupportingDocsReady,

    @NotNull(message = "Please make a choice")
    Boolean replaceExisting,

    @NotNull(message = "Please make a choice")
    Boolean relatedToExisting,

    @Valid
    @NotNull(message = "You must add at least one data resource")
    @Size(min = 1, message = "You must add at least one data resource")
    List<DataResourceModel> dataResources,

    String additionalInfo
) {
    @AssertTrue(message = "Funder(s) not specified")
    public boolean isValidFunderOther() {
        return !"Other".equals(funder) || (funderOther != null && !funderOther.trim().isEmpty());
    }

    @AssertTrue(message = "You must add at least one data resource")
    public boolean isValidDataResources() {
        return dataResources != null && !dataResources.isEmpty();
    }
}
