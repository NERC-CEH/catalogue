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

/**
 * Compare the agreed file listing with the uploaded files. Agreed names and patterns, and upload
 * paths, are first put as an upload stores them (asUploaded); paths that are then the same count
 * once. Unmatched paths are given as first listed.
 *
 * `expected` is `{ files, fileGroups, fileNumber }`, any of which may be absent, as may the
 * keys inside each file or group. `uploads` is `[{ path, status }]` from every listing that holds
 * data files. `complete` says every listing that could hold a file is fully loaded: only then
 * can a file be called not uploaded or a group short. A partial listing can only undercount, so
 * `over` is flagged regardless.
 */
export function matchExpected (expected, uploads, { complete }) {
  const files = (expected && expected.files) || []
  const groups = (expected && expected.fileGroups) || []

  // As put for comparing, to the path as first listed
  const present = new Map()
  for (const upload of uploads || []) {
    if (!upload.path) continue
    const path = asUploaded(upload.path)
    if (!ABSENT_STATUSES.has(upload.status) && !present.has(path)) present.set(path, upload.path)
  }

  const named = new Set(files.map(file => file.name).filter(Boolean).map(asUploaded))

  const fileResults = files.map(file => {
    if (!file.name) return { ...file, status: 'unknown' }
    if (present.has(asUploaded(file.name))) return { ...file, status: 'uploaded' }
    return { ...file, status: complete ? 'not-uploaded' : 'unknown' }
  })

  const matchers = groups.map(group => group.pattern ? patternToRegExp(asUploaded(group.pattern)) : null)
  const found = groups.map(() => 0)
  const unmatched = []
  present.forEach((listed, path) => {
    if (named.has(path)) return
    const index = matchers.findIndex(matcher => matcher && matcher.test(path))
    if (index === -1) {
      unmatched.push(listed)
    } else {
      found[index]++
    }
  })

  const groupResults = groups.map((group, index) => {
    const count = toCount(group.count)
    let flag = null
    if (count !== null && matchers[index]) {
      if (found[index] > count) flag = 'over'
      else if (complete && found[index] < count) flag = 'short'
    }
    return { ...group, found: found[index], flag }
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
