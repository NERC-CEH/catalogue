import { describe, expect, it, vi } from 'vitest'
import {
  Author, Contact, DistributionFormat, Funding, MapDataSource, MultipleDate, ObservedProperty, OnlineResource,
  Reference, RightsHolder, ServiceAgreement, SpatialResolution, Supplemental
} from '../../src/models'

// What each editor model refuses, and what it keeps, before a record is saved (dri-one #473)
const messages = model => (model.validate(model.attributes) || []).map(error => error.message || error)

describe('Editor model validation', () => {
  describe('Author (service agreement)', () => {
    const valid = { email: 'a@example.org', organisationName: 'UKCEH', givenName: 'Ann', familyName: 'Other' }

    it('accepts a named author with an affiliation and email', () => {
      expect(new Author(valid).validate(valid)).toBeUndefined()
    })

    it('accepts an institutional author name instead of a person\'s', () => {
      const attrs = { email: 'a@example.org', organisationName: 'UKCEH', displayName: 'The Survey Team' }
      expect(new Author(attrs).validate(attrs)).toBeUndefined()
    })

    it('requires an email, an affiliation and a name', () => {
      expect(messages(new Author({}))).toEqual([
        'Email address is mandatory',
        'Affiliation is mandatory',
        'You must include the author\'s full name OR an institional author name '
      ])
    })

    it('refuses a malformed email and an ORCiD without its URL', () => {
      const author = new Author({ ...valid, email: 'not-an-email', nameIdentifier: '0000-1234-5678-999X' })
      expect(messages(author)).toEqual([
        'That email address is invalid',
        expect.stringMatching(/^That ORCiD is invalid/)
      ])
    })

    it('leaves out an empty address when saved', () => {
      expect(new Author(valid).toJSON().address).toBeUndefined()
      expect(new Author({ ...valid, address: { city: 'Lancaster' } }).toJSON().address).toEqual({ city: 'Lancaster' })
    })
  })

  describe('RightsHolder', () => {
    it('takes an organisation or a person', () => {
      expect(messages(new RightsHolder({ organisationName: 'UKCEH' }))).toEqual([])
      expect(messages(new RightsHolder({ givenName: 'Ann', familyName: 'Other' }))).toEqual([])
    })

    it('refuses neither, and both', () => {
      expect(messages(new RightsHolder({ givenName: 'Ann' }))).toEqual(['You must add either an organisation name or a persons name.'])
      expect(messages(new RightsHolder({ organisationName: 'UKCEH', familyName: 'Other' }))).toEqual(['You must add either an organisation name OR a persons name.'])
    })
  })

  describe('Contact', () => {
    const valid = { role: 'author', organisationName: 'UKCEH' }

    it('accepts a contact with a role and organisation', () => {
      expect(new Contact(valid).validate(valid)).toBeNull()
    })

    it('requires a role and an organisation name', () => {
      expect(messages(new Contact({}))).toEqual(['Role is mandatory.', 'Organisation name is mandatory.'])
    })

    it('checks the email, ORCiD and ROR', () => {
      const contact = new Contact({ ...valid, email: 'nope', nameIdentifier: '0000-1234', organisationIdentifier: 'https://ror.org/x' })
      expect(messages(contact)).toEqual([
        'That email address is wrong',
        'Invalid ORCiD',
        expect.stringMatching(/^ORCiDs should be entered as/),
        'Invalid RoR'
      ])
    })

    it('accepts a full ORCiD and ROR', () => {
      const contact = new Contact({ ...valid, email: 'a@example.org', nameIdentifier: 'https://orcid.org/0000-0002-1825-0097', organisationIdentifier: 'https://ror.org/00pggkr55' })
      expect(messages(contact)).toEqual([])
    })

    it('leaves out an empty address when saved', () => {
      expect(new Contact(valid).toJSON().address).toBeUndefined()
    })
  })

  describe('Funding', () => {
    it('requires the funding body', () => {
      expect(messages(new Funding({}))).toEqual(['Funding body is mandatory'])
    })

    it('checks an award URI is a URL', () => {
      expect(messages(new Funding({ funderName: 'NERC', awardURI: 'not a url' }))).toEqual(["That URL doesn't look right!"])
      expect(messages(new Funding({ funderName: 'NERC', awardURI: 'https://gtr.ukri.org/projects?ref=NE%2FX1' }))).toEqual([])
    })
  })

  describe('Reference', () => {
    it('checks the DOI and the NORA link', () => {
      expect(messages(new Reference({ doi: '10.1234/abc', nora: 'nora' }))).toEqual(['Invalid DOI url', 'Invalid NORA URL'])
      expect(messages(new Reference({ doi: 'https://doi.org/10.1234/abc', nora: 'https://nora.nerc.ac.uk/id/eprint/1/' }))).toEqual([])
    })
  })

  describe('links (Supplemental, OnlineResource)', () => {
    it('refuses something that isn\'t a URL', () => {
      expect(messages(new Supplemental({ url: 'see the report' }))).toEqual(["That url isn't right."])
      expect(messages(new OnlineResource({ url: 'see the report' }))).toEqual(["That url isn't right."])
    })

    it('requires the online resource\'s URL, but not a supplemental one\'s', () => {
      expect(messages(new OnlineResource({}))).toEqual(["That url isn't right."])
      expect(messages(new Supplemental({}))).toEqual([])
    })

    // They each had a copy of the URL pattern whose \u escapes had lost their `u`, so a domain with a
    // non-ASCII letter was refused here while Funding and Reference accepted it
    it('accepts a domain with non-ASCII letters, as Funding and Reference do', () => {
      const url = 'https://exämple.org/data'
      expect(messages(new Supplemental({ url }))).toEqual([])
      expect(messages(new OnlineResource({ url }))).toEqual([])
      expect(messages(new Funding({ funderName: 'NERC', awardURI: url }))).toEqual([])
    })
  })

  describe('DistributionFormat', () => {
    it('requires a version once a name or type is given', () => {
      expect(messages(new DistributionFormat({ name: 'CSV' }))).toEqual([expect.stringMatching(/^The version is mandatory/)])
      expect(messages(new DistributionFormat({ type: 'text/csv', version: 'unknown' }))).toEqual([])
      expect(messages(new DistributionFormat({}))).toEqual([])
    })
  })

  describe('SpatialResolution', () => {
    it('takes a scale or a distance, not both', () => {
      expect(messages(new SpatialResolution({ equivalentScale: '10000' }))).toEqual([])
      expect(messages(new SpatialResolution({ equivalentScale: '10000', distance: '5' }))).toEqual([expect.stringMatching(/EITHER/)])
    })
  })

  describe('MultipleDate', () => {
    it('accepts yyyy-mm-dd dates in order', () => {
      expect(messages(new MultipleDate({ begin: '2020-01-01', end: '2021-06-30' }))).toEqual([])
    })

    it('refuses another format, naming the field', () => {
      expect(messages(new MultipleDate({ publicationDate: '01/02/2020' }))).toEqual(['Publication date is wrong. The date format is yyyy-mm-dd'])
    })

    it('refuses something that isn\'t a date at all', () => {
      expect(messages(new MultipleDate({ revisionDate: 'soon' }))).toEqual([
        'Revision date is wrong. The date format is yyyy-mm-dd',
        "Revision date doesn't look like a valid date"
      ])
    })

    it('refuses an end before the beginning', () => {
      expect(messages(new MultipleDate({ begin: '2021-01-01', end: '2020-01-01' }))).toEqual(['End date is before start date'])
    })

    it('ignores dates not given yet', () => {
      expect(messages(new MultipleDate({ begin: '', end: null }))).toEqual([])
    })
  })

  describe('ObservedProperty', () => {
    it('leaves out empty constraints when saved', () => {
      expect(new ObservedProperty({ value: 'SITE' }).toJSON()).toEqual({ value: 'SITE' })
      expect(new ObservedProperty({ value: 'ID', constraints: { unique: true } }).toJSON().constraints).toEqual({ unique: true })
    })
  })

  describe('MapDataSource', () => {
    const attribute = (name, buckets) => ({ name, buckets })

    it('requires bucket bounds to be numbers', () => {
      const source = new MapDataSource({ attributes: [attribute('rain', [{ min: '0', max: 'lots' }])] })
      expect(messages(source)).toEqual(['Bucket values must be numbers'])
    })

    it('requires layer names to be unique', () => {
      const source = new MapDataSource({ attributes: [attribute('rain', []), attribute('rain', [])] })
      expect(messages(source)).toEqual(['Layer names must be unique'])
    })

    it('accepts numeric buckets on distinct layers', () => {
      const source = new MapDataSource({ attributes: [attribute('rain', [{ min: '-1.5', max: '.5' }]), attribute('snow', [])] })
      expect(messages(source)).toEqual([])
    })

    it('saves the features or the attributes, whichever it is styled by', () => {
      const byFeatures = new MapDataSource({})
      expect(byFeatures.stylingMode).toBe('features')
      expect(byFeatures.toJSON().features).toBeDefined()
      expect(byFeatures.toJSON().attributes).toBeUndefined()

      const byAttributes = new MapDataSource({ attributes: [attribute('rain', [])] })
      expect(byAttributes.stylingMode).toBe('attributes')
      expect(byAttributes.toJSON().features).toBeUndefined()
    })

    it('announces a change of styling mode', () => {
      const source = new MapDataSource({})
      const changed = vi.fn()
      source.on('change', changed)

      source.setStylingMode('attributes')

      expect(source.stylingMode).toBe('attributes')
      expect(changed).toHaveBeenCalled()
    })
  })

  describe('ServiceAgreement', () => {
    const inDays = days => {
      const date = new Date()
      date.setDate(date.getDate() + days)
      return date.toISOString().slice(0, 10)
    }

    it('saves to its record, or creates one in the EIDC catalogue', () => {
      expect(new ServiceAgreement({ id: 'abc' }).urlRoot()).toBe('/service-agreement/abc')
      expect(new ServiceAgreement({}, { id: 'abc' }).urlRoot()).toBe('/service-agreement/abc?catalogue=eidc')
    })

    it('requires a title', () => {
      expect(new ServiceAgreement({}, { id: 'abc' }).validate({})).toEqual(['A title is mandatory'])
    })

    it('takes an embargo up to five years ahead, not in the past', () => {
      const agreement = new ServiceAgreement({}, { id: 'abc' })
      expect(agreement.validate({ title: 'T', availability: inDays(30) })).toBeUndefined()
      expect(agreement.validate({ title: 'T', availability: inDays(-30) })).toEqual(['Embargo date is in the past or too far in the future'])
      expect(agreement.validate({ title: 'T', availability: inDays(6 * 366) })).toEqual(['Embargo date is in the past or too far in the future'])
    })
  })
})
