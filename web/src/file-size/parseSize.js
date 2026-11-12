const POWERS = { B: 0, KB: 1, MB: 2, GB: 3, TB: 4 }
const NUMBER = '(\\d+(?:\\.\\d+)?)'
const UNIT = '(B|KB|MB|GB|TB)'
const SINGLE = new RegExp(`^${NUMBER}\\s*${UNIT}?$`, 'i')
const RANGE = new RegExp(`^${NUMBER}\\s*${UNIT}?\\s*[-–]\\s*${NUMBER}\\s*${UNIT}?$`, 'i')

// A size beyond what a number holds exactly (and the Java Long it is saved as) is unreadable, not rounded
const toBytes = (number, unit = 'B') => {
  const bytes = Math.round(Number(number) * 1024 ** POWERS[unit.toUpperCase()])
  return Number.isSafeInteger(bytes) ? bytes : null
}

/**
 * Reads a size as people write it ("32 MB", "0.4gb", "2048", "2-2.5 MB") into whole bytes, using
 * the binary multiples formatBytes shows. A single size gives { bytes }, a range { min, max }, and
 * anything else, or a size of nothing, null. A range's first unit may be left off ("2-2.5 MB"),
 * and a range whose ends are equal ("2-2 MB") is that one size. A size too large to hold exactly is
 * unreadable.
 */
export const parseSize = text => {
  const trimmed = String(text ?? '').trim()
  const single = SINGLE.exec(trimmed)
  if (single) {
    const bytes = toBytes(single[1], single[2])
    return bytes > 0 ? { bytes } : null
  }
  const range = RANGE.exec(trimmed)
  if (range) {
    const min = toBytes(range[1], range[2] ?? range[4])
    const max = toBytes(range[3], range[4] ?? range[2])
    if (min === null || max === null || min <= 0 || max < min) return null
    return max === min ? { bytes: min } : { min, max }
  }
  return null
}

/** The bytes of a single size, or null for a range or anything that is not a size. */
export const parseExactSize = text => parseSize(text)?.bytes ?? null
