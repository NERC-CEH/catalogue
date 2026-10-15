import $ from 'jquery'
import Backbone from 'backbone'
import {
  AuthorView, ContactView, FundingView, KeywordVocabularyView, ModelInfoView, NercModelInfoView, RightsHolderView,
  SupportingDocView
} from '../src/views'
import { Contact, SupportingDoc } from '../src/models'
import { fakeServer } from '../../test/support'

// The editor's other look-up boxes, driven through the real jQuery UI autocomplete against a fake
// server: what each searches for as the editor types, what it offers, and what choosing one puts in
// the record (dri-one #473)
describe('The editor\'s look-up boxes', () => {
  const json = body => ({ status: 200, contentType: 'application/json', responseText: JSON.stringify(body) })
  const requested = () => decodeURIComponent(fakeServer.lastRequest().url)
  const offered = $input => $input.autocomplete('widget').find('li').map((i, item) => item.textContent).get()
  const choose = ($input, index = 0) => $input.autocomplete('widget').find('li > div').eq(index).trigger('click')
  const place = view => { $(document.body).append(view.el); return view }

  beforeEach(() => {
    fakeServer.install()
    $('html').data('catalogue', 'eidc')
  })

  afterEach(() => {
    fakeServer.uninstall()
    $('html').removeData('catalogue')
    $('.ui-autocomplete').remove()
    $(document.body).empty()
  })

  // Contacts, funders, and a service agreement's authors and rights holders all look organisations
  // up in ROR, by name, and keep the one chosen as its name and identifier
  describe('organisations', () => {
    const pickers = [
      ['a contact', ContactView, Contact, 'organisationName', 'organisationIdentifier'],
      ['a funder', FundingView, Backbone.Model, 'funderName', 'funderIdentifier'],
      ['an author', AuthorView, Backbone.Model, 'organisationName', 'organisationIdentifier'],
      ['a rights holder', RightsHolderView, Backbone.Model, 'organisationName', 'organisationIdentifier']
    ]
    const ror = [
      { id: 'https://ror.org/00pggkr55', name: 'UK Centre for Ecology & Hydrology' },
      { id: 'https://ror.org/02wfhk785', name: 'Natural Environment Research Council' }
    ]

    pickers.forEach(([who, View, Model, name, identifier]) => {
      describe(`for ${who}`, () => {
        let model, view, $input

        beforeEach(() => {
          model = new Model()
          view = place(new View({ model, index: 'c1' }))
          $input = view.$('.orgAutocomplete')
        })

        it('searches organisations by what has been typed', () => {
          $input.val(' ecology ').autocomplete('search')

          expect(requested()).toBe('/organisation/names?query=ecology')
          fakeServer.lastRequest().respondWith(json(ror))
          expect(offered($input)).toEqual(['UK Centre for Ecology & Hydrology', 'Natural Environment Research Council'])
        })

        it('keeps the organisation chosen, by name and identifier', () => {
          $input.val('ecology').autocomplete('search')
          fakeServer.lastRequest().respondWith(json(ror))

          choose($input)

          expect(model.get(name)).toBe('UK Centre for Ecology & Hydrology')
          expect(model.get(identifier)).toBe('https://ror.org/00pggkr55')
          expect($input.val()).toBe('UK Centre for Ecology & Hydrology')
          const $identifier = view.$(`[data-name=${identifier}]`)
          expect($identifier.val()).toBe('https://ror.org/00pggkr55')
        })

        it('forgets the identifier once the name is typed over, so a stale one isn\'t kept', () => {
          $input.val('ecology').autocomplete('search')
          fakeServer.lastRequest().respondWith(json(ror))
          choose($input)

          $input.val('UK Centre for Ecology').trigger('input')

          expect(model.get(identifier)).toBe('')
          expect(view.$(`[data-name=${identifier}]`).val()).toBe('')
        })

        it('stops searching when the look-up fails, rather than spinning', () => {
          $input.val('ecology').autocomplete('search')
          fakeServer.lastRequest().respondWith({ status: 503, contentType: 'text/plain', responseText: '' })

          expect(offered($input)).toEqual([])
          expect($input.hasClass('ui-autocomplete-loading')).toBeFalse()
        })
      })
    })
  })

  describe('keywords', () => {
    let model, view, $input

    beforeEach(() => {
      model = new Backbone.Model()
      view = place(new KeywordVocabularyView({ model, vocabs: { eidc: ['gemet', 'envThes', 'not-a-vocabulary'] } }))
      $input = view.$('.keywordPicker .autocomplete')
    })

    it('offers a tick box for each of the catalogue\'s vocabularies it knows', () => {
      expect(view.$('.vocabularyPicker .vocab').length).toBe(2)
      expect(view.$('.vocabularyPicker').text()).toContain('GEMET')
      expect(view.$('.vocabularyPicker').text()).toContain('EnvThes')
    })

    it('searches the ticked vocabularies, and says which each keyword comes from', () => {
      $input.val('soil').autocomplete('search')

      expect(requested()).toBe('/vocabulary/keywords?query=soil&vocab=gemet,envThes')
      fakeServer.lastRequest().respondWith(json([{ label: 'soil', vocabId: 'gemet', url: 'http://www.eionet.europa.eu/gemet/concept/7843' }]))
      expect(offered($input)).toEqual(['soil (gemet)'])
    })

    it('leaves out a vocabulary once it is unticked', () => {
      view.$('.vocabularyPicker .vocab').first().find('input').trigger('change')

      $input.val('soil').autocomplete('search')

      expect(requested()).toBe('/vocabulary/keywords?query=soil&vocab=envThes')
    })

    it('keeps the keyword chosen with its URI, and locks them', () => {
      $input.val('soil').autocomplete('search')
      fakeServer.lastRequest().respondWith(json([{ label: 'soil', vocabId: 'gemet', url: 'http://www.eionet.europa.eu/gemet/concept/7843' }]))

      choose($input)

      expect(model.get('value')).toBe('soil')
      expect(model.get('uri')).toBe('http://www.eionet.europa.eu/gemet/concept/7843')
      expect(view.$('.value').val()).toBe('soil')
      expect(view.$('.value').prop('disabled')).toBeTrue()
      expect(view.$('.uri').prop('disabled')).toBeTrue()
      expect(view.$('.keywordPicker').hasClass('d-none')).toBeTrue()
    })

    it('opens a saved keyword locked, without the search', () => {
      const saved = place(new KeywordVocabularyView({
        model: new Backbone.Model({ value: 'soil', uri: 'http://www.eionet.europa.eu/gemet/concept/7843' }),
        vocabs: { eidc: ['gemet'] }
      }))

      expect(saved.$('.keywordPicker').hasClass('d-none')).toBeTrue()
      expect(saved.$('.uri').prop('disabled')).toBeTrue()
    })
  })

  // A model record names the models it uses, found among the catalogue's model records
  describe('models', () => {
    const pickers = [
      ['CEH models', ModelInfoView, 'CEH_MODEL'],
      ['NERC models', NercModelInfoView, 'nerc-model']
    ]

    pickers.forEach(([kind, View, documentType]) => {
      describe(`for ${kind}`, () => {
        let model, view, $input

        beforeEach(() => {
          model = new Backbone.Model()
          view = place(new View({ model, index: 'm1' }))
          $input = view.$('.autocomplete')
        })

        it(`lists the catalogue's ${documentType} records before anything is typed`, () => {
          $input.val('').autocomplete('search')

          expect(requested()).toBe(`/eidc/documents?term=documentType:${documentType}`)
        })

        it('searches only those records, however the search is worded', () => {
          $input.val('JULES OR documentType:GEMINI_DOCUMENT').autocomplete('search')

          expect(requested()).toBe(`/eidc/documents?term=documentType:${documentType} AND (JULES OR documentType:GEMINI_DOCUMENT)`)
        })

        it('keeps the model chosen, by title and identifier', () => {
          $input.val('jules').autocomplete('search')
          fakeServer.lastRequest().respondWith(json({ results: [{ title: 'JULES', identifier: 'abc' }, { title: 'LUCI', identifier: 'def' }] }))
          expect(offered($input)).toEqual(['JULES', 'LUCI'])

          choose($input, 1)
          $input.trigger('change')

          expect(model.get('id')).toBe('def')
          expect(model.get('name')).toBe('LUCI')
          expect(view.$('.identifier').val()).toBe('def')
        })
      })
    })
  })

  describe('a service agreement\'s supporting document', () => {
    let model, view, $format

    beforeEach(() => {
      model = new SupportingDoc({ content: ['qc'] })
      view = place(new SupportingDocView({ model, index: 's1' }))
      $format = view.$('.autocomplete')
    })

    it('offers the formats as soon as the box is focused, and keeps the one chosen', () => {
      $format.trigger('focus')

      expect(offered($format)).toEqual(['docx (preferred)', 'txt', 'csv'])
      choose($format, 1)
      expect(model.get('format')).toBe('txt')
    })

    it('records which contents it has, from tick boxes ticked as saved', () => {
      const box = value => view.$(`input[type=checkbox][value=${value}]`)
      expect(view.$('input[type=checkbox]').length).toBe(9)
      expect(box('qc').prop('checked')).toBeTrue()

      box('dataStructure').prop('checked', true).trigger('change')
      box('qc').prop('checked', false).trigger('change')

      expect(model.get('content')).toEqual(['dataStructure'])
    })
  })
})
