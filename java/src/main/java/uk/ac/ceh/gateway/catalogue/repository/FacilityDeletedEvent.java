package uk.ac.ceh.gateway.catalogue.repository;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class FacilityDeletedEvent {
    private final String facilityId;
    private final List<String> belongToFilenames;
}
