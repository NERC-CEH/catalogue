package uk.ac.ceh.gateway.catalogue.depositRequest;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;

public record DataResourceModel(
    @NotBlank(message = "Working title is required")
    String title,

    @NotBlank(message = "Brief description is required")
    String description,

    @NotBlank(message = "Resource type is required.")
    String resourceType,

    String resourceTypeOther,

    Boolean easilyRecreated,

    @NotBlank(message = "Resource format is required.")
    String resourceFormat,

    String resourceFormatOther,

    @NotBlank(message = "Size is required.")
    String size
) {
    @AssertTrue(message = "Resource type not specified.")
    public boolean isValidResourceTypeOther() {
        return !"Other".equals(resourceType) || (resourceTypeOther != null && !resourceTypeOther.trim().isEmpty());
    }

    @AssertTrue(message = "Selection is required.")
    public boolean isValidEasilyRecreated() {
        return !("Other".equals(resourceType) || "Model output".equals(resourceType)) || (easilyRecreated != null);
    }

    @AssertTrue(message = "Resource format not specified.")
    public boolean isValidResourceFormatOther() {
        return !"Other".equals(resourceFormat) || (resourceFormatOther != null && !resourceFormatOther.trim().isEmpty());
    }
}
