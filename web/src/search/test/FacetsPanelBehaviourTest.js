import $ from 'jquery'
import Backbone from 'backbone'
import { FacetsPanelView } from '../src/views'
import { spy } from '../../test/support'

// The search page's facet filters: each facet's choices as a multi-select box (select2), and choosing
// or removing one moves the search to that choice's address (dri-one #473)
describe('FacetsPanelView', () => {
  let view, model

  const facets = [
    {
      displayName: 'Topic',
      results: [
        { name: 'Hydrology', url: '/eidc/documents?facet=topic%7CHydrology', active: true },
        { name: 'Soil', url: '/eidc/documents?facet=topic%7CSoil', active: false },
        { name: 'Unknown', url: '/eidc/documents?facet=topic%7CUnknown', active: false }
      ]
    },
    { displayName: 'Licence', results: [{ name: 'OGL', url: '/eidc/documents?facet=licence%7COGL&page=2', active: false }] },
    { displayName: 'Empty', results: [] }
  ]

  beforeEach(() => {
    model = new Backbone.Model()
    model.getResults = () => new Backbone.Model({ facets })
    model.setState = spy('setState')
    view = new FacetsPanelView({ el: $('<div>').appendTo(document.body), model })
  })

  afterEach(() => {
    view.destroyFacetSearch()
    $(document.body).empty()
  })

  it('shows each facet with results as a multi-select box of its choices, the active ones chosen', () => {
    view.render()

    const $selects = view.$('select.search-facet')
    expect($selects.length).toBe(2)
    expect(view.$('.facet label').first().text()).toBe('Filter topic')
    expect($selects.first().find('option').map((i, option) => option.textContent).get()).toEqual(['Hydrology', 'Soil'])
    expect($selects.first().val()).toEqual(['/eidc/documents?facet=topic%7CHydrology'])
    expect($selects.first().data('select2')).toBeDefined()
  })

  it('shows a chosen name in full, brackets and all', () => {
    facets[0].results[1].name = 'Natural Environment Research Council (NERC)'
    view.render()

    const select2Options = view.$('select.search-facet').first().data('select2').options.options
    expect(select2Options.templateSelection({ text: 'Natural Environment Research Council (NERC)' })).toBe('Natural Environment Research Council (NERC)')
    facets[0].results[1].name = 'Soil'
  })

  it('moves the search to a choice\'s address when it is chosen', () => {
    view.render()

    view.$('select.search-facet').first().trigger({ type: 'select2:select', params: { data: { id: '/eidc/documents?facet=topic%7CSoil' } } })

    expect(model.setState).toHaveBeenCalledWith({ facet: 'topic|Soil' })
  })

  it('moves the search back when a choice is removed, without reopening the list', () => {
    view.render()
    const $select = view.$('select.search-facet').first()

    $select.trigger({ type: 'select2:unselect', params: { data: { id: '/eidc/documents?facet=licence%7COGL&page=2' } } })
    const opening = $.Event('select2:opening')
    $select.trigger(opening)

    expect(model.setState).toHaveBeenCalledWith({ facet: 'licence|OGL', page: 2 })
    expect(opening.isDefaultPrevented()).toBeTrue()
    const nextOpening = $.Event('select2:opening')
    $select.trigger(nextOpening)
    expect(nextOpening.isDefaultPrevented()).toBeFalse()
  })

  it('clears the search state for an address with no query', () => {
    expect(view.handleMultiSelect('/eidc/documents', model)).toBeFalse()
    expect(model.setState).toHaveBeenCalledWith({})
  })

  it('redraws when new results arrive, without doubling up the boxes or their handlers', () => {
    view.render()
    model.trigger('results-sync')

    expect(view.$('select.search-facet').length).toBe(2)
    expect(view.$('.select2-container').length).toBe(2)
    view.$('select.search-facet').first().trigger({ type: 'select2:select', params: { data: { id: '/eidc/documents?facet=topic%7CSoil' } } })
    expect(model.setState).toHaveBeenCalledTimes(1)
  })

  it('leaves alone a box that is already a multi-select', () => {
    view.render()
    const before = view.$('select.search-facet').first().data('select2')

    view.createFacetSearch(model)

    expect(view.$('select.search-facet').first().data('select2')).toBe(before)
  })
})
