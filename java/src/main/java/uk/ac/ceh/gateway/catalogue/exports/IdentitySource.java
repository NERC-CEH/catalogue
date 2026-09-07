package uk.ac.ceh.gateway.catalogue.exports;

/**
 * An authority describing a person or an organisation the catalogue names.
 *
 * <p>A marker over {@link AuthoritySource}, as {@link ReferenceSource} is, and
 * with the same lifespan: it exists so {@link IdentityGraphService} can be
 * handed exactly its own sources, and goes when the graph services are
 * collapsed into one.
 */
public interface IdentitySource extends AuthoritySource {
}
