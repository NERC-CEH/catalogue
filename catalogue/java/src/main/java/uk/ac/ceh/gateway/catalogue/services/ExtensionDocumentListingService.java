package uk.ac.ceh.gateway.catalogue.services;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.io.FilenameUtils;

/**
 * Defines a listing service which will filter a list of filenames into a list
 * of document names if there exists filenames with the given extensions
 * @author cjohn
 */
@Data
@AllArgsConstructor
public class ExtensionDocumentListingService implements DocumentListingService {
    private final List<String> extensions;
    
    public ExtensionDocumentListingService() {
        this(Arrays.asList("meta", "raw"));
    }
    
    @Override
    public List<String> filterFilenames(Collection<String> files) {
        //Scan through the files list return any which there exists a .meta and .raw
        //file
        return files.stream()
                .map((f) -> new Filename(f))
                .collect(Collectors.groupingBy(Filename::getName))
                .entrySet()
                .stream()
                .filter((e) -> e.getValue().stream()
                                            .map((f)-> f.getExtension())
                                            .collect(Collectors.toList())
                                            .containsAll(extensions))
                .map((e) -> e.getKey())
                .collect(Collectors.toList());  
    }
    
    @Data
    private static class Filename {
        private final String filename;
        
        public String getExtension() {
            return FilenameUtils.getExtension(filename);
        }
        
        public String getName() {
            return FilenameUtils.removeExtension(filename);
        }
    }
}
