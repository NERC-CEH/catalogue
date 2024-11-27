package uk.ac.ceh.gateway.catalogue.geometry;

import java.util.Optional;

public interface GeometryType {
    Optional<String> getWkt();
    Optional<BoundingBox> getBoundingBox();
}
