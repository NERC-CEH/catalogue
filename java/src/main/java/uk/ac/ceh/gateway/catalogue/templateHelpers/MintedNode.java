package uk.ac.ceh.gateway.catalogue.templateHelpers;

import lombok.val;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Builds the opaque local name behind a minted RDF node.
 *
 * <p>Several helpers in this package identify a thing the catalogue holds no
 * external identifier for — a person ({@link ContactUri}), a grant ({@link
 * FundingUri}), a licence ({@link LicenceUri}), a file format ({@link
 * FormatUri}) — by hashing whatever the record does say about it. Each carried
 * its own private copy of the same three lines; this is that code, once.
 *
 * <p>Hashing rather than slugging is deliberate throughout. The keys are names,
 * grant numbers and free text: not URI-safe, of unbounded length, and a readable
 * slug would invite consumers to parse the identifier as an assertion about the
 * key. Every minted node carries the readable form as a literal instead.
 */
final class MintedNode {

    /**
     * 64 bits of SHA-256. At the scale of one catalogue — a few thousand
     * distinct keys per node type — the chance of any collision is around
     * 1e-13. Where a collision would actually matter it is discussed on the
     * caller: see {@link ContactUri}'s note on merged names.
     */
    private static final int HASH_BYTES = 8;

    /**
     * Separates the parts of a composite key. A NUL cannot occur in any of the
     * strings keyed on here, so {@code ("ab", "c")} and {@code ("a", "bc")}
     * cannot hash alike — the ambiguity plain concatenation would allow.
     */
    private static final String KEY_SEPARATOR = "\0";

    private MintedNode() {
    }

    /**
     * @param prefix   the node's Turtle prefixed-name stem, e.g. {@code ":person_"},
     *                 so a minted node is recognisable as one in the store
     * @param keyParts the values the node's identity derives from, in a fixed
     *                 order, already normalised by the caller — which owns the
     *                 question of what counts as the same key
     * @return the prefixed name to emit
     */
    static String from(String prefix, String... keyParts) {
        return prefix + hash(String.join(KEY_SEPARATOR, keyParts));
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
