/**
 * Whether a repeating-row entry holds anything worth persisting.
 *
 * ParentView creates a row model the moment its add button is clicked and immediately syncs the
 * collection into the parent model, so a row that is added and never filled in arrives here as an
 * attribute-less object. Those must not reach the record: on the record page a blank keyword renders
 * as a stray separator, and a list whose only entry is blank renders a labelled row with no value.
 * See NERC-CEH/dri-one#297.
 *
 * Only '', whitespace, null and undefined count as empty. The number 0 and the boolean false are
 * content - boundingBoxes is one of the fields ParentView drives, and a box of all zeroes is a real
 * location, so a plain falsy check would silently discard it.
 */
export function hasContent (value) {
  if (value === null || value === undefined) {
    return false
  }
  if (typeof value === 'string') {
    return value.trim().length > 0
  }
  if (Array.isArray(value)) {
    return value.some(entry => hasContent(entry))
  }
  if (typeof value === 'object') {
    return Object.values(value).some(entry => hasContent(entry))
  }
  return true
}
