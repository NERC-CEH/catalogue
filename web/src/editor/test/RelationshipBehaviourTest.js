import { afterEach, beforeEach, describe, expect, it } from 'vitest'
import $ from 'jquery'
import RelationshipView, { clearRelationshipRules, recordUrl } from '../src/views/RelationshipView'
import {
  CodeDocumentEditorView, GeminiEditorView, InfrastructureRecordEditorView, MonitoringActivityEditorView,
  MonitoringFacilityEditorView, MonitoringNetworkEditorView, MonitoringProgrammeEditorView, ResearchActivityEditorView
} from '../src/editors'
import { EditorMetadata, GeminiEditorMetadata } from '../src'
import { fakeServer } from '../../test/support'

// The relationship picker as an editor uses it: the record search it sends, through the real
// autocomplete, for the kind of record being edited, what it offers, and the record it then shows
// (dri-one #473)
describe('Relationships in the editor', () => {
  const PART_OF = 'http://purl.org/dc/terms/isPartOf'
  const UTILISES = 'https://digital.ceh.ac.uk/ontology/doo/utilises'
  const REPLACES = 'http://purl.org/dc/terms/replaces'

  const settle = () => new Promise(resolve => setTimeout(resolve, 0))
  const json = body => ({ status: 200, contentType: 'application/json', responseText: JSON.stringify(body) })
  const url = () => decodeURIComponent(fakeServer.lastRequest().url)

  // Answers the request the view is waiting on, after checking what it asked for
  async function answer (expected, body) {
    await settle()
    expect(url()).toMatch(expected)
    fakeServer.lastRequest().respondWith(json(body))
    await settle()
  }

  beforeEach(() => {
    fakeServer.install()
    clearRelationshipRules()
    $('html').data('catalogue', 'eidc')
  })

  afterEach(() => {
    fakeServer.uninstall()
    $('html').removeData('catalogue')
    $('.relationship-autocomplete').remove()
    $(document.body).empty()
  })

  describe('picking a record', () => {
    let view, model, $search

    beforeEach(() => {
      model = new EditorMetadata({ target: '' })
      const parentModel = new GeminiEditorMetadata({ id: 'cosmos', type: 'dataset', resourceType: { value: 'dataset' } })
      view = new RelationshipView({
        el: $('<div>').appendTo(document.body),
        model,
        parentModel,
        options: [{ value: PART_OF, label: 'Part of' }, { value: UTILISES, label: 'Produced at' }]
      })
      $search = view.$('.autocomplete')
    })

    it('waits for a relationship before searching', () => {
      expect($search.prop('disabled')).toBe(true)
      expect($search.attr('placeholder')).toBe('Choose a relationship type first')

      view.$('.relationshipList').val(PART_OF).trigger('change')

      expect($search.prop('disabled')).toBe(false)
      expect($search.attr('placeholder')).toBe('Enter record ID or type to search…')
      expect(model.get('relation')).toBe(PART_OF)
    })

    it('searches the record\'s own catalogue for only the kinds of record the relationship allows', async () => {
      view.$('.relationshipList').val(PART_OF).trigger('change')

      $search.val('rain').autocomplete('search')
      await answer(/^\/relationships\/rules\?type=dataset$/, { [PART_OF]: { resourceTypes: ['Aggregation'] } })

      expect(url()).toMatch(/^\/eidc\/documents\?term=/)
      expect(url()).toContain('resourceType:("Aggregation")')
      expect(url()).toContain('(rain)')
      expect(url()).toContain('NOT identifier:cosmos')
    })

    // A menu that can't be chosen from, saying why: the search's outcome either way, where an empty
    // menu looked like no search at all
    const notice = () => $('.relationship-autocomplete li')

    it('says so when what the relationship can link to can\'t be loaded, rather than offering anything', async () => {
      // Unfiltered, the search offered records the save then rejected, from the record's own
      // catalogue only: on staging and production the proxy had no route for the rules
      view.$('.relationshipList').val(UTILISES).trigger('change')
      $search.val('blue lough').autocomplete('search')
      await settle()
      expect(url()).toMatch(/^\/relationships\/rules/)
      fakeServer.lastRequest().respondWith({ status: 404, responseText: 'Not Found' })
      await settle()

      expect(url()).toMatch(/^\/relationships\/rules/) // no search sent
      expect(notice().length).toBe(1)
      expect(notice().text()).toContain('couldn\'t be loaded')
      expect(notice().hasClass('ui-state-disabled')).toBe(true)
    })

    it('asks again for what the relationship can link to on the next search', async () => {
      view.$('.relationshipList').val(UTILISES).trigger('change')
      $search.val('blue lough').autocomplete('search')
      await settle()
      fakeServer.lastRequest().respondWith({ status: 404, responseText: 'Not Found' })
      await settle()

      $search.val('blue lough 2').autocomplete('search')
      await answer(/^\/relationships\/rules/, { [UTILISES]: { resourceTypes: ['Monitoring facility'], catalogues: ['eidc', 'ukceh'] } })

      expect(url()).toMatch(/^\/documents\?term=/)
    })

    it('says when nothing matches', async () => {
      view.$('.relationshipList').val(PART_OF).trigger('change')
      $search.val('nothing like this').autocomplete('search')
      await answer(/^\/relationships\/rules/, { [PART_OF]: { resourceTypes: ['Aggregation'] } })
      await answer(/^\/eidc\/documents\?term=/, { results: [] })

      expect(notice().length).toBe(1)
      expect(notice().text()).toBe('No matching records')
      expect(notice().hasClass('ui-state-disabled')).toBe(true)
    })

    it('says when the search itself fails', async () => {
      view.$('.relationshipList').val(PART_OF).trigger('change')
      $search.val('rain').autocomplete('search')
      await answer(/^\/relationships\/rules/, { [PART_OF]: { resourceTypes: ['Aggregation'] } })
      fakeServer.lastRequest().respondWith({ status: 500, responseText: 'boom' })
      await settle()

      expect(notice().text()).toContain('The search failed')
      expect(notice().hasClass('ui-state-disabled')).toBe(true)
    })

    it('offers what the search finds, saying where each comes from, and shows the one chosen', async () => {
      view.$('.relationshipList').val(UTILISES).trigger('change')
      $search.val('cosmos').autocomplete('search')
      await answer(/^\/relationships\/rules/, { [UTILISES]: { resourceTypes: ['Monitoring facility'], catalogues: ['eidc', 'ukceh'] } })
      expect(url()).toMatch(/^\/documents\?term=/)
      await answer(/^\/documents\?term=/, { results: [{ identifier: '640ccee2', title: 'COSMOS at Wimpole', resourceType: 'Monitoring facility', catalogue: 'ukceh' }] })
      await answer(/^\/catalogues$/, [{ id: 'ukceh', title: 'UKCEH digital assets' }])

      const $items = $('.relationship-autocomplete li')
      expect($items.length).toBe(1)
      expect($items.text()).toContain('COSMOS at Wimpole (Monitoring facility)')
      expect($items.find('span').text()).toBe('UKCEH digital assets · 640ccee2')

      $items.first().find('div').trigger('click')
      $search.trigger('change')
      await answer(/^\/documents\/640ccee2$/, { title: 'COSMOS at Wimpole', type: 'monitoringFacility', id: '640ccee2' })

      expect(model.get('target')).toBe('640ccee2')
      expect(view.$('.read-only-identifier').val()).toBe('COSMOS at Wimpole (monitoringFacility, 640ccee2)')
      expect(view.$('.relationshipList').prop('disabled')).toBe(true)
      expect(view.$('.relationshipSearch').hasClass('d-none')).toBe(true)
    })
  })

  // Each kind of relationship is offered with what it means beneath its name, which the styled
  // select sets on a second line and leaves out of the closed select. Both are escaped, but the
  // line between them is markup, and escaping the two as one showed its <span> as text.
  describe('the kinds of relationship offered', () => {
    let view
    const option = value => view.$(`.relationshipList option[value="${value}"]`)

    beforeEach(async () => {
      view = new RelationshipView({
        el: $('<div>').appendTo(document.body),
        model: new EditorMetadata({ target: '' }),
        options: [
          { value: PART_OF, label: 'Part of', description: 'This dataset is one of a series' },
          { value: UTILISES, label: 'Produced <at>' }
        ]
      })
      await settle()
    })

    it('set what each means beneath its name', () => {
      const $description = option(PART_OF).children('span')

      expect($description.length).toBe(1)
      expect($description.text()).toBe('This dataset is one of a series')
      expect(option(PART_OF).text()).toBe('Part ofThis dataset is one of a series')
    })

    it('escape the name and what it means', () => {
      expect(option(UTILISES).text()).toBe('Produced <at>')
      expect(option(UTILISES).children().length).toBe(0)
    })
  })

  describe('a record already chosen', () => {
    const shows = async (target, fetched) => {
      const view = new RelationshipView({
        el: $('<div>').appendTo(document.body),
        model: new EditorMetadata({ relation: PART_OF, target }),
        options: [{ value: PART_OF, label: 'Part of' }]
      })
      await answer(fetched, { title: 'Rainfall', type: 'dataset', id: 'abc' })
      return view.$('.read-only-identifier').val()
    }

    it('is named from this catalogue when stored as an id', async () => {
      expect(await shows('abc', /^\/documents\/abc$/)).toBe('Rainfall (dataset, abc)')
      expect($('.target-problem').prop('hidden')).toBe(true)
    })

    it('is named from this catalogue when stored as the catalogue\'s /id/ URI', async () => {
      expect(await shows('https://catalogue.ceh.ac.uk/id/abc', /^\/documents\/abc$/)).toBe('Rainfall (dataset, abc)')
    })

    it('is named from this catalogue when stored as the catalogue\'s /documents/ URI', async () => {
      expect(await shows('https://catalogue.ceh.ac.uk/documents/abc', /^\/documents\/abc$/)).toBe('Rainfall (dataset, abc)')
    })

    // A target is stored as given, through the API too, so it can be any URL. Fetched with $.getJSON,
    // one with `=?` in it became JSONP, and ran as script in the editor of whoever opened the record
    // (dri-one #441). Only this catalogue's own records are looked up.
    describe('on another site', () => {
      const stored = async target => {
        const before = fakeServer.requests().length
        const view = new RelationshipView({
          el: $('<div>').appendTo(document.body),
          model: new EditorMetadata({ relation: PART_OF, target }),
          options: [{ value: PART_OF, label: 'Part of' }]
        })
        await settle()
        await settle()
        return { view, requested: fakeServer.requests().slice(before).map(request => request.url) }
      }
      const scripts = () => $('script[src*="evil.example"]').length

      it('is shown as stored, without asking that site for it', async () => {
        const { view, requested } = await stored('https://doi.org/10.5285/abc')

        expect(requested).toEqual([])
        expect(view.$('.read-only-identifier').val()).toBe('https://doi.org/10.5285/abc')
        expect(view.$('.target-problem').prop('hidden')).toBe(true)
      })

      it('is never loaded as a script, whatever its address asks for', async () => {
        const { requested } = await stored('https://evil.example/x.js?callback=?')

        expect(requested).toEqual([])
        expect(scripts()).toBe(0)
      })

      it('is never loaded as a script when made to look like one of this catalogue\'s own', async () => {
        const { requested } = await stored('https://catalogue.ceh.ac.uk/id/abc?callback=?')

        expect(requested).toEqual([])
        expect(scripts()).toBe(0)
      })
    })

    // Shown only as its id, a link to a missing record looked like any other (dri-one #465). The
    // stored target is kept, so the record can still be saved or the relationship removed.
    describe('that can\'t be fetched', () => {
      const failing = async (target, fetched, status) => {
        const model = new EditorMetadata({ relation: PART_OF, target })
        const view = new RelationshipView({
          el: $('<div>').appendTo(document.body),
          model,
          options: [{ value: PART_OF, label: 'Part of' }]
        })
        await settle()
        expect(url()).toMatch(fetched)
        fakeServer.lastRequest().respondWith({ status, contentType: 'application/json', responseText: '{}' })
        await settle()
        return { field: view.$('.read-only-identifier'), problem: view.$('.target-problem'), model }
      }

      it('says the record isn\'t there, or can\'t be seen, when stored as an id', async () => {
        const { field, problem, model } = await failing('c70f2c9a', /^\/documents\/c70f2c9a$/, 404)

        // Beneath the field, which a narrow column cut short: the id stays in it
        expect(field.val()).toBe('c70f2c9a')
        expect(problem.text()).toBe('Record not found, or you don\'t have access to it')
        expect(problem.prop('hidden')).toBe(false)
        expect(field.hasClass('is-invalid')).toBe(true)
        expect(model.get('target')).toBe('c70f2c9a')
      })

      it('says the same when it can\'t be seen, stored as a URI, as the save check does', async () => {
        // One message for both, as RelationshipTargets doesn't say which a target is
        const { field, problem, model } = await failing('https://catalogue.ceh.ac.uk/id/c70f2c9a', /^\/documents\/c70f2c9a$/, 403)

        expect(field.val()).toBe('https://catalogue.ceh.ac.uk/id/c70f2c9a')
        expect(problem.text()).toBe('Record not found, or you don\'t have access to it')
        expect(field.hasClass('is-invalid')).toBe(true)
        expect(model.get('target')).toBe('https://catalogue.ceh.ac.uk/id/c70f2c9a')
      })

      it('says only that its details couldn\'t be loaded when the server fails, not that the link is broken', async () => {
        const { field, problem, model } = await failing('c70f2c9a', /^\/documents\/c70f2c9a$/, 500)

        expect(field.val()).toBe('c70f2c9a')
        expect(problem.text()).toBe('Couldn\'t load the record\'s details')
        expect(problem.prop('hidden')).toBe(false)
        expect(field.hasClass('is-invalid')).toBe(false)
        expect(model.get('target')).toBe('c70f2c9a')
      })
    })
  })

  describe('where a stored target is fetched from', () => {
    it('fetches this catalogue\'s records from this catalogue, whatever the environment', () => {
      expect(recordUrl('abc')).toBe('/documents/abc')
      expect(recordUrl('https://catalogue.ceh.ac.uk/id/abc')).toBe('/documents/abc')
      expect(recordUrl('http://catalogue.ceh.ac.uk/documents/abc')).toBe('/documents/abc')
    })

    it('fetches nothing for another site\'s record, or for a value that isn\'t an id (dri-one #441)', () => {
      expect(recordUrl('https://doi.org/10.5285/abc')).toBeNull()
      expect(recordUrl('https://evil.example/x.js?callback=?')).toBeNull()
      expect(recordUrl('https://catalogue.ceh.ac.uk/id/abc?callback=?')).toBeNull()
      expect(recordUrl('../maintenance')).toBeNull()
    })
  })

  // Each editor with relationships asks for the rules of the kind of record it edits, which is
  // what the search is then restricted by
  describe('each editor', () => {
    const editors = [
      ['Gemini', GeminiEditorView, GeminiEditorMetadata, { resourceType: { value: 'aggregate' } }, 'aggregate'],
      ['code', CodeDocumentEditorView, EditorMetadata, {}, 'computationalNotebook'],
      ['infrastructure', InfrastructureRecordEditorView, EditorMetadata, {}, 'infrastructureRecord'],
      ['monitoring activity', MonitoringActivityEditorView, EditorMetadata, {}, 'monitoringActivity'],
      ['monitoring facility', MonitoringFacilityEditorView, EditorMetadata, {}, 'monitoringFacility'],
      ['monitoring network', MonitoringNetworkEditorView, EditorMetadata, {}, 'monitoringNetwork'],
      ['monitoring programme', MonitoringProgrammeEditorView, EditorMetadata, {}, 'monitoringProgramme'],
      ['research activity', ResearchActivityEditorView, EditorMetadata, {}, 'researchActivity']
    ]

    editors.forEach(([name, Editor, Model, attributes, type]) => {
      it(`searches for the ${name} record's kind (${type})`, async () => {
        $(document.body).html('<div id="search"></div>')
        const editor = new Editor({ model: new Model(attributes, { mediaType: 'application/json' }), el: '#search' })
        const relationships = editor.sections.flatMap(section => section.views)
          .find(each => each.data?.ObjectInputView === RelationshipView)
        expect(relationships, 'has a relationships list').toBeDefined()

        relationships.$('button.add').trigger('click')
        const $row = relationships.$('.existing .relationshipList').last()
        const relation = $row.find('option').filter((i, option) => option.value === REPLACES).val() || $row.find('option[value!=""]').first().val()
        $row.val(relation).trigger('change')
        $row.closest('.row').find('.autocomplete').val('rain').autocomplete('search')
        await settle()

        expect(fakeServer.lastRequest().url).toBe(`/relationships/rules?type=${type}`)
      })
    })
  })
})
