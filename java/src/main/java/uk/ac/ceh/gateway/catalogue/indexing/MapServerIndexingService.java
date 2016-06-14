package uk.ac.ceh.gateway.catalogue.indexing;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import org.apache.commons.io.FileUtils;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.services.BundledReaderService;
import uk.ac.ceh.gateway.catalogue.services.DocumentListingService;

/**
 * The following indexing service is responsible for producing Map Server MapFiles
 * in a specified directory. 
 * @author cjohn
 * @param <D> Document type which a map service can be created from
 */
public class MapServerIndexingService<D extends MetadataDocument> extends AbstractIndexingService<D, MapFile> {
    private static final String MAP_FILE_EXTENSION = ".map";
    
    private final File mapFiles;

    public MapServerIndexingService(
            BundledReaderService<D> reader, 
            DocumentListingService listingService, 
            DataRepository<?> repo,
            IndexGenerator<D, MapFile> indexGenerator,
            File mapFiles) {
        super(reader, listingService, repo, indexGenerator);
        this.mapFiles = mapFiles;
    }

    @Override
    protected void clearIndex() throws DocumentIndexingException {
        try {
            FileUtils.cleanDirectory(mapFiles);
        } catch (IOException ex) {
            throw new DocumentIndexingException("Failed to clean map file directory", ex);
        }
    }

    @Override
    protected void index(MapFile toIndex) throws Exception {
        if(toIndex != null) {
            File mapFile = getMapFileLocation(toIndex.getDocument().getId());
            toIndex.writeTo(new FileWriter(mapFile));
        }
    }

    @Override
    public boolean isIndexEmpty() throws DocumentIndexingException {
        return mapFiles.list(new MapFileFilenameFilter()).length == 0;
    }

    @Override
    public void unindexDocuments(List<String> unIndex) throws DocumentIndexingException {
        for(String mapFile: unIndex) {
            FileUtils.deleteQuietly(getMapFileLocation(mapFile));
        }
    }
    
    private File getMapFileLocation(String id) {
        return new File(mapFiles, id + MAP_FILE_EXTENSION);
    }
    
    private static class MapFileFilenameFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(MAP_FILE_EXTENSION);
        }
    }
}