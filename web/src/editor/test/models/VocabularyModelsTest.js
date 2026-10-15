import { describe, expect, it } from 'vitest'
import {
  AccessLimitation, EnvironmentalDomain, FacilityType, InfrastructureCategory, InfrastructureChallenge, InspireTheme,
  KeywordTheme, PurposeOfCollection, SaPhysicalState, SaSpecimenType, SaTaxa, SaTissue, TopicCategory
} from '../../src/models'

// Models for a choice from a controlled vocabulary: choosing a term sets the URI (and, for some, other
// details) the record saves with it (dri-one #473)
describe('Vocabulary models', () => {
  // Each with the attribute its term is chosen in
  const LOOKUPS = [
    [EnvironmentalDomain, 'value'], [FacilityType, 'value'], [InfrastructureChallenge, 'value'],
    [InspireTheme, 'theme'], [KeywordTheme, 'value'], [PurposeOfCollection, 'value'],
    [SaPhysicalState, 'value'], [SaSpecimenType, 'value'], [SaTaxa, 'value'], [SaTissue, 'value'],
    [TopicCategory, 'value']
  ]

  LOOKUPS.forEach(([Model, attribute]) => {
    const terms = Object.keys(Model.prototype.uris)

    describe(`${terms.length} terms in ${attribute}, starting "${terms[0]}"`, () => {
      it('sets the URI of the chosen term', () => {
        const model = new Model()
        terms.forEach(term => {
          model.set(attribute, term)
          expect(model.get('uri'), term).toBe(Model.prototype.uris[term])
        })
      })

      it('clears the URI for a term it doesn\'t know', () => {
        const model = new Model()
        model.set(attribute, terms[0])
        model.set(attribute, 'not a term')
        expect(model.get('uri')).toBe('')
      })
    })
  })

  describe('AccessLimitation', () => {
    it('sets the URI, availability and description of the chosen limitation', () => {
      const limitation = new AccessLimitation()
      limitation.set('value', 'embargoed')

      expect(limitation.get('uri')).toBe('http://vocab.nerc.ac.uk/collection/N07/current/EMBG/')
      expect(limitation.get('availability')).toBe('Embargoed')
      expect(limitation.get('description')).toMatch(/^Access to the resource is restricted until/)
    })

    it('has a URI and an availability for every limitation it offers', () => {
      const { uris, availability } = AccessLimitation.prototype
      Object.keys(uris).forEach(value => expect(availability[value], value).toBeDefined())
    })

    it('clears them for a limitation it doesn\'t know', () => {
      const limitation = new AccessLimitation()
      limitation.set('value', 'embargoed')
      limitation.set('value', 'something else')

      expect(limitation.get('uri')).toBeNull()
      expect(limitation.get('availability')).toBeNull()
      expect(limitation.get('description')).toBeNull()
    })
  })

  describe('InfrastructureCategory', () => {
    it('sets the URI, class and description of the chosen category', () => {
      const category = new InfrastructureCategory()
      category.set('value', 'labsTest')

      expect(category.get('uri')).toBe('http://vocabs.ceh.ac.uk/ri/labsTest')
      expect(category.get('infrastructureClass')).toBe('Analytical facilities')
      expect(category.get('description')).toBe('Test labs')
    })

    it('clears them for a category it doesn\'t know', () => {
      const category = new InfrastructureCategory()
      category.set('value', 'labsTest')
      category.set('value', 'nonsense')

      expect(category.get('uri')).toBeNull()
      expect(category.get('infrastructureClass')).toBeNull()
    })
  })
})
