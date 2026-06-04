package uk.ac.ceh.gateway.catalogue.waf;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.gateway.catalogue.TimeConstants;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.postprocess.PostProcessingException;
import uk.ac.ceh.gateway.catalogue.services.MetadataListingService;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GeminiWafService {
    private final MetadataListingService listing;
    private final DataRepository<CatalogueUser> repo;
    private final AtomicReference<List<String>> preFetchWafFiles = new AtomicReference<>();

    public GeminiWafService(
        DataRepository<CatalogueUser> repo,
        MetadataListingService listing
    ) {
        this.repo = repo;
        this.listing = listing;
    }

    @SneakyThrows
    public List<String> getWafFiles() {
        if (preFetchWafFiles.get() == null) {
            preFetchWafFiles.set(getFiles());
        }
        return preFetchWafFiles.get();
    }

    private List<String> getFiles() throws IOException, PostProcessingException {
        List<String> resourceTypes = Arrays.asList("dataset", "service");
        DataRevision<CatalogueUser> latestRevision = repo.getLatestRevision();
        return (latestRevision == null) ? Collections.emptyList() : listing
            .getPublicDocuments(latestRevision.getRevisionID(), GeminiDocument.class, resourceTypes)
            .stream()
            .map((d)-> d + ".xml")
            .collect(Collectors.toList());
    }

    @Scheduled(initialDelay = TimeConstants.ONE_MINUTE*5, fixedDelay = TimeConstants.ONE_DAY)
    @SneakyThrows
    public void fetchFiles() {
        preFetchWafFiles.set(getFiles());
    }
}
