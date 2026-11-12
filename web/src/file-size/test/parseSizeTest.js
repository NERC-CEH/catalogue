import { describe, expect, it } from 'vitest'
import { parseExactSize, parseSize } from '../parseSize'

// Shared with FileSizesTest.java, which expects formatBytes's strings from the Java side:
//   0 -> "0 B", 1 -> "1 B", 1023 -> "1023 B", 1024 -> "1 KB", 1536 -> "1.5 KB",
//   33554432 -> "32 MB", 1048575 -> "1 MB", 1.5 * 1024^4 -> "1.5 TB"
describe('Reading a file size into bytes', () => {
  it('reads a single size whatever the case or spacing', () => {
    expect(parseSize('32 MB')).toEqual({ bytes: 33554432 })
    expect(parseSize('32mb')).toEqual({ bytes: 33554432 })
    expect(parseSize('32MB')).toEqual({ bytes: 33554432 })
  })

  it('rounds to whole bytes', () => {
    expect(parseSize('0.4 gb')).toEqual({ bytes: Math.round(0.4 * 1024 ** 3) })
    expect(parseSize('1.5 TB')).toEqual({ bytes: 1.5 * 1024 ** 4 })
  })

  it('reads a bare number as bytes', () => {
    expect(parseSize('2048')).toEqual({ bytes: 2048 })
  })

  it('reads a range in its several spellings', () => {
    const range = { min: 2097152, max: 2621440 }
    for (const text of ['2-2.5 MB', '2 MB - 2.5 MB', '2–2.5 MB', '2MB-2.5MB', '2 - 2.5 mb']) {
      expect(parseSize(text), text).toEqual(range)
    }
  })

  it('gives nothing for text that is not a size', () => {
    for (const text of ['2.5-2 MB', '>= 2 MB', 'about 2 MB', '', 'MB', '-3 MB', '0']) {
      expect(parseSize(text), text).toBeNull()
    }
  })

  it('gives nothing for a missing value', () => {
    expect(parseSize(undefined)).toBeNull()
    expect(parseSize(null)).toBeNull()
  })
})

describe('Reading an exact file size', () => {
  it('gives the bytes of a single size', () => {
    expect(parseExactSize('3 MB')).toBe(3145728)
  })

  it('gives nothing for a range or for text that is not a size', () => {
    expect(parseExactSize('2-3 MB')).toBeNull()
    expect(parseExactSize('lots')).toBeNull()
  })
})
