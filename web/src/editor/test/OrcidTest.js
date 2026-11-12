import { describe, expect, it } from 'vitest'
import { isIsni, normalisePersonIdentifier, orcidChecksumValid, orcidId, personIdentifierError } from '../src/utils/orcid'

// ORCID iDs as editors type them: stored in full, and checked by their last character (dri-one #534)
describe('ORCID iDs', () => {
  describe('orcidChecksumValid', () => {
    it('accepts iDs whose check character matches', () => {
      expect(orcidChecksumValid('0000-0002-1825-0097')).toBe(true)
      expect(orcidChecksumValid('0000-0002-1694-233X')).toBe(true)
      expect(orcidChecksumValid('0000-0002-1694-233x')).toBe(true)
    })

    it('refuses a wrong digit or two transposed', () => {
      expect(orcidChecksumValid('0000-0002-1825-0098')).toBe(false)
      expect(orcidChecksumValid('0000-0002-1852-0097')).toBe(false)
    })
  })

  describe('orcidId', () => {
    it('reads the iD however it is written', () => {
      ['0000-0002-1825-0097', 'https://orcid.org/0000-0002-1825-0097', 'http://orcid.org/0000-0002-1825-0097',
        'orcid.org/0000-0002-1825-0097', 'https://www.orcid.org/0000-0002-1825-0097/', ' 0000-0002-1825-0097 ']
        .forEach(value => expect(orcidId(value)).toBe('0000-0002-1825-0097'))
    })

    it('is null for anything that isn\'t a real iD', () => {
      [undefined, '', '0000-0002-1825-0098', '0000-0002-1825', 'https://example.org/0000-0002-1825-0097']
        .forEach(value => expect(orcidId(value)).toBeNull())
    })
  })

  describe('normalisePersonIdentifier', () => {
    it('keeps anything shaped like an iD as https://orcid.org/…, with an upper-case X', () => {
      expect(normalisePersonIdentifier('0000-0002-1694-233x')).toBe('https://orcid.org/0000-0002-1694-233X')
      expect(normalisePersonIdentifier('http://orcid.org/0000-0002-1825-0097')).toBe('https://orcid.org/0000-0002-1825-0097')
      // Its wrong check character is left for validation to say
      expect(normalisePersonIdentifier('0000-0002-1825-0098')).toBe('https://orcid.org/0000-0002-1825-0098')
    })

    it('leaves anything else as it is, trimmed', () => {
      expect(normalisePersonIdentifier(' https://isni.org/isni/000000012146438X ')).toBe('https://isni.org/isni/000000012146438X')
      expect(normalisePersonIdentifier('Clare')).toBe('Clare')
      expect(normalisePersonIdentifier(undefined)).toBe('')
    })
  })

  describe('personIdentifierError', () => {
    it('is nothing for an iD, an ISNI or no identifier', () => {
      expect(personIdentifierError('https://orcid.org/0000-0002-1825-0097')).toBeNull()
      expect(personIdentifierError('https://isni.org/isni/000000012146438X')).toBeNull()
      expect(personIdentifierError(undefined)).toBeNull()
    })

    it('says when an iD\'s check character is wrong', () => {
      expect(personIdentifierError('0000-0002-1825-0098'))
        .toBe('https://orcid.org/0000-0002-1825-0098 isn\'t a real ORCID iD: its last character doesn\'t match the rest. Check it was copied correctly.')
    })

    it('says what an iD looks like for anything else', () => {
      expect(personIdentifierError('Clare Rowland')).toMatch(/^An ORCID iD looks like https:\/\/orcid\.org\/0000-0002-1825-0097/)
    })
  })

  it('recognises an ISNI URL', () => {
    expect(isIsni('https://isni.org/isni/000000012146438X')).toBe(true)
    expect(isIsni('000000012146438X')).toBe(false)
  })
})
