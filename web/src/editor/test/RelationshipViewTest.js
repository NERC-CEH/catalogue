import RelationshipView, { clearRelationshipRules } from '../src/views/RelationshipView.js'
import { EditorMetadata } from '../src'
import $ from 'jquery'
import 'jquery-ui/ui/widgets/autocomplete'

describe('Test RelationshipView', function () {
  let model = null
  let view = null
  const options = [{ value: 'http://purl.org/dc/terms/relation', label: 'Relationship' }]

  beforeEach(function () {
    model = new EditorMetadata({ value: 'http://purl.org/dc/terms/relation', target: '' })
    view = new RelationshipView({ model, options })
    spyOn($, 'getJSON').and.callFake((url) => {
      return {
        title: 'title',
        id: 'uid-123',
        type: 'type'
      }
    })
  })

  it('test render', () => {
    // when
    view.render()
    // then
    expect(view.$('.relationshipSearch')).toBeDefined()
  })

  it('should set relationshipSearch to d-none if target exists', async () => {
    model = new EditorMetadata({ value: 'http://purl.org/dc/terms/relation', target: 'target' })
    view = new RelationshipView({ model, options })
    await view.render()
    expect(view.$('.relationshipSearch').hasClass('d-none')).toBeTrue()
  })

  it('should disable relationshipList if target exists', async () => {
    model = new EditorMetadata({ value: 'http://purl.org/dc/terms/relation', target: 'target' })
    view = new RelationshipView({ model, options })
    await view.render()
    expect(view.$('.relationshipList').prop('disabled')).toBeTrue()
  })

  it('should set relationshipRecord to d-none if target does not exist', async () => {
    model = new EditorMetadata({ value: 'http://purl.org/dc/terms/relation', target: '' })
    view = new RelationshipView({ model, options })
    await view.render()
    expect(view.$('.relationshipRecord').hasClass('d-none')).toBeTrue()
  })

  it('should enable autocomplete when relationship selected', async () => {
    await view.render()

    view.$('.relationshipList')
      .val('http://purl.org/dc/terms/relation')
      .trigger('change')

    expect(view.$('.autocomplete').prop('disabled')).toBeFalse()
  })

  it('should do correct http call for uid', async () => {
    model = new EditorMetadata({ value: 'http://purl.org/dc/terms/relation', target: 'exampleUid' })
    view = new RelationshipView({ model, options })
    await view.render()
    expect($.getJSON).toHaveBeenCalledWith('/documents/exampleUid')
  })

  it('should do correct http call for uri', async () => {
    model = new EditorMetadata({ value: 'http://purl.org/dc/terms/relation', target: 'http://exampleUri' })
    view = new RelationshipView({ model, options })
    await view.render()
    expect($.getJSON).toHaveBeenCalledWith('http://exampleUri')
  })

  describe('relationship-specific search filters', function () {
    const UTILISES = 'https://digital.ceh.ac.uk/ontology/doo/utilises'
    const REPLACES = 'http://purl.org/dc/terms/replaces'
    const RELATION = 'http://purl.org/dc/terms/relation'

    // What /relationships/rules serves for a dataset (see RelationshipRulesController)
    const datasetRules = {
      [REPLACES]: { resourceTypes: ['Dataset'], catalogues: [] },
      [UTILISES]: { resourceTypes: ['Monitoring facility', 'Monitoring network'], catalogues: ['eidc', 'ukceh'] }
    }

    beforeEach(function () {
      clearRelationshipRules()
      $('html').data('catalogue', 'eidc')
      $.getJSON.and.callFake(url => url.startsWith('/relationships/rules') ? datasetRules : { results: [] })
    })

    async function queryFor (relation, parent = new EditorMetadata({ id: 'cosmos', type: 'dataset' })) {
      const m = new EditorMetadata({ value: relation, target: '' })
      const v = new RelationshipView({ model: m, options: [{ value: relation, label: 'Relationship' }], parentModel: parent })
      // The autocomplete is bound during initialize and render() replaces the
      // input, so capture the source callback before rendering.
      const source = v.$('.autocomplete').autocomplete('option', 'source')
      await v.render()
      v.$('.relationshipList').val(relation)
      await source({ term: 'rainfall' }, () => {})
      return decodeURIComponent($.getJSON.calls.mostRecent().args[0])
    }

    it('fetches the rules for the record being edited', async () => {
      await queryFor(REPLACES)
      expect($.getJSON).toHaveBeenCalledWith('/relationships/rules?type=dataset')
    })

    it('filters on the indexed resourceType label, not the type key (dri-one #439)', async () => {
      const query = await queryFor(REPLACES)
      expect(query).toContain('resourceType:("Dataset")')
      expect(query).not.toContain('"dataset"')
    })

    it('searches the record\'s own catalogue when the rule names none', async () => {
      const query = await queryFor(REPLACES)
      expect(query).toMatch(/^\/eidc\/documents\?term=/)
    })

    it('searches the rule\'s catalogues across the whole catalogue for produced at (dri-one #439)', async () => {
      const query = await queryFor(UTILISES)
      expect(query).toMatch(/^\/documents\?term=/)
      expect(query).toContain('resourceType:("Monitoring facility" OR "Monitoring network")')
      expect(query).toContain('(catalogue:eidc OR catalogue_view:eidc OR catalogue:ukceh OR catalogue_view:ukceh)')
      expect(query).not.toContain('ukeof')
    })

    it('keeps the search term and excludes the record itself', async () => {
      const query = await queryFor(UTILISES)
      expect(query).toContain('(rainfall)')
      expect(query).toContain('NOT identifier:cosmos')
    })

    it('leaves an open relationship unfiltered', async () => {
      const query = await queryFor(RELATION)
      expect(query).not.toContain('resourceType:')
      expect(query).toMatch(/^\/eidc\/documents\?term=/)
    })

    it('takes a Gemini record\'s type from its resource type, which the editor can change', async () => {
      const parent = new EditorMetadata({ id: 'cosmos', type: 'dataset', resourceType: { value: 'service' } })
      await queryFor(REPLACES, parent)
      expect($.getJSON).toHaveBeenCalledWith('/relationships/rules?type=service')
    })

    it('falls back to an unfiltered search if the rules cannot be fetched', async () => {
      spyOn(console, 'error')
      $.getJSON.and.callFake(url => url.startsWith('/relationships/rules') ? Promise.reject(new Error('down')) : { results: [] })
      const query = await queryFor(REPLACES)
      expect(query).not.toContain('resourceType:')
      expect(console.error).toHaveBeenCalled()
    })
  })
})
