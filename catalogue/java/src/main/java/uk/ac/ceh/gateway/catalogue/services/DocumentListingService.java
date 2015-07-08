package uk.ac.ceh.gateway.catalogue.services;

import java.util.List;

/**
 *
 * @author cjohn
 */
public interface DocumentListingService {
    List<String> filterFilenames(List<String> files);
}
