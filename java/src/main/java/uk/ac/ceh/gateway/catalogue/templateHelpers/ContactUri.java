package uk.ac.ceh.gateway.catalogue.templateHelpers;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.val;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;

import java.text.Normalizer;

/**
 * Decides which RDF node a contact is, and returns it ready to emit as Turtle.
 *
 * <p>Before this existed, {@code templates/rdf/_macros.ftl} keyed a contact on
 * the record and the contact's position within it — {@code :<recordId>_a17} — so
 * a researcher got a brand-new node for every record they appeared on, and their
 * identity changed if someone reordered the author list. A production audit
 * (dri-one #319) found 12,815 {@code foaf:Person} nodes standing for fewer than
 * 3,000 people: Wood, C. alone held 167 datasets across 65 nodes, and Dodd, B.
 * had 74 datasets across 74 nodes. Authorship was not queryable.
 *
 * <p>Precedence, most trustworthy identifier first:
 * <ol>
 *   <li>an ORCID, canonicalised by {@link UriNormaliser}</li>
 *   <li>an ISNI — {@code ResponsibleParty.isIsni()} existed but was called from
 *       nowhere, so a depositor who supplied one still got a throwaway node</li>
 *   <li>a ROR, for a contact that is an organisation rather than a person</li>
 *   <li>a node minted from the person's own name, stable across records</li>
 *   <li>the record-scoped node, for a contact with nothing to identify it</li>
 * </ol>
 *
 * <h2>What the name-derived node does and does not claim</h2>
 *
 * <p>The key is the contact's {@code foaf:name} reduced to its letters and
 * digits, so {@code "Wood, C.M."}, {@code "Wood, C. M."} and {@code "wood,c.m."}
 * converge, and an accent typed two ways in two records does not fork the
 * person. It deliberately goes no further. Two people who publish under the same
 * name <em>are</em> merged: production holds a {@code Turner, S.} who is both
 * Simon and Stephen, and a {@code Savage, J.} who is both James and Joanna.
 * Accepting that is the point of #319 — the alternative measured on the same
 * data (name plus organisation) leaves Wood, C. split five ways, because one
 * person's organisation is variously "Centre for Ecology &amp; Hydrology",
 * "UK Centre for Ecology &amp; Hydrology" and "University of the West of
 * England". The fix for a wrong merge is the ORCID the record should carry.
 *
 * <p>The key is hashed rather than slugged (see {@link MintedNode}). A name is
 * not URI-safe — spaces, commas, apostrophes, accents, arbitrary length — and a
 * readable slug invites consumers to parse the URI as an assertion about the
 * name. {@code foaf:name} sits on the node itself, so nothing is lost by making
 * the identifier opaque. At the ~3,400 distinct names in production the chance
 * of a hash collision is about 3e-13, and a collision would merge two people:
 * the same failure the key already accepts for shared names.
 *
 * <p>Note that a person's name is not a lookup: the templates render one record
 * at a time and cannot see the rest of the corpus, so the node has to be a pure
 * function of the fields on the contact in front of us.
 */
@Service
@ToString
@RequiredArgsConstructor
public class ContactUri {

    /** Local-name prefix, so a minted node is recognisable as one in the store. */
    private static final String PERSON_PREFIX = ":person_";

    /** @see #identifyOrganisation */
    private static final String ORGANISATION_PREFIX = ":organisation_";

    /** @see #identifyRole */
    private static final String ROLE_PREFIX = ":role_";

    private final UriNormaliser uriNormaliser;

    /**
     * @param contact  the contact to identify
     * @param recordId the id of the record being rendered, for the fallback node
     * @param prefix   the contact's role in that record ({@code a}, {@code c},
     *                 {@code pub}), for the fallback node
     * @param index    the contact's position in its list, for the fallback node
     * @return a Turtle node: an {@code <IRI>} or a prefixed name, never blank
     */
    public String identify(ResponsibleParty contact, String recordId, String prefix, int index) {
        val recordScoped = ":" + recordId + "_" + prefix + index;

        if (contact.getFullName().isBlank()) {
            return contact.isRor()
                ? iriOrElse(contact.getOrganisationIdentifier(), recordScoped)
                : recordScoped;
        }

        if (contact.isOrcid() || contact.isIsni()) {
            return iriOrElse(contact.getNameIdentifier(), personNode(contact, recordScoped));
        }
        return personNode(contact, recordScoped);
    }

