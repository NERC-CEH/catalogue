import SearchAppView from '../src/SearchAppView'
import SearchApp from '../src/SearchApp'
import $ from 'jquery'

describe('Test SearchAppView', () => {
  let view = null

  beforeEach(() => {
    $(document.body).html('<div id=\'search\'><div class=\'spatial-filter\'><div class=\'map\'></div></div></div>')
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
  })
})
