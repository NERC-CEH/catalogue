package uk.ac.ceh.gateway.catalogue.exports;

/**
 * An authority describing its own concepts.
 *
 * <p>A marker over {@link AuthoritySource}, as {@link ReferenceSource} and
 * {@link IdentitySource} are, so {@link VocabularyGraphService} can be handed
 * exactly the vocabularies that fetch. The four label-only authorities — GEMET,
 * EnvThes, research activities and FDRI — have no source, because their labels
 * come from the Solr keyword harvest and cost no network request.
 */
public interface VocabularySource extends AuthoritySource {
}
