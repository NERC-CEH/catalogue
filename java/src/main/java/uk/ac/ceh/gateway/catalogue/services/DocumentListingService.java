package uk.ac.ceh.gateway.catalogue.services;

import java.util.Collection;
import java.util.List;

public interface DocumentListingService {
    List<String> filterFilenames(Collection<String> files);
    List<String> filterFilenamesEitherExtension(Collection<String> files);
}
