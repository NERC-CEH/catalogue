package uk.ac.ceh.gateway.catalogue.model;

import lombok.NonNull;
import lombok.Value;

@Value
public class Relationship {
    private final @NonNull String relation, target;
}
