package uk.ac.ceh.gateway.catalogue.depositRequest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataResourceModel {
    @NotBlank(message = "Working title is required")
    private String title;

    @NotBlank(message = "Brief description is required")
    private String description;

    @NotBlank(message = "Resource type is required.")
    private String resourceType;

    private String resourceTypeOther;

    private Boolean easilyRecreated;

    @NotBlank(message = "Resource format is required.")
    private String resourceFormat;

    private String resourceFormatOther;

    @NotBlank(message = "Size is required.")
    private String size;

    @JsonCreator
    public static DataResourceModel create(
        @JsonProperty("title") String title,
        @JsonProperty("description") String description,
        @JsonProperty("resourceType") String resourceType,
        @JsonProperty("resourceTypeOther") String resourceTypeOther,
        @JsonProperty("easilyRecreated") Boolean easilyRecreated,
        @JsonProperty("resourceFormat") String resourceFormat,
        @JsonProperty("resourceFormatOther") String resourceFormatOther,
        @JsonProperty("size") String size)
    {
        return DataResourceModel.builder()
            .title(title)
            .description(description)
            .resourceType(resourceType)
            .resourceTypeOther(resourceTypeOther)
            .easilyRecreated(easilyRecreated)
            .resourceFormat(resourceFormat)
            .resourceFormatOther(resourceFormatOther)
            .size(size)
            .build();
    }

    @AssertTrue(message = "Resource type not specified.")
    private boolean isValidResourceTypeOther() {
        return !"Other".equals(resourceType) ||
            (resourceTypeOther != null && !resourceTypeOther.trim().isEmpty());
    }

    @AssertTrue(message = "Selection is required.")
    private boolean isValidEasilyRecreated() {
        return !("Other".equals(resourceType) || "Model output".equals(resourceType)) ||
            (easilyRecreated != null);
    }

    @AssertTrue(message = "Resource format not specified.")
    private boolean isValidResourceFormatOther() {
        return !"Other".equals(resourceFormat) ||
            (resourceFormatOther != null && !resourceFormatOther.trim().isEmpty());
    }
}
