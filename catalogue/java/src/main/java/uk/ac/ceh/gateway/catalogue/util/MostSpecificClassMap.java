package uk.ac.ceh.gateway.catalogue.util;

import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * The following class allows us to register a class to instance mapping. The 
 * registered instance can be looked up by either the original class or one 
 * which is a sub class of that registered.
 * 
 * This implementation will favour the most specific compatible instances which
 * have been registered. 
 * @author cjohn
 * @param <T> type of elements the classes are mapped to
 */
public class MostSpecificClassMap<T> implements ClassMap<T> {
    private final Multiset<Class<?>> clazzes;
    private final Map<Class<?>, T> lookup;
    
    public MostSpecificClassMap() {
        this.clazzes = TreeMultiset.create(new ClassHierarchyComparator());
        this.lookup = new HashMap<>();
    }
    
    /**
     * Returns the an instance which has been mapped to the given class. The 
     * class lookup. The instance which gets returned map have been mapped to a 
     * super class of the one specified. Given the following class hierarchy and
     * registration:
     * 
     *   Object       classMap.register(A.class, "Alice")
     *     |                  .register(B.class, "Bob");
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
        for(Class<?> curr: clazzes) {
            if(curr.isAssignableFrom(clazz)) {
                return lookup.get(curr);
            }
        }
        return null;
    }
    
    @Override
    public MostSpecificClassMap<T> register(Class<?> clazz, T instance) {
        clazzes.add(clazz);
        lookup.put(clazz, instance);
        return this;
    }

    private static class ClassHierarchyComparator implements Comparator<Class<?>> {
        @Override
        public int compare(Class<?> o1, Class<?> o2) {
            return depth(o2) - depth(o1);
        }
        
        private int depth(Class<?> clazz) {
            Class<?> superClass = clazz.getSuperclass();
            if(superClass != null) {
                return depth(superClass) + 1;
            }
            return 0;
        }
    }
}
