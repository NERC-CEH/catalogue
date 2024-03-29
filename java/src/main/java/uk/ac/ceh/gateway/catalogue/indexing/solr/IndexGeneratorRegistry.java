package uk.ac.ceh.gateway.catalogue.indexing.solr;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ceh.gateway.catalogue.indexing.DocumentIndexingException;
import uk.ac.ceh.gateway.catalogue.indexing.IndexGenerator;
import uk.ac.ceh.gateway.catalogue.indexing.ClassMap;

@Slf4j
@ToString(onlyExplicitlyIncluded = true)
public class IndexGeneratorRegistry<D, I> implements IndexGenerator<D, I> {
    private final ClassMap<IndexGenerator<?, I>> lookup;

    public IndexGeneratorRegistry(ClassMap<IndexGenerator<?, I>> lookup) {
        this.lookup = lookup;
        log.info("Creating {}", this);
    }

    @Override
    public I generateIndex(D toIndex) throws DocumentIndexingException {
        @SuppressWarnings("unchecked")
        IndexGenerator<D, I> indexGenerator = (IndexGenerator<D, I>) lookup.get(toIndex.getClass());
        if(indexGenerator == null) {
            throw new DocumentIndexingException("No Index Generator registered to handle " + toIndex);
        }
        return indexGenerator.generateIndex(toIndex);
    }
}
