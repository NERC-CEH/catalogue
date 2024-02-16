package uk.ac.ceh.gateway.catalogue.indexing.mapserver;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.document.DocumentListingService;
import uk.ac.ceh.gateway.catalogue.document.reading.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.gemini.GeminiDocument;
import uk.ac.ceh.gateway.catalogue.indexing.AbstractIndexingService;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.IndexGenerator;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * The following indexing service is responsible for producing Map Server MapFiles
 * in a specified directory.
 */
@Slf4j
@ToString(callSuper = true)
public class MapServerIndexingService extends AbstractIndexingService<MetadataDocument, MapFile> {
    private static final Pattern MAP_FILE_PATTERN = Pattern.compile("_.*\\.map$");
    private static final String MAP_FILE_EXTENSION = ".map";
    private static final String FALLBACK_PROJECTION = "default";

    private final File mapFiles;

    public MapServerIndexingService(
            BundledReaderService<MetadataDocument> reader,
            DocumentListingService listingService,
            DataRepository<CatalogueUser> repo,
            IndexGenerator<MetadataDocument, MapFile> indexGenerator,
            @Qualifier("mapsLocation") File mapFiles
    ) {
        super(reader, listingService, repo, indexGenerator);
        this.mapFiles = mapFiles;
        log.info("Creating {}", this);
    }

    @Override
    protected void clearIndex() {
        Arrays.stream(Objects.requireNonNull(mapFiles.listFiles(new MapFileFilenameFilter())))
            .forEach(FileUtils::deleteQuietly);
    }

    @Override
    protected boolean canIndex(MetadataDocument doc) {
        if (doc instanceof GeminiDocument gemini) {
            if (gemini.getType().equals("service")) {
                val possibleMapDataDefinition = Optional.ofNullable(gemini.getMapDataDefinition());
                if (possibleMapDataDefinition.isPresent()) {
                    val mapDataDefinition = possibleMapDataDefinition.get();
                    val possibleData = Optional.ofNullable(mapDataDefinition.getData());
                    if (possibleData.isPresent()) {
                        val data = possibleData.get();
                        return !data.isEmpty();
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected void index(MapFile toIndex) throws Exception {
        if(toIndex != null) {
            String id = toIndex.getDocument().getId();
            //If there is only one source projection system used for this mapfile
            //then there is no need to create custom projection system map files.
            //Only generate the default fallback .map file
            if(toIndex.getProjectionSystems().size() != 1) {
                for(String epsgCode: toIndex.getProjectionSystems()) {
                    File fileLocation = getMapFileLocation(id, epsgCode);
                    toIndex.writeTo(epsgCode, new FileWriter(fileLocation));
                    changeFilePermissions(fileLocation.getPath());
                }
            }
            File mapFileLocation = getMapFileLocation(id, FALLBACK_PROJECTION);
            toIndex.writeTo(null, new FileWriter(mapFileLocation));
            changeFilePermissions(mapFileLocation.getPath());
        }
    }

    private void changeFilePermissions(String filename) {
        // Set the file permissions to allow mapserver to read them
        try {
            Runtime.getRuntime().exec(new String[]{format("chmod 644 %s", filename)});
        } catch (IOException e) {
            throw new RuntimeException(format("Cannot change file permissions for: %s", filename), e);
        }
    }

    @Override
    public boolean isIndexEmpty() throws DocumentIndexingException {
        return getIndexedFiles().isEmpty();
    }

    @Override
    public void unindexDocuments(List<String> unIndex) throws DocumentIndexingException {
        for(String indexed: unIndex) {
            Arrays.stream(Objects.requireNonNull(mapFiles.listFiles(new MapFileFilenameFilter())))
                .filter((f) -> f.getName().startsWith(indexed))
                .filter((f) -> MAP_FILE_PATTERN.matcher(f.getName().substring(indexed.length())).matches())
                .forEach(FileUtils::deleteQuietly);
        }
    }

    /* Make sure that we remove the index of a document before perform a new index*/
    @Override
    public void indexDocuments(List<String> documents, String revision) throws DocumentIndexingException {
        unindexDocuments(documents);
        super.indexDocuments(documents, revision);
    }

    /**
     * Returns the list of ids which have been indexed by this indexing service
     * @return a list of ids indexed
     */
    public List<String> getIndexedFiles() {
        return Arrays.stream(Objects.requireNonNull(mapFiles.listFiles(new MapFileFilenameFilter())))
                .map(File::getName)
                .map((f) -> f.substring(0, f.lastIndexOf('_')))
                .distinct()
                .collect(Collectors.toList());
    }

    private File getMapFileLocation(String id, String projection) {
        return new File(mapFiles, id + "_" + projection + MAP_FILE_EXTENSION);
    }

    private static class MapFileFilenameFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            return MAP_FILE_PATTERN.matcher(name).find();
        }
    }
}
