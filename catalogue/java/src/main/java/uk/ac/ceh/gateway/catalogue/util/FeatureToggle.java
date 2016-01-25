package uk.ac.ceh.gateway.catalogue.util;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FeatureToggle {
    private final boolean impFacetsEnabled;
}