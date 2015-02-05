package uk.ac.ceh.gateway.catalogue.indexing;

import static java.lang.String.format;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import uk.ac.ceh.components.datastore.DataRepository;
import uk.ac.ceh.components.datastore.DataRepositoryException;
import uk.ac.ceh.components.datastore.DataRevision;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.model.Permission;

public class ViewIndexer {
    private final DataRepository<CatalogueUser> repo;

    public ViewIndexer(DataRepository<CatalogueUser> repo) {
        this.repo = repo;
    }
    
    public List<String> index(MetadataDocument document) {
        Objects.requireNonNull(document);
        List<String> toReturn = getViews(document);        
        getAuthor(document).ifPresent(a -> toReturn.add(a));
        return toReturn;
    }
    
    private List<String> getViews(MetadataDocument document) {
        Optional<List<String>> possible = Optional.ofNullable(document)
            .map(MetadataDocument::getMetadata)
            .map(m -> { return m.getIdentities(Permission.VIEW); });
        
        if (possible.isPresent() && !possible.get().isEmpty()) {
            return possible.get();
        } else {
            return new ArrayList<>();
        }
    }
    
    private Optional<String> getAuthor(MetadataDocument document) {
        try {
            return repo.getRevisions(format("%s.meta", document.getId()))
                .stream()
                .findFirst()
                .map(DataRevision<CatalogueUser>::getAuthor)
                .map(CatalogueUser::getUsername)
                .map(String::toLowerCase);
        } catch (DataRepositoryException ex) {
            return Optional.empty();
        }
    }
}