package uk.ac.ceh.gateway.catalogue.util;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * The following class allows us to register a class to instance mapping. The 
 * registered instance can be looked up by either the original class or one 
 * which is a sub class of that registered.
 * 
 * This implementation will favour the first compatible instance which has been 
 * registered. 
 * @author cjohn
 * @param <T> type of elements the classes are mapped to
 */
public class PrioritisedClassMap<T> implements ClassMap<T> {
    private final List<ClassMapping> lookup;
    
    public PrioritisedClassMap() {
        this.lookup = new ArrayList<>();
    }
    
    /**
     * Returns the an instance which has been mapped to the given class. The 
     * class lookup. The instance which gets returned map have been mapped to a 
     * super class of the one specified. Given the following class hierarchy and
     * registration:
     * 
     *   Object       classMap.register(B.class, "Bob")
     *     |                  .register(A.class, "Alice");
     *     A
     *    / \
     *   B   C
     * 
     * The following calls will be true:
     * 
     *    classMap.get(A.class) -> "Alice";
     *    classMap.get(C.class) -> "Alice";
     *    classMap.get(B.class) -> "Bob";
     * 
     * @param clazz to lookup
     * @return instance mapped to the class (or a super class of it) or null if
     *  no match can be made.
     */
    @Override
    public T get(Class<?> clazz) {
        for(ClassMapping curr: lookup) {
            if(curr.clazz.isAssignableFrom(clazz)) {
                return curr.getInstance();
            }
        }
        return null;
    }
    
    @Override
    public PrioritisedClassMap<T> register(Class<?> clazz, T instance) {
        lookup.add(new ClassMapping(clazz, instance));
        return this;
    }
    
    @Data
    private class ClassMapping {
        private final Class<?> clazz;
        private final T instance;
    }
}
