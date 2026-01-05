package uk.ac.ceh.gateway.catalogue.datapreviewer;

import com.google.common.collect.Multimap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.model.MetadataInfo;
import uk.ac.ceh.gateway.catalogue.model.Permission;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;
import uk.ac.ceh.gateway.catalogue.templateHelpers.JenaLookupService;
import uk.ac.ceh.gateway.catalogue.indexing.jena.Ontology;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.HubbubResponse;
import uk.ac.ceh.gateway.catalogue.upload.hubbub.UploadService;

import java.io.File;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DataPreviewerService {

    private final UploadService uploadService;
    private final DocumentRepository documentRepository;
    private final JenaLookupService jenaLookupService;

    private static final String OPEN_ACCESS_URI =
        "http://purl.org/coar/access_right/c_abf2";

    public Object preview(String id) throws DocumentRepositoryException {

        GeminiDocument doc = (GeminiDocument) documentRepository.read(id);

        if (doc == null) {
            throw new IllegalArgumentException("Document not found: " + id);
        }

        return switch (doc.getType()) {
            case "dataset" -> previewDataset(doc);
            case "aggregate" -> previewCollection(doc);
            default -> throw new UnsupportedOperationException(
                "Unsupported document type for preview: " + doc.getType()
            );
        };
    }

    private DatasetPreviewResponse previewDataset(GeminiDocument dataset) {

        enforcePublicAccess(dataset);

        List<PreviewDatasetFile> files = fetchDatasetFiles(dataset.getId());
        Map<String, String> observedProperties =
            extractObservedProperties(dataset);

        return new DatasetPreviewResponse(
            "dataset",
            dataset.getId(),
            dataset.getTitle(),
            observedProperties,
            files
        );
    }

    private CollectionPreviewResponse previewCollection(
        GeminiDocument collection
    ) throws DocumentRepositoryException {

        String collectionUri = collection.getUri();

        if (collectionUri == null || collectionUri.isBlank()) {
            throw new IllegalStateException("Collection has no URI");
        }

        List<String> datasetIds =
            resolveCollectionDatasetIds(collectionUri);

        Map<String, String> aggregatedObservedProperties =
            new LinkedHashMap<>();

        List<CollectionPreviewResponse.DatasetEntry> datasets =
            new ArrayList<>();

        for (String datasetId : datasetIds) {

            GeminiDocument dataset =
                (GeminiDocument) documentRepository.read(datasetId);

            if (dataset == null) {
                continue;
            }

            try {
                enforcePublicAccess(dataset);
            } catch (SecurityException e) {
                continue;
            }

            extractObservedProperties(dataset)
                .forEach(aggregatedObservedProperties::putIfAbsent);

            List<PreviewDatasetFile> files =
                fetchDatasetFiles(datasetId);

            datasets.add(
                new CollectionPreviewResponse.DatasetEntry(
                    dataset.getId(),
                    dataset.getTitle(),
                    files
                )
            );
        }

        return new CollectionPreviewResponse(
            "aggregate",
            collection.getId(),
            collection.getTitle(),
            aggregatedObservedProperties,
            datasets
        );
    }

    private void enforcePublicAccess(GeminiDocument document) {

        MetadataInfo metadataInfo = document.getMetadata();
        Multimap<Permission, String> permissions =
            metadataInfo.getPermissions();

        boolean hasPublicView =
            permissions.containsEntry(Permission.VIEW, "public");

        boolean isAvailable =
            "Available".equalsIgnoreCase(document.getResourceStatus());

        boolean isFreelyAvailable =
            document.getAccessLimitation() != null &&
                OPEN_ACCESS_URI.equals(
                    document.getAccessLimitation().getUri()
                );

        if (!(hasPublicView && isAvailable && isFreelyAvailable)) {
            throw new SecurityException("Dataset not publicly accessible");
        }
    }

    private List<PreviewDatasetFile> fetchDatasetFiles(String datasetId) {
        HubbubResponse hubbubResponse =
            uploadService.get(datasetId, "eidchub", 1, 10000);

        if (hubbubResponse == null || hubbubResponse.getData() == null) {
            return List.of();
        }

        return hubbubResponse.getData().stream()
            .map(f -> new PreviewDatasetFile(
                new File(f.getPath()).getName(),
                f.getPath(),
                f.getBytes() != null ? f.getBytes() : 0L,
                f.getMimeType()
            ))
            .toList();
    }

    private Map<String, String> extractObservedProperties(
        GeminiDocument dataset
    ) {

        Map<String, String> result = new LinkedHashMap<>();

        if (dataset.getFileset() == null) {
            return result;
        }

        dataset.getFileset().forEach(fs -> {
            if (fs.getObservedProperty() == null) return;

            fs.getObservedProperty().forEach(prop -> {
                if (prop.getValue() != null && prop.getTitle() != null) {
                    result.putIfAbsent(
                        prop.getValue().trim(),
                        prop.getTitle().trim()
                    );
                }
            });
        });

        return result;
    }


    private List<String> resolveCollectionDatasetIds(String collectionUri) {

        return jenaLookupService
            .inverseRelationships(collectionUri, Ontology.EIDC_MEMBER_OF.getURI())
            .stream()
            .map(link -> extractIdFromUri(link.getHref()))
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    }

    private String extractIdFromUri(String uri) {
        if (uri == null) {
            return null;
        }
        return uri.substring(uri.lastIndexOf('/') + 1);
    }
}
