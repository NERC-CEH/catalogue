package uk.ac.ceh.gateway.catalogue.services;

import lombok.Data;
import org.apache.commons.io.FilenameUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Defines a listing service which will filter a list of filenames into a list
 * of document names if there exists filenames with the given extensions
 */
public class ExtensionDocumentListingService implements DocumentListingService {
    private final List<String> extensions = Arrays.asList("meta", "raw");
    
    @Override
    public List<String> filterFilenames(Collection<String> files) {
        //Scan through the files list, return any which there exists a .meta and .raw
        //file
        return filterFilenames(files,(e) -> e.getValue().stream()
                                              .map((f)-> f.getExtension())
                                              .collect(Collectors.toList())
                                              .containsAll(extensions));
    }

    @Override
    public List<String> filterFilenamesEitherExtension(Collection<String> files) {
      //Scan through the files list, return any which there exists a .meta or .raw
      //file
      return filterFilenames(files,(e) -> {
          List<String> exts = e.getValue().stream()
                                        .map((f)-> f.getExtension())
                                        .collect(Collectors.toList());
          return exts.contains("meta") || exts.contains("raw");
        }
      );
    }

    private List<String> filterFilenames(Collection<String> files, Predicate<Map.Entry<String,List<Filename>>> filter) {
          return files.stream()
                  .map((f) -> new Filename(f))
                  .collect(Collectors.groupingBy(Filename::getName))
                  .entrySet()
                  .stream()
                  .filter(filter)
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
