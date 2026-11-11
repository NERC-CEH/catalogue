import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import $ from 'jquery'
import Backbone from 'backbone'
import {
  AuthorView, ContactView, FundingView, KeywordVocabularyView, ModelInfoView, NercModelInfoView, RightsHolderView,
  SupportingDocView
} from '../src/views'
import { Contact, SupportingDoc } from '../src/models'
import { fakeServer } from '../../test/support'
import { autocompleteOf } from '../src/utils/autocomplete'

// The editor's other look-up boxes, driven through the real look-up box (utils/autocomplete.js)
// against a fake server: what each searches for as the editor types, what it offers, and what
// choosing one puts in the record (dri-one #473, #533)
describe('The editor\'s look-up boxes', () => {
  const json = body => ({ status: 200, contentType: 'application/json', responseText: JSON.stringify(body) })
  const requested = () => decodeURIComponent(fakeServer.lastRequest().url)
  const settle = () => new Promise(resolve => setTimeout(resolve, 0))
  const options = $input => [...autocompleteOf($input).menu.querySelectorAll('[role=option]')]
  const offered = $input => options($input).map(option => option.textContent)
  const choose = ($input, index = 0) => options($input)[index].click()
  // Searches as typing does once it pauses: the request goes at once, the answer comes later, once
  // jQuery has passed the response on (through timers of its own, so waiting on the search itself)
  let searching = null
  const search = ($input, term) => { $input.val(term); searching = autocompleteOf($input).search() }
  const answer = async response => { fakeServer.lastRequest().respondWith(response); await searching }
  const place = view => { $(document.body).append(view.el); return view }

  beforeEach(() => {
    fakeServer.install()
    $('html').data('catalogue', 'eidc')
  })

  afterEach(() => {
    fakeServer.uninstall()
    $('html').removeData('catalogue')
    $('.autocomplete-menu').remove()
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

    // The two "York University"s, as ROR has them (dri-one #417)
    const yorks = [
      {
        id: 'https://ror.org/022jz8688',
        name: 'York University',
        aliases: ['York College'],
        city: 'York',
        region: 'Nebraska',
        country: 'United States',
        countryCode: 'US',
        types: ['education'],
        website: 'https://york.edu'
      },
      {
        id: 'https://ror.org/05fq50484',
        name: 'York University',
        city: 'Toronto',
        region: 'Ontario',
        country: 'Canada',
        countryCode: 'CA',
        types: ['education', 'funder'],
        website: 'https://www.yorku.ca'
      }
    ]

    pickers.forEach(([who, View, Model, name, identifier], index) => {
      describe(`for ${who}`, () => {
        let model, view, $input

        beforeEach(() => {
          model = new Model()
          view = place(new View({ model, index: 'c1' }))
          $input = view.$('.orgAutocomplete')
        })

        it('searches organisations by what has been typed', async () => {
          search($input, ' ecology ')

          expect(requested()).toBe('/organisation/names?query=ecology')
          await answer(json(ror))
          expect(offered($input)).toEqual(['UK Centre for Ecology & Hydrology', 'Natural Environment Research Council'])
        })

        it('keeps the organisation chosen, by name and identifier', async () => {
          search($input, 'ecology')
          await answer(json(ror))

          choose($input)

          expect(model.get(name)).toBe('UK Centre for Ecology & Hydrology')
          expect(model.get(identifier)).toBe('https://ror.org/00pggkr55')
          expect($input.val()).toBe('UK Centre for Ecology & Hydrology')
          const $identifier = view.$(`[data-name=${identifier}]`)
          expect($identifier.val()).toBe('https://ror.org/00pggkr55')
        })

        it('forgets the identifier once the name is typed over, so a stale one isn\'t kept', async () => {
          search($input, 'ecology')
          await answer(json(ror))
          choose($input)

          $input.val('UK Centre for Ecology').trigger('input')

          expect(model.get(identifier)).toBe('')
          expect(view.$(`[data-name=${identifier}]`).val()).toBe('')
        })

        it('stops searching when the look-up fails, rather than spinning', async () => {
          search($input, 'ecology')
          await answer({ status: 503, contentType: 'text/plain', responseText: '' })

          expect(offered($input)).toEqual([])
          expect($input.attr('aria-busy')).toBeUndefined()
        })

        it('tells same-named organisations apart by where they are, what they are and their website', async () => {
          search($input, 'york university')
          await answer(json(yorks))

          expect(offered($input)).toEqual([
            'York UniversityYork, Nebraska, United States · Education · york.edu',
            'York UniversityToronto, Ontario, Canada · Education, Funder · yorku.ca'
          ])
          expect(options($input)[0].querySelector('.org-details').textContent).toBe('York, Nebraska, United States · Education · york.edu')
        })

        it('says where the organisation chosen is, beneath its name', async () => {
          const $location = () => view.$('.org-location')
          expect($location().prop('hidden')).toBe(true)
          search($input, 'york university')
          await answer(json(yorks))

          choose($input, 1)

          expect($location().text()).toBe('Toronto, Ontario, Canada · Education, Funder · yorku.ca')
          expect($location().prop('hidden')).toBe(false)
          expect($input.attr('aria-describedby')).toContain($location().attr('id'))

          $input.val('York Univ').trigger('input')

          expect($location().text()).toBe('')
          expect($location().prop('hidden')).toBe(true)
        })

        it('says where the organisation a record already has is, looking it up by its identifier', async () => {
          // An identifier of its own, as organisations seen are remembered from one picker to the next
          const id = `https://ror.org/0${index}test417`
          view.remove()
          model = new Model({ [name]: 'University of York', [identifier]: id })
          view = place(new View({ model, index: 'c2' }))

          expect(requested()).toBe(`/organisation/names?id=${id}`)
          fakeServer.lastRequest().respondWith(json([{
            id, name: 'University of York', city: 'York', region: 'England', country: 'United Kingdom', types: ['education', 'funder'], website: 'https://www.york.ac.uk'
          }]))
          await vi.waitFor(() => expect(view.$('.org-location').text()).toBe('York, England, United Kingdom · Education, Funder · york.ac.uk'))
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

    it('searches the ticked vocabularies, and says which each keyword comes from', async () => {
      search($input, 'soil')

      expect(requested()).toBe('/vocabulary/keywords?query=soil&vocab=gemet,envThes')
      await answer(json([{ label: 'soil', vocabId: 'gemet', url: 'http://www.eionet.europa.eu/gemet/concept/7843' }]))
      expect(offered($input)).toEqual(['soil (gemet)'])
    })

    it('leaves out a vocabulary once it is unticked', () => {
      view.$('.vocabularyPicker .vocab').first().find('input').trigger('change')

      search($input, 'soil')

      expect(requested()).toBe('/vocabulary/keywords?query=soil&vocab=envThes')
    })

    it('keeps the keyword chosen with its URI, and locks them', async () => {
      search($input, 'soil')
      await answer(json([{ label: 'soil', vocabId: 'gemet', url: 'http://www.eionet.europa.eu/gemet/concept/7843' }]))

      choose($input)

      expect(model.get('value')).toBe('soil')
      expect(model.get('uri')).toBe('http://www.eionet.europa.eu/gemet/concept/7843')
      expect(view.$('.value').val()).toBe('soil')
      expect(view.$('.value').prop('disabled')).toBe(true)
      expect(view.$('.uri').prop('disabled')).toBe(true)
      expect(view.$('.keywordPicker').hasClass('d-none')).toBe(true)
    })

    it('opens a saved keyword locked, without the search', () => {
      const saved = place(new KeywordVocabularyView({
        model: new Backbone.Model({ value: 'soil', uri: 'http://www.eionet.europa.eu/gemet/concept/7843' }),
        vocabs: { eidc: ['gemet'] }
      }))

      expect(saved.$('.keywordPicker').hasClass('d-none')).toBe(true)
      expect(saved.$('.uri').prop('disabled')).toBe(true)
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
          search($input, '')

          expect(requested()).toBe(`/eidc/documents?term=documentType:${documentType}`)
        })

        it('searches only those records, however the search is worded', () => {
          search($input, 'JULES OR documentType:GEMINI_DOCUMENT')

          expect(requested()).toBe(`/eidc/documents?term=documentType:${documentType} AND (JULES OR documentType:GEMINI_DOCUMENT)`)
        })

        it('keeps the model chosen, by title and identifier', async () => {
          search($input, 'jules')
          await answer(json({ results: [{ title: 'JULES', identifier: 'abc' }, { title: 'LUCI', identifier: 'def' }] }))
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

    it('offers the formats as soon as the box is focused, and keeps the one chosen', async () => {
      $format.trigger('focus')
      await settle()

      expect(offered($format)).toEqual(['docx (preferred)', 'txt', 'csv'])
      choose($format, 1)
      expect(model.get('format')).toBe('txt')
    })

    it('records which contents it has, from tick boxes ticked as saved', () => {
      const box = value => view.$(`input[type=checkbox][value=${value}]`)
      expect(view.$('input[type=checkbox]').length).toBe(9)
      expect(box('qc').prop('checked')).toBe(true)

      box('dataStructure').prop('checked', true).trigger('change')
      box('qc').prop('checked', false).trigger('change')

      expect(model.get('content')).toEqual(['dataStructure'])
    })
  })
})
