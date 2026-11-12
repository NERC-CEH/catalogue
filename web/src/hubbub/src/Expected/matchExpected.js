// Statuses (see File/File.js) that say a listed file is gone. Every other status counts as uploaded.
export const ABSENT_STATUSES = new Set([
  'MISSING',
  'UNKNOWN_MISSING',
  'MOVED_UNKNOWN_MISSING',
  'ZIPPED_UNKNOWN_MISSING'
])

/**
 * Turn a file group pattern into an anchored regular expression: `*` is any run of characters
 * within one path segment, `?` is one character other than `/`, and everything else is literal.
 */
export function patternToRegExp (pattern) {
  const body = [...pattern].map(char => {
    if (char === '*') return '[^/]*'
    if (char === '?') return '[^/]'
    return char.replace(/[.*+?^${}()|[\]\\/-]/g, '\\$&')
  }).join('')
  return new RegExp(`^${body}$`)
}

/**
 * A name as an upload through the page stores it: lower case and a hyphen for each space, as
 * UploadView.uploaded and UploadService.sanitisedFilename do. Applied to both sides before
 * comparing: a name typed as `Site 1/Data.csv` matches the `site-1/data.csv` that arrives, and a
 * zip's folder, a registered file or a cloud transfer, which keep their case, match too.
 * JavaScript's toLowerCase is locale-independent, like Java's toLowerCase(Locale.ROOT).
 */
export const asUploaded = name => name.toLowerCase().replaceAll(' ', '-')

const isDigits = value => typeof value === 'string' || typeof value === 'number'
  ? /^\d+$/.test(String(value).trim())
  : false

const toCount = value => isDigits(value) ? parseInt(String(value).trim(), 10) : null

/** How far, as a fraction, an uploaded size may be from the agreed one before it is flagged. */
export const SIZE_TOLERANCE = 0.1

// A byte count that says something: a listing without sizes, or a size of 0, is unknown
const knownBytes = value => typeof value === 'number' && Number.isFinite(value) && value > 0

const outside = (bytes, min, max) => bytes < min - min * SIZE_TOLERANCE || bytes > max + max * SIZE_TOLERANCE

/**
 * Compare the agreed file listing with the uploaded files. Agreed names and patterns, and upload
 * paths, are first put as an upload stores them (asUploaded); paths that are then the same count
 * once. Unmatched paths are given as first listed.
 *
 * `expected` is `{ files, fileGroups, fileNumber }`, any of which may be absent, as may the
 * keys inside each file or group. `uploads` is `[{ path, status, bytes? }]` from every listing that
 * holds data files. `complete` says every listing that could hold a file is fully loaded: only then
 * can a file be called not uploaded or a group short. A partial listing can only undercount, so
 * `over` is flagged regardless.
 *
 * Sizes (dri-one #537) are checked only when `complete`, and only for uploads whose `bytes` is
 * known, each within SIZE_TOLERANCE: a named file against its `bytes` (`sizeDiffers`), each file a
 * group matches against its `minBytes`-`maxBytes` (`sizesDiffer`), and the sum of a group's files
 * against its `totalBytes` (`totalDiffers`), the last only once the group's count is met and every
 * file it matches has a size, so a partial sum is never compared. An agreement whose sizes are
 * only text has none of these fields and is not checked.
 */
export function matchExpected (expected, uploads, { complete }) {
  const files = (expected && expected.files) || []
  const groups = (expected && expected.fileGroups) || []

  // As put for comparing, to the upload as first listed
  const present = new Map()
  for (const upload of uploads || []) {
    if (!upload.path) continue
    const path = asUploaded(upload.path)
    if (!ABSENT_STATUSES.has(upload.status) && !present.has(path)) present.set(path, upload)
  }

  const named = new Set(files.map(file => file.name).filter(Boolean).map(asUploaded))

  const fileResults = files.map(file => {
    if (!file.name) return { ...file, status: 'unknown', sizeDiffers: false }
    const upload = present.get(asUploaded(file.name))
    if (upload) {
      const sizeDiffers = Boolean(complete) && knownBytes(file.bytes) && knownBytes(upload.bytes) &&
        outside(upload.bytes, file.bytes, file.bytes)
      return { ...file, status: 'uploaded', sizeDiffers }
    }
    return { ...file, status: complete ? 'not-uploaded' : 'unknown', sizeDiffers: false }
  })

  const matchers = groups.map(group => group.pattern ? patternToRegExp(asUploaded(group.pattern)) : null)
  // The sizes of the files each group matches, as listed (unknown ones included)
  const matched = groups.map(() => [])
  const unmatched = []
  present.forEach((upload, path) => {
    if (named.has(path)) return
    const index = matchers.findIndex(matcher => matcher && matcher.test(path))
    if (index === -1) {
      unmatched.push(upload.path)
    } else {
      matched[index].push(upload.bytes)
    }
  })
  const found = matched.map(sizes => sizes.length)

  const groupResults = groups.map((group, index) => {
    const count = toCount(group.count)
    let flag = null
    if (count !== null && matchers[index]) {
      if (found[index] > count) flag = 'over'
      else if (complete && found[index] < count) flag = 'short'
    }
    const countMet = count !== null && count === found[index]
    return { ...group, found: found[index], flag, ...groupSizes(group, matched[index], complete, countMet) }
  })

  const expectedTotal = toCount(expected && expected.fileNumber)
  return {
    files: fileResults,
    groups: groupResults,
    unmatched,
    // Every uploaded data file, matched or not, against the agreement's number of files
    total: {
      expected: expectedTotal,
      found: present.size,
      differs: Boolean(complete) && expectedTotal !== null && present.size !== expectedTotal
    }
  }
}

// A group's size flags from the sizes of the files it matches (as listed). One bound alone
// is taken as both.
function groupSizes (group, sizes, complete, countMet) {
  const min = knownBytes(group.minBytes) ? group.minBytes : group.maxBytes
  const max = knownBytes(group.maxBytes) ? group.maxBytes : group.minBytes
  const known = sizes.filter(knownBytes)
  const sizesDiffer = Boolean(complete) && knownBytes(min) &&
    known.some(bytes => outside(bytes, min, max))
  const totalDiffers = Boolean(complete) && countMet && knownBytes(group.totalBytes) &&
    known.length === sizes.length && known.length > 0 &&
    outside(known.reduce((sum, bytes) => sum + bytes, 0), group.totalBytes, group.totalBytes)
  return { sizesDiffer, totalDiffers }
}
