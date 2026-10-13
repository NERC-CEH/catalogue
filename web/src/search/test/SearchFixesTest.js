/* global beforeAll, afterAll */
import $ from 'jquery'
import SearchApp from '../src/SearchApp'
import SearchAppView from '../src/SearchAppView'
import { shareUrl } from '../src/views/SearchPageView'
import { MessageView } from '../src/views'
import facetsPanelTemplate from '../src/templates/facetsPanelTemplate'
import facetResultsTemplate from '../src/templates/facetResultsTemplate'
import searchPage from '../src/templates/searchPage'

/**
 * Search page fixes (NERC-CEH/dri-one#460): a failed search showed an empty pane, a search was
 * dropped when another setting changed within the debounce, every page loaded with parameters
 * searched again for results the server had already rendered, and several controls misbehaved.
 */
describe('Search page', () => {
  describe('searching', () => {
    let app

    beforeEach(() => {
      app = new SearchApp()
      spyOn(app, 'searchNow')
    })

    it('still searches when a non-search setting changes before the debounce fires', () => {
      app.set('term', 'soil')
      app.set('mapsearch', true) // e.g. the Spatial search tab, within the second

      app.flushSearch()

      expect(app.searchNow).toHaveBeenCalledTimes(1)
    })

    it('does not search again for results the server has already rendered', () => {
      app.setState({ term: 'soil' }, { serverRendered: true })

      app.flushSearch()

      expect(app.searchNow).not.toHaveBeenCalled()
    })

    it('searches once for several changes', () => {
      app.set('term', 'soil')
      app.set('page', 2)

      app.flushSearch()
      app.flushSearch()

      expect(app.searchNow).toHaveBeenCalledTimes(1)
    })
  })

  it('reports a failed search instead of leaving an empty pane', () => {
    $(document.body).html('<div id="message-panel"></div>')
    const app = new SearchApp()
    const messages = new MessageView({ model: app })

    app.getResults().trigger('error', app.getResults(), { status: 502, responseJSON: { message: 'Semantic search is unavailable' } })

    expect($('#message-panel .alert').text()).toContain('Semantic search is unavailable')
    messages.remove()
    $(document.body).empty()
  })

  it('dismisses a message with its close icon', () => {
    $(document.body).html('<div id="message-panel"></div>')
    const app = new SearchApp()
    const messages = new MessageView({ model: app })
    app.trigger('info', 'Hello')

    $('#message-panel .fa-times').trigger('click')

    expect($('#message-panel .alert').length).toBe(0)
    messages.remove()
    $(document.body).empty()
  })

  it('marks the initial state as rendered when the server has rendered results', () => {
    const original = window.location.href
    window.history.replaceState(null, '', '?term=soil')
    try {
      $(document.body).html('<div id="search"><span id="num-records">1</span></div>')
      const app = new SearchApp()
      spyOn(app, 'setState')
      spyOn(SearchAppView.prototype, 'render') // the map and forms need their full page
      new SearchAppView({ model: app }) // eslint-disable-line no-new

      expect(app.setState).toHaveBeenCalledWith(jasmine.objectContaining({ term: 'soil' }), { serverRendered: true })
    } finally {
      window.history.replaceState(null, '', original)
      $(document.body).empty()
    }
  })

  it('shares the search without paging, whatever order the parameters are in', () => {
    expect(shareUrl('https://catalogue.ceh.ac.uk/eidc/documents?page=2&term=soil&rows=20&facet=x'))
      .toBe('https://catalogue.ceh.ac.uk/eidc/documents?term=soil&facet=x')
  })

  it('shows "Number of citations" as the chosen sort', () => {
    const results = [{ identifier: 'r1', title: 'A', shortenedDescription: 'B', documentType: 'GEMINI_DOCUMENT', recordType: 'Dataset', state: 'published', url: '/documents/r1' }]
    const html = searchPage({ results, numFound: 1, page: 1, rows: 20, url: '/x', catalogueId: 'eidc', sortField: 'incomingCitationCount', order: 'desc', prevPage: null, nextPage: null })

    expect($('<div>').html(html).find('option[value="incomingCitationCount-desc"]').prop('selected')).toBe(true)
  })

  it('leaves out a facet with no values', () => {
    const html = facetsPanelTemplate({
      facets: [
        { displayName: 'Topic', fieldName: 'topic', results: [] },
        { displayName: 'Funder', fieldName: 'funder', results: [{ name: 'NERC', count: 1, url: '?facet=funder|NERC', active: false }] }
      ],
      template: facetResultsTemplate
    })

    expect($('<div>').html(html).find('.facet').length).toBe(1)
  })

  // Typing queues a debounced search, and pressing Enter straight after searched at once and then
  // again when the debounce fired, so a failed search showed its message twice
  describe('submitting a typed term', () => {
    it('searches once, not again when the debounce fires', async () => {
      const app = new SearchApp()
      spyOn(app.results.constructor.prototype, 'fetch') // no requests
      const searches = spyOn(app, 'createSearchPage').and.callThrough() // one per search, this app's only

      app.set('term', 'soil')
      app.searchNow()
      await new Promise(resolve => setTimeout(resolve, 1_200))

      expect(searches).toHaveBeenCalledTimes(1)
    })
  })

  // The panel is fixed just under the navbar, but the header is sticky with a z-index, so the
  // header painted over every message
  describe('the message panel', () => {
    let frame

    beforeAll(done => {
      frame = document.createElement('iframe')
      frame.style.width = '1200px'
      frame.style.height = '600px'
      frame.onload = () => done()
      frame.srcdoc = `<!doctype html><html><head><link rel="stylesheet" href="/base/build/test-css/style-eidc.css"></head>
        <body><header style="height: 300px">The header</header>
        <div id="message-panel" style="display: block"><div class="alert alert-danger">Search failed</div></div></body></html>`
      document.body.appendChild(frame)
    })

    afterAll(() => frame.remove())

    it('is shown above the header', () => {
      const doc = frame.contentDocument
      const box = doc.getElementById('message-panel').getBoundingClientRect()

      const topmost = doc.elementFromPoint(box.left + box.width / 2, box.top + box.height / 2)

      expect(topmost.closest('#message-panel')).not.toBeNull()
    })
  })
})
