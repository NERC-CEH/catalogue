import { cleanText } from '../src/utils/textUtils'
import { formatDateForInput } from '../src/utils/dateUtils'

describe('textUtils.cleanText', function () {
  it('returns falsy input unchanged', function () {
    expect(cleanText('')).toBe('')
    expect(cleanText(undefined)).toBeUndefined()
    expect(cleanText(null)).toBeNull()
  })

  it('trims surrounding whitespace', function () {
    expect(cleanText('  hello  ')).toBe('hello')
  })

  it('replaces curly double quotes with straight quotes', function () {
    expect(cleanText('“quoted”')).toBe('"quoted"')
  })

  it('replaces curly single quotes/apostrophes with straight quotes', function () {
    expect(cleanText('it’s ‘here’')).toBe("it's 'here'")
  })

  it('replaces en/em dashes with a hyphen', function () {
    expect(cleanText('a–b—c')).toBe('a-b-c')
  })

  it('replaces an ellipsis character with three dots', function () {
    expect(cleanText('wait…')).toBe('wait...')
  })

  it('replaces a bullet character with a hyphen', function () {
    expect(cleanText('• item')).toBe('- item')
  })

  it('applies every replacement together', function () {
    expect(cleanText('  “CEH’s data—set…”  '))
      .toBe('"CEH\'s data-set..."')
  })
})

describe('dateUtils.formatDateForInput', function () {
  it('returns an empty string for falsy input', function () {
    expect(formatDateForInput(undefined)).toBe('')
    expect(formatDateForInput(null)).toBe('')
    expect(formatDateForInput('')).toBe('')
  })

  it('formats an ISO date-time (UTC) as YYYY-MM-DD', function () {
    // Explicit Z keeps this deterministic regardless of the runner timezone.
    expect(formatDateForInput('2022-03-05T06:23:57Z')).toBe('2022-03-05')
  })

  it('formats a date-only string as YYYY-MM-DD', function () {
    expect(formatDateForInput('2020-12-31')).toBe('2020-12-31')
  })
})
