package uk.ac.ceh.gateway.catalogue.people;

import java.util.List;

/**
 * A person as their own ORCID record names them. A name the person keeps private comes back blank:
 * the iD still exists, ORCID just won't say whose it is.
 *
 * @param orcid {@code https://orcid.org/<id>}
 * @param creditName the name they publish under, where it differs
 */
public record OrcidPerson(String orcid, String givenNames, String familyName, String creditName, List<String> otherNames) {}
