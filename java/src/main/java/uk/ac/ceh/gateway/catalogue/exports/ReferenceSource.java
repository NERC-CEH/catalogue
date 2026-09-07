package uk.ac.ceh.gateway.catalogue.exports;

/**
 * An authority describing something the catalogue's records cite — a paper, a
 * grant, a place, a monitoring site.
 *
 * <p>A marker over {@link AuthoritySource}, so {@link ReferenceGraphService} can
 * be handed exactly the sources it publishes without matching on graph names. It
 * exists only until the three graph services are collapsed into one, at which
 * point it and its two siblings go with them.
 */
public interface ReferenceSource extends AuthoritySource {
}
