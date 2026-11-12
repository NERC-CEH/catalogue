package uk.ac.ceh.gateway.catalogue.people;

import java.util.List;

/**
 * Someone the catalogue already names with an ORCID, as the editor's people look-up offers them: the
 * name most records give, how else they have been written, the organisations they have been named with,
 * most used first, and in how many records. Enough to tell two namesakes apart, which ORCID warns a
 * name alone can't (dri-one #534).
 *
 * @param orcid {@code https://orcid.org/<id>}
 * @param records how many of the records the editor can see name them
 */
public record KnownPerson(
    String orcid,
    String givenName,
    String familyName,
    List<String> otherNames,
    List<Affiliation> affiliations,
    int records
) {
    /** @param identifier its ROR, or blank */
    public record Affiliation(String name, String identifier) {}
}
