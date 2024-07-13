package uk.ac.ceh.gateway.catalogue.repository;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

@Value
@AllArgsConstructor
/**
 * Facilities can cease belonging to a network when either the facility is deleted or has 'belongTo' relationships
 * removed.  Both situations use this event to alert a listener.  This event contains two pieces of information
 * for the listener:
 * - the id of the facility that has changed
 * - a list of the monitoring network ids that are no longer referenced by this facility
 *
 */
public class FacilityBelongToRemovedEvent {
    private final String facilityId;
    private final List<String> belongToFilenames;
}
