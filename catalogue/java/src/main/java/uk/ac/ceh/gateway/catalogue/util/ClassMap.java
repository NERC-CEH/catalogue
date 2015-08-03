package uk.ac.ceh.gateway.catalogue.util;

/**
 * This class is used for getting a specific instance based upon a given class
 * @author cjohn
 * @param <T> The type of instance which should be found for a class
 * @see PrioritisedClassMap
 */
public interface ClassMap<T> {
    T get(Class<?> clazz);
    
    ClassMap<T> register(Class<?> clazz, T instance);
}
