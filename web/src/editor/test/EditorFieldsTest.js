import $ from 'jquery'
import _ from 'underscore'
import Backbone from 'backbone'
import 'jquery-ui/ui/widgets/autocomplete'
import {
  ContactView,
  DataTypeProvenanceView,
  DatasetReferenceDateView,
  FundingView,
  LinkDocumentSelectorView,
  ModelInfoView,
  NercModelInfoView,
  ObservedPropertySimpleView,
  ObservedPropertyView
} from '../src/views'
import { Contact, InfrastructureChallenge, MultipleDate, PurposeOfCollection } from '../src/models'
import infrastructureChallengeTemplate from '../src/templates/InfrastructureChallenge'
import purposeOfCollectionTemplate from '../src/templates/PurposeOfCollection'
import KeywordVocabularyView from '../src/views/KeywordVocabularyView'
import MapReprojectionView from '../src/views/MapReprojectionView'
import { spy } from '../../test/support'

/**
 * Editor fields that did the wrong thing, or nothing at all (NERC-CEH/dri-one#458).
 */
describe('Editor fields', () => {
  // Calls an autocomplete's source as jQuery UI would, and returns the URL it asked for
  const autocompleteRequest = (view, term) => {
    const request = $.Deferred()
    spyOn($, 'getJSON').and.returnValue(request.promise())
    const response = spy('response')
    view.$('.ui-autocomplete-input').first().autocomplete('option', 'source')({ term }, response)
    return { url: $.getJSON.calls.mostRecent().args[0], request, response }
  }

  describe('a reprojection row', () => {
    it('is removed by its remove button', () => {
      const reprojections = new Backbone.Collection([{ epsgCode: '27700' }])
      const view = new MapReprojectionView({ model: reprojections.first() })

      view.$('button.remove').trigger('click')

      expect(reprojections.length).toBe(0)
    })
  })

  describe('an observed property', () => {
    [ObservedPropertyView, ObservedPropertySimpleView].forEach(View => {
      it(`keeps its edits to itself (${View === ObservedPropertyView ? 'full' : 'simple'})`, () => {
        // In the editor the row sits inside a FilesetView, whose own modify would copy the field
        const fileset = spy('fileset change')
        const $fileset = $('<div>').on('change', fileset)
        const view = new View({ model: new Backbone.Model({ constraints: {} }) })
        $fileset.append(view.el)

        view.$('[data-name="title"]').val('Air temperature').trigger('change')

        expect(view.model.get('title')).toBe('Air temperature')
        expect(fileset).not.toHaveBeenCalled()
      })
    })
  })

  describe('a contact with its own role options', () => {
    const roleoptions = [{ value: 'funder', label: 'Funder' }, { value: 'user', label: 'User' }]

    it('saves no role until one is chosen', () => {
      const view = new ContactView({ model: new Contact(), roleoptions })

      expect(view.model.get('role')).toBeUndefined()
      expect(view.$('.role-select').val()).toBe('')
      expect(view.$('.role-select option').first().text()).toBe('Select a role')
      expect(view.model.validate(view.model.attributes)).toContain({ message: 'Role is mandatory.' })
    })

    it('saves the role that is chosen', () => {
      const view = new ContactView({ model: new Contact(), roleoptions })

      view.$('.role-select').val('user').trigger('change')
      view.$('[data-name="organisationName"]').val('UKCEH').trigger('change')

      expect(view.model.get('role')).toBe('user')
    })

    it('shows the role a new row will be saved with, when the section uses the full list', () => {
      const view = new ContactView({ model: new Contact() })

      expect(view.model.get('role')).toBe('pointOfContact')
      expect(view.$('.role-select').val()).toBe('pointOfContact')
    })

    it('still defaults the role when the section has one', () => {
      const view = new ContactView({ model: new Contact(), roleDefault: 'publisher' })

      view.$('[data-name="organisationName"]').val('UKCEH').trigger('change')

      expect(view.model.get('role')).toBe('publisher')
    })
  })

  describe('a type-ahead', () => {
    const awkward = 'Centre for Ecology & Hydrology #1 ??'
    const encoded = encodeURIComponent(awkward)

    afterEach(() => { $('html').removeData('catalogue') })

    it('encodes the organisation being searched for', () => {
      const view = new ContactView({ model: new Contact() })

      const { url } = autocompleteRequest(view, awkward)

      expect(url).toBe(`/organisation/names?query=${encoded}`)
    })

    it('encodes the keyword being searched for', () => {
      $('html').data('catalogue', 'eidc')
      const view = new KeywordVocabularyView({ model: new Backbone.Model(), vocabs: { eidc: ['cast'] } })

      const { url } = autocompleteRequest(view, awkward)

      expect(url).toBe(`/vocabulary/keywords?query=${encoded}&vocab=cast`)
    })

    ;[[ModelInfoView, 'CEH_MODEL'], [NercModelInfoView, 'nerc-model']].forEach(([View, type]) => {
      it(`encodes the model being searched for (${type})`, () => {
        $('html').data('catalogue', 'eidc')
        const view = new View({ model: new Backbone.Model() })

        const { url } = autocompleteRequest(view, awkward)

        expect(url).toBe(`/eidc/documents?term=${encodeURIComponent(`documentType:${type} AND (${awkward})`)}`)
      })
    })

    it('answers with nothing when the search fails', () => {
      const view = new ContactView({ model: new Contact() })

      const { request, response } = autocompleteRequest(view, 'Centre (')
      request.reject()

      expect(response).toHaveBeenCalledWith([])
    })

    it('fills in the funder, not an organisation, on a funding row', () => {
      const view = new FundingView({ model: new Backbone.Model() })

      view.$('.orgAutocomplete').autocomplete('option', 'select')({}, { item: { label: 'NERC', url: 'https://ror.org/02b5d8509' } })

      expect(view.model.get('funderName')).toBe('NERC')
      expect(view.model.get('funderIdentifier')).toBe('https://ror.org/02b5d8509')
      expect(view.model.has('organisationName')).toBe(false)
    })
  })

  describe('the link document search', () => {
    let requests, view

    beforeEach(() => {
      requests = []
      spyOn($, 'getJSON').and.callFake((url, success) => {
        const request = $.Deferred()
        requests.push({ url, request })
        if (url.startsWith('/catalogues')) {
          success([{ id: 'eidc', title: 'EIDC' }])
        }
        return request.promise()
      })
      view = new LinkDocumentSelectorView({ model: new Backbone.Model({ id: 'rec1' }), modelAttribute: 'linkedDocumentId', label: 'Link' })
      requests = []
    })

    it('encodes the term', () => {
      view.$('#term').val('soil & water')
      view.search()

      expect(requests[0].url).toBe(`/eidc/documents?term=${encodeURIComponent('state:published AND view:public AND NOT documentType:LINK_DOCUMENT AND (soil & water)')}`)
    })

    it('ignores a response that arrives after a newer one', () => {
      view.$('#term').val('soil')
      view.search()
      view.$('#term').val('soil moisture')
      view.search()

      requests[1].request.resolve({ results: [{ identifier: 'new', title: 'Soil moisture', shortenedDescription: '' }] })
      requests[0].request.resolve({ results: [{ identifier: 'old', title: 'Soil', shortenedDescription: '' }] })

      expect(view.results.pluck('identifier')).toEqual(['new'])
    })

    it('waits for typing to pause before searching', async () => {
      view.$('#term').val('s').trigger('keyup')
      view.$('#term').val('so').trigger('keyup')
      view.$('#term').val('soil').trigger('keyup')
      expect(requests.length).toBe(0)

      await new Promise(resolve => setTimeout(resolve, 600))

      expect(requests.length).toBe(1)
      expect(requests[0].url).toContain(encodeURIComponent('(soil)'))
    })
  })

  describe('a date field', () => {
    [
      [DatasetReferenceDateView, ['creationDate', 'publicationDate', 'unavailableDate', 'releasedDate', 'supersededDate']],
      [DataTypeProvenanceView, ['creationDate', 'modificationDate']]
    ].forEach(([View, names]) => {
      names.forEach(name => {
        it(`never writes undefined while typing (${name})`, () => {
          const model = new MultipleDate()
          const view = new View({ model })

          view.$(`#input-${name}`).val('2026-10-13').trigger('input')

          expect(model.has(name) && model.get(name) === undefined).toBe(false)
          expect(_.values(model.attributes)).not.toContain(undefined)
        })
      })
    })

    it('validates a missing date without throwing, and names a withdrawn one', () => {
      const model = new MultipleDate()

      expect(() => model.validate({ creationDate: undefined })).not.toThrow()
      expect(model.validate({ unavailableDate: '13/10/2026' })[0].message).toBe('Date withdrawn is wrong. The date format is yyyy-mm-dd')
    })
  })

  describe('a vocabulary choice', () => {
    [
      ['purpose of collection', PurposeOfCollection, purposeOfCollectionTemplate],
      ['infrastructure challenge', InfrastructureChallenge, infrastructureChallengeTemplate]
    ].forEach(([name, Model, template]) => {
      it(`has a URI for every ${name} option`, () => {
        const values = $(`<div>${template({ data: {} })}</div>`).find('option').toArray().map(o => o.value).filter(v => v)

        values.forEach(value => {
          const model = new Model()
          model.set('value', value)
          expect(model.get('uri')).withContext(value).toMatch(/^https?:\/\//)
        })
      })
    })
  })
})
