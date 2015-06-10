package uk.ac.ceh.gateway.catalogue.services;

import java.util.Collection;
import java.util.List;

/**
 *
 * @author cjohn
 */
public interface DocumentListingService {
    List<String> filterFilenames(Collection<String> files);
}
