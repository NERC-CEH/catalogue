package uk.ac.ceh.gateway.catalogue.indexing;

/**
 * The following Functional Interface allows document indexing tasks to be
 * represented as java 8 Functional Suppliers.
 * 
 * This class comes in useful when adding around-advice to a call. E.g 
 * performing a task inside a jena transaction.
 * 
 * @see java.util.function.Supplier
 * @param <T> The return type of this supplier
 */ 
@FunctionalInterface
public interface DocumentIndexingSupplier<T> {
   T get() throws DocumentIndexingException;
}
