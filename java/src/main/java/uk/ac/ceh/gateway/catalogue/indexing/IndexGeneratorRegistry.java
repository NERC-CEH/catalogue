package uk.ac.ceh.gateway.catalogue.indexing;

import lombok.AllArgsConstructor;
import uk.ac.ceh.gateway.catalogue.util.ClassMap;

@AllArgsConstructor
public class IndexGeneratorRegistry<D, I> implements IndexGenerator<D, I> {
    private final ClassMap<IndexGenerator<?, I>> lookup;
        
    @Override
    public I generateIndex(D toIndex) throws DocumentIndexingException {
        IndexGenerator<D, I> indexGenerator = (IndexGenerator<D, I>) lookup.get(toIndex.getClass());
        if(indexGenerator == null) {
            throw new DocumentIndexingException("No Index Generator registered to handle " + toIndex);
        }
        return indexGenerator.generateIndex(toIndex);
    }
}
