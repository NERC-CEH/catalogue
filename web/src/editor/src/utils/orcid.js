/*
 * ORCID iDs as editors type them (dri-one #534). An iD can be pasted bare, with or without a scheme,
 * or with a lower-case x; it is stored as https://orcid.org/…, the form records use and ORCID asks for.
 * Its last character is an ISO 7064 MOD 11-2 checksum of the other fifteen digits, so a mistyped or
 * transposed digit is caught as it is typed rather than saved as somebody else's iD.
 */
export const ORCID_PREFIX = 'https://orcid.org/'

const ORCID_FORM = /^(?:(?:https?:\/\/)?(?:www\.)?orcid\.org\/)?(\d{4}-\d{4}-\d{4}-\d{3}[\dXx])\/?$/
// An ISNI identifies a person as well as an ORCID does, and the catalogue already treats one as a
// person's identifier (ResponsibleParty.isIsni, the RDF export)
const ISNI_FORM = /^https?:\/\/isni\.org\/isni\/\d{15}[\dX]$/

/** Whether the check character of a bare iD (0000-0002-1825-0097) is right */
export function orcidChecksumValid (id) {
  const digits = id.replace(/-/g, '')
  let total = 0
  for (const digit of digits.slice(0, -1)) {
    total = (total + Number(digit)) * 2
  }
  const result = (12 - (total % 11)) % 11
  return digits.slice(-1).toUpperCase() === (result === 10 ? 'X' : String(result))
}

/** The bare, upper-cased iD a value has the shape of, valid or not, or null */
function orcidShape (value) {
  const match = ORCID_FORM.exec((value || '').trim())
  return match ? match[1].toUpperCase() : null
}

/** The bare iD of a value that is a real ORCID iD, check character and all, or null */
export function orcidId (value) {
  const id = orcidShape(value)
  return id && orcidChecksumValid(id) ? id : null
}

/**
 * What to keep for what was typed: https://orcid.org/<id> for anything shaped like an ORCID iD (a
 * wrong check character is left for validation to point out), and anything else as it is.
 */
export function normalisePersonIdentifier (value) {
  const id = orcidShape(value)
  return id ? ORCID_PREFIX + id : (value || '').trim()
}

export function isIsni (value) {
  return ISNI_FORM.test((value || '').trim())
}

/** What's wrong with a person's identifier, or null for one that's fine (or absent) */
export function personIdentifierError (value) {
  if (!value) return null
  const id = orcidShape(value)
  if (id) {
    return orcidChecksumValid(id)
      ? null
      : `${ORCID_PREFIX}${id} isn't a real ORCID iD: its last character doesn't match the rest. Check it was copied correctly.`
  }
  if (isIsni(value)) return null
  return 'An ORCID iD looks like https://orcid.org/0000-0002-1825-0097. Paste it from the person\'s ORCID record.'
}
