import { describe, expect, it } from 'vitest'
import { matchedAlias, orgDetails, orgName, orgOption } from '../src/utils/orgUtils'

// How an organisation from ROR is described in the organisation look-up, so same-named ones can be
// told apart (dri-one #417)
describe('Describing an organisation', () => {
  const nyu = {
    id: 'https://ror.org/0190ak572',
    name: 'New York University',
    acronyms: ['NYU'],
    city: 'New York',
    region: 'New York',
    country: 'United States',
    types: ['education'],
    website: 'https://www.nyu.edu/'
  }

  describe('orgName', () => {
    it('adds the acronym in brackets', () => {
      expect(orgName(nyu)).toBe('New York University (NYU)')
    })

    it('leaves out an acronym the name already says', () => {
      expect(orgName({ name: 'UKCEH', acronyms: ['UKCEH'] })).toBe('UKCEH')
    })

    it('is the name alone without an acronym', () => {
      expect(orgName({ name: 'University of York' })).toBe('University of York')
    })
  })

  describe('orgDetails', () => {
    it('says where it is, what kind it is and its website\'s host', () => {
      expect(orgDetails(nyu)).toBe('New York, United States · Education · nyu.edu')
    })

    it('says a name once where ROR repeats it', () => {
      expect(orgDetails({ city: 'Oslo', region: 'Oslo', country: 'Norway' })).toBe('Oslo, Norway')
      expect(orgDetails({ city: 'Luxembourg', region: 'Luxembourg', country: 'Luxembourg' })).toBe('Luxembourg')
    })

    it('leaves out a region that is a GeoNames code', () => {
      expect(orgDetails({ city: 'Singapore', region: 'SG.01', country: 'Singapore', types: ['government'] })).toBe('Singapore · Government')
      expect(orgDetails({ city: 'Abuja', region: 'FCT', country: 'Nigeria' })).toBe('Abuja, FCT, Nigeria')
    })

    // 3,906 active organisations have no website
    it('leaves out what ROR doesn\'t say', () => {
      expect(orgDetails({ city: 'York', country: 'United Kingdom' })).toBe('York, United Kingdom')
      expect(orgDetails({ name: 'Somewhere' })).toBe('')
    })

    it('leaves out a website that isn\'t a URL', () => {
      expect(orgDetails({ country: 'United Kingdom', website: 'not a url' })).toBe('United Kingdom')
    })
  })

  describe('matchedAlias', () => {
    const nebraska = { name: 'York University', aliases: ['York College'] }

    it('is the alias a search matched better than the name', () => {
      expect(matchedAlias(nebraska, 'york college')).toBe('York College')
    })

    it('is nothing when the name matched as well', () => {
      expect(matchedAlias(nebraska, 'york')).toBeNull()
      expect(matchedAlias(nebraska, 'york university')).toBeNull()
    })

    it('is nothing when the acronym matched', () => {
      expect(matchedAlias({ name: 'UK Centre for Ecology & Hydrology', acronyms: ['UKCEH'], aliases: ['Canolfan Ecoleg a Hydroleg y DU'] }, 'ukceh')).toBeNull()
    })

    it('ignores case and accents', () => {
      expect(matchedAlias({ name: 'Institut X', aliases: ['École Polytechnique'] }, 'ecole')).toBe('École Polytechnique')
    })

    it('is nothing for an empty search', () => {
      expect(matchedAlias(nebraska, '  ')).toBeNull()
    })
  })

  describe('orgOption', () => {
    it('puts the name on one line and the details on a quieter one beneath', () => {
      const option = orgOption(nyu, 'nyu')

      expect(option.querySelector('.org-name').textContent).toBe('New York University (NYU)')
      expect(option.querySelector('.org-details').textContent).toBe('New York, United States · Education · nyu.edu')
      expect(option.querySelector('.org-alias')).toBeNull()
    })

    it('says what else it is called when an alias was matched', () => {
      const option = orgOption({ name: 'York University', aliases: ['York College'], country: 'United States' }, 'York College')

      expect(option.querySelector('.org-alias').textContent).toBe('also known as York College')
    })

    it('has no details line when ROR says nothing more than the name', () => {
      expect(orgOption({ name: 'Somewhere' }).querySelectorAll('.org-details').length).toBe(0)
    })

    // ROR's names are third-party data
    it('writes what ROR says as text, never as markup', () => {
      const option = orgOption({ name: '<img src=x onerror=alert(1)>', city: '<b>York</b>' })

      expect(option.querySelector('img')).toBeNull()
      expect(option.querySelector('b')).toBeNull()
      expect(option.querySelector('.org-name').textContent).toBe('<img src=x onerror=alert(1)>')
    })
  })
})
