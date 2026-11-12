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
 * UploadView.uploaded and UploadService.sanitisedFilename do. Applied to the agreement's side only,
 * so a name typed as `Site 1/Data.csv` matches the `site-1/data.csv` that arrives. JavaScript's
 * toLowerCase is locale-independent, like Java's toLowerCase(Locale.ROOT).
 */
export const asUploaded = name => name.toLowerCase().replaceAll(' ', '-')

const isDigits = value => typeof value === 'string' || typeof value === 'number'
  ? /^\d+$/.test(String(value).trim())
  : false

const toCount = value => isDigits(value) ? parseInt(String(value).trim(), 10) : null

/**
 * Compare the agreed file listing with the uploaded files. Agreed names and patterns are first put
 * as an upload stores them (asUploaded); upload paths are compared as listed.
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

  const present = new Set(
    (uploads || []).filter(upload => !ABSENT_STATUSES.has(upload.status)).map(upload => upload.path)
  )

  const named = new Set(files.map(file => file.name).filter(Boolean).map(asUploaded))
  const claimed = new Set()

  const fileResults = files.map(file => {
    if (!file.name) return { ...file, status: 'unknown' }
    const name = asUploaded(file.name)
    if (present.has(name)) {
      claimed.add(name)
      return { ...file, status: 'uploaded' }
    }
    return { ...file, status: complete ? 'not-uploaded' : 'unknown' }
  })

  const matchers = groups.map(group => group.pattern ? patternToRegExp(asUploaded(group.pattern)) : null)
  const found = groups.map(() => 0)
  const unmatched = []
  present.forEach(path => {
    if (named.has(path)) return
    const index = matchers.findIndex(matcher => matcher && matcher.test(path))
    if (index === -1) {
      unmatched.push(path)
    } else {
      found[index]++
      claimed.add(path)
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
    total: {
      expected: expectedTotal,
      found: claimed.size,
      differs: Boolean(complete) && expectedTotal !== null && claimed.size !== expectedTotal
    }
  }
}
