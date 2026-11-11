import { beforeEach, describe, expect, it } from 'vitest'
import SearchAppView from '../src/SearchAppView'
import SearchApp from '../src/SearchApp'
import $ from 'jquery'

describe('Test SearchAppView', () => {
  let view = null

  beforeEach(() => {
    $(document.body).html('<div id=\'search\'><div class=\'mapsearch\'><div class=\'map\'></div></div></div>')
    const model = new SearchApp({ title: 'some text' })
    view = new SearchAppView({
      model
    })
  })

  it('View should be defined', () => {
    // then
    expect(view).toBeDefined()
  })

  it('Sub views should be defined and use the same model', () => {
    // then
    expect(view.searchFormView).toBeDefined()
    expect(view.searchResultsView).toBeDefined()
    expect(view.facetsPanelView).toBeDefined()
    expect(view.spatialFilterView).toBeDefined()

    expect(view.searchFormView.model.get('title')).toEqual('some text')
    expect(view.searchResultsView.model.get('title')).toEqual('some text')
    expect(view.facetsPanelView.model.get('title')).toEqual('some text')
    expect(view.spatialFilterView.model.get('title')).toEqual('some text')
  })

  it('starts from the search the form sent, its + read as a space (dri-one #532)', () => {
    const original = window.location.href
    window.history.replaceState(null, '', '?term=soil+carbon&facet=resourceType%7Cdataset&page=2')
    try {
      const model = new SearchApp()
      // eslint-disable-next-line no-new
      new SearchAppView({ model })

      expect(model.get('term')).toBe('soil carbon')
      expect(model.get('facet')).toBe('resourceType|dataset')
      expect(model.get('page')).toBe(2)
    } finally {
      window.history.replaceState(null, '', original)
    }
  })
})