    /**
     * A URI we recognise but cannot emit (see {@link UriNormaliser}) must not
     * cost the contact its identity, so it falls through rather than failing.
     */
    private String iriOrElse(String uri, String fallback) {
        val canonical = uriNormaliser.normalise(uri);
        return canonical.isEmpty() ? fallback : "<" + canonical + ">";
    }

    private String personNode(ResponsibleParty contact, String fallback) {
        val key = nameKey(contact.getFullName());
        return key.isEmpty() ? fallback : MintedNode.from(PERSON_PREFIX, key);
    }

    /**
     * Identifies the organisation a contact says they belong to, where that
     * organisation has no ROR.
     *
     * <p>{@link #identify} already resolves a ROR to the ROR node itself. The
     * fallback — a contact whose affiliation is only a typed name — used to be
     * emitted as {@code foaf:member [foaf:name "..."]}, a blank node. A
     * production audit (dri-one #334) found 3,128 of them standing for 774
     * distinct names: the University of Exeter appeared 91 times as 91
     * unrelated nodes, so no query could ask which datasets an institution
     * holds.
     *
     * <p>Keyed on the organisation's name by the same rule as a person's, and
     * with the same limitation: {@code "University of Edinburgh"} and
     * {@code "The University of Edinburgh"} are two institutions as far as this
     * is concerned, as are {@code "Institute of Terrestrial Ecology"} and the
     * {@code "UK Centre for Ecology & Hydrology"} it was renamed to. Minting
     * makes those pairs visible and joinable; reconciling them is data cleanup,
     * and the durable fix is the ROR the record should carry.
     *
     * @param contact the contact whose stated affiliation to identify
     * @return a prefixed name, or blank where the contact names no organisation
     */
    public String identifyOrganisation(ResponsibleParty contact) {
        val key = nameKey(contact.getOrganisationName());
        return key.isEmpty() ? "" : MintedNode.from(ORGANISATION_PREFIX, key);
    }

    /**
     * Identifies the {@code pro:RoleInTime} tying a contact to the role they
     * held on one record.
     *
     * <p>Emitted as a blank node when the role vocabulary arrived (dri-one
     * #323). A {@code RoleInTime} is a reified statement — person, role,
     * record — and reification exists so the statement can be pointed at,
     * which a blank node is precisely what prevents. Nothing of it had reached
     * production before #334, because {@code doiRoleUri} fires on essentially
     * every author: the first export would have added roughly 16,000 blank
     * nodes to the ~15,600 already there.
     *
     * <p>Unlike the other minted nodes this one needs no normalisation. Its key
     * is three identifiers that are themselves already canonical, so the node
     * is stable by construction, and two identical role statements on one
     * record converge rather than forking.
     *
     * @param contactNode the Turtle node {@link #identify} returned for the contact
     * @param roleUri     the Turtle node for the role, from {@code doiRoleUri}
     * @param recordId    the id of the record the role was held on
     * @return a prefixed name
     */
    public String identifyRole(String contactNode, String roleUri, String recordId) {
        return MintedNode.from(ROLE_PREFIX, contactNode, roleUri, recordId);
    }

    /**
     * Reduces a name to the part of it we treat as identifying. Decomposing to
     * NFKD first turns an accented character into its base letter followed by a
     * combining mark, and a mark is not a letter — so keeping only letters and
     * digits folds accents, drops punctuation and spacing, and leaves non-Latin
     * scripts intact, in one pass.
     */
    private static String nameKey(String fullName) {
        val decomposed = Normalizer.normalize(fullName, Normalizer.Form.NFKD);
        val key = new StringBuilder(decomposed.length());
        decomposed.codePoints()
            .filter(Character::isLetterOrDigit)
            .forEach(codePoint -> key.appendCodePoint(Character.toLowerCase(codePoint)));
        return key.toString();
    }
}
