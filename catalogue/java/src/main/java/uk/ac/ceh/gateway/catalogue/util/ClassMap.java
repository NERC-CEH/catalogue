package uk.ac.ceh.gateway.catalogue.util;

/**
 *
 * @author cjohn
 * @param <T>
 */
public interface ClassMap<T> {
    T get(Class<?> clazz);
    
    ClassMap<T> register(Class<?> clazz, T instance);
}
