package uk.ac.ceh.gateway.catalogue.templateHelpers;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.val;
import org.springframework.stereotype.Service;
import uk.ac.ceh.gateway.catalogue.model.ResponsibleParty;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.HexFormat;

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
 * <p>The key is hashed rather than slugged. A name is not URI-safe — spaces,
 * commas, apostrophes, accents, arbitrary length — and a readable slug invites
 * consumers to parse the URI as an assertion about the name. {@code foaf:name}
 * sits on the node itself, so nothing is lost by making the identifier opaque.
 *
 * <p>Note that a person's name is not a lookup: the templates render one record
 * at a time and cannot see the rest of the corpus, so the node has to be a pure
 * function of the fields on the contact in front of us.
 */
@Service
@ToString
@RequiredArgsConstructor
public class ContactUri {

    /**
     * 64 bits of SHA-256. At the ~3,400 distinct names in production the chance
     * of any collision is about 3e-13; a collision would merge two people, the
     * same failure the key already accepts for shared names.
     */
    private static final int HASH_BYTES = 8;

    /** Local-name prefix, so a minted node is recognisable as one in the store. */
    private static final String PERSON_PREFIX = ":person_";

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
        return key.isEmpty() ? fallback : PERSON_PREFIX + hash(key);
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

    private static String hash(String key) {
        val digest = sha256().digest(key.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest, 0, HASH_BYTES);
    }

    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is required of every JVM", ex);
        }
    }
}
