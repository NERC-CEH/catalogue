import $ from 'jquery'
import L from 'leaflet'
import SearchApp from '../src/SearchApp'
import SearchAppView from '../src/SearchAppView'
import SearchRouter from '../src/SearchRouter'
import { MessageView } from '../src/views'
import { fakeClock, fakeServer, spy } from '../../test/support'

// The search page as initSearch puts it together, on the markup templates/html/search.ftlh renders:
// what a visitor's changes search for, what comes back on the page, the address bar kept in step,
// the links and tabs, and an area drawn on the map. The server is faked (dri-one #473).
//
// The page searches a second after the last change, through underscore's debounce. That reads a
// Date.now it copied when it loaded, so no fake clock can move it on: the specs run the queued search
// as the debounce would, with flushSearch(), and one waits the real second.
describe('The search page', () => {
  let app, view
  const original = window.location.href
  const appUrl = () => window.location.href.split('#')[0].split('?')[0]

  // The page as the server renders it with no results of its own (search.ftlh)
  const markup = `
    <div id="message-panel" style="display: none"></div>
    <div class="search facets-mode container" id="search">
      <div class="filters">
        <ul class="nav nav-tabs" role="tablist">
          <li class="nav-item"><a class="nav-link active" href="#facet-tab" data-bs-toggle="tab">Filters</a></li>
          <li class="nav-item search-tab map-tab"><a class="nav-link" href="#spatial-tab" data-bs-toggle="tab">Spatial search</a></li>
        </ul>
        <div class="tab-content">
          <div class="tab-pane active" id="facet-tab"><div class="facet-filter"></div></div>
          <div class="tab-pane" id="spatial-tab"><div class="mapsearch"></div></div>
        </div>
      </div>
      <form class="search-form" action="/documents" method="get">
        <input placeholder="Search…" name="term" type="text" class="form-control">
        <button class="btn" type="submit" aria-label="Search"></button>
      </form>
      <div class="results"></div>
    </div>`

  // What the server answers, as the local stack answered ?rows=2&page=2
  const answer = (titles = ['UK Upland Waters Monitoring Network', 'COSMOS-UK soil moisture'], extra = {}) => ({
    numFound: 26,
    page: 2,
    rows: 2,
    order: 'asc',
    url: `${appUrl()}?page=2&rows=2`,
    prevPage: `${appUrl()}?rows=2`,
    nextPage: `${appUrl()}?page=3&rows=2`,
    results: titles.map((title, i) => ({
      identifier: `record-${i}`,
      title,
      state: 'published',
      resourceType: 'Dataset',
      documentType: 'GEMINI_DOCUMENT',
      shortenedDescription: `About ${title}`,
      locations: ['POLYGON((-8.6 49.8, -8.6 60.8, 1.7 60.8, 1.7 49.8, -8.6 49.8))']
    })),
    facets: [{
      displayName: 'Topic',
      fieldName: 'topic',
      results: [{ active: false, count: 2, name: 'Biodiversity', url: `${appUrl()}?facet=topic%7C0/Biodiversity/` }]
    }],
    ...extra
  })
  const respond = (request, body) => request.respondWith({ status: 200, contentType: 'application/json', responseText: JSON.stringify(body) })
  const requested = () => new URL(fakeServer.lastRequest().url, window.location.href).searchParams
  const searchRuns = () => app.flushSearch()
  const wait = milliseconds => new Promise(resolve => setTimeout(resolve, milliseconds))
  const shown = () => $('.results .result__title').map((i, title) => title.textContent.trim()).get()

  // As initSearch does, with the address the visitor arrived at
  function open (query = '', markupBefore = markup) {
    window.history.replaceState(null, '', window.location.pathname + query)
    $(document.body).html(markupBefore)
    app = new SearchApp()
    view = new SearchAppView({ model: app })
    view.facetsPanelView.createFacetSearch(app)
    new SearchRouter({ model: app, location: window.location }) // eslint-disable-line no-new
    new MessageView({ model: app }) // eslint-disable-line no-new
  }

  beforeEach(() => fakeServer.install())

  afterEach(() => {
    view.facetsPanelView.destroyFacetSearch()
    // Opening the map tab fits the map to it a moment later, which would find the map removed
    view.spatialFilterView.map.invalidateSize = () => {}
    view.spatialFilterView.map.remove()
    app.debouncedFlush.cancel()
    fakeServer.uninstall()
    window.history.replaceState(null, '', original)
    $(document.body).empty()
  })

  describe('searching', () => {
    it('searches a second after the last change, and shows what comes back', async () => {
      open()
      const searches = spyOn(app, 'searchNow').and.callThrough()

      app.set('term', 'up')
      await wait(500)
      app.set('term', 'upland')
      await wait(800)
      expect(searches).not.toHaveBeenCalled()
      await wait(400)

      expect(searches).toHaveBeenCalledTimes(1)
      expect(requested().get('term')).toBe('upland')
      respond(fakeServer.lastRequest(), answer())
      expect(shown()).toEqual(['UK Upland Waters Monitoring Network', 'COSMOS-UK soil moisture'])
      expect($('.recordCount-total').text()).toBe('26')
      expect($('.facet-filter select.search-facet option').text()).toBe('Biodiversity')
    })

    it('goes back to the first page when the search changes, but keeps a page that was chosen', () => {
      open()

      app.set('page', 3)
      searchRuns()
      expect(requested().get('page')).toBe('3')

      app.set('term', 'upland')
      searchRuns()
      expect(app.get('page')).toBe(1)
      expect(requested().has('page')).toBeFalse()
    })

    it('doesn\'t search for a change that isn\'t part of the search', () => {
      open()

      $('.map-tab').trigger('click')
      searchRuns()

      expect(app.get('mapsearch')).toBeTrue()
      expect(fakeServer.requestCount()).toBe(0)
    })

    it('sends each chosen facet as a parameter of its own', () => {
      open()

      app.setState({ facet: ['topic|Soil', 'licence|OGL'] })
      searchRuns()

      expect(requested().getAll('facet')).toEqual(['topic|Soil', 'licence|OGL'])
    })

    it('clears the old results while searching, and shows only the newest search when an older answer comes late', () => {
      open()
      app.set('term', 'rain')
      searchRuns()
      respond(fakeServer.lastRequest(), answer(['Rainfall 1960-2020']))
      expect(shown()).toEqual(['Rainfall 1960-2020'])

      app.set('term', 'river')
      searchRuns()
      expect($('.results').is(':empty')).toBeTrue()
      const slow = fakeServer.lastRequest()
      app.set('term', 'river flow')
      searchRuns()
      const newest = fakeServer.lastRequest()
      respond(slow, answer(['River Thames']))
      expect($('.results').is(':empty')).withContext('still waiting for the newest search').toBeTrue()
      respond(newest, answer(['River flows']))

      expect(shown()).toEqual(['River flows'])
    })

    it('suggests related searches, as links separated by commas', () => {
      open()

      app.set('term', 'soil')
      searchRuns()
      respond(fakeServer.lastRequest(), answer(undefined, {
        relatedSearches: [{ href: `${appUrl()}?term=soil+moisture`, title: 'Soil moisture' }, { href: `${appUrl()}?term=peat`, title: 'Peat' }]
      }))

      const $related = $('.results__related_searches')
      expect($related.find('h3').text()).toBe('Related Searches')
      expect($related.text()).toContain('Soil moisture, Peat')
      expect($related.find('a').map((i, link) => link.getAttribute('href')).get()).toEqual([`${appUrl()}?term=soil+moisture`, `${appUrl()}?term=peat`])
    })

    it('says why a search failed, from the server\'s reason', () => {
      open()

      app.set('term', 'upland')
      searchRuns()
      fakeServer.lastRequest().respondWith({ status: 502, contentType: 'application/json', responseText: '{"message":"Semantic search is unavailable"}' })

      expect($('#message-panel').is(':visible')).toBeTrue()
      expect($('#message-panel .alert-danger').text()).toContain('Semantic search is unavailable')
    })
  })

  describe('the address', () => {
    it('follows the search, so it can be bookmarked or shared', () => {
      open()

      app.setState({ term: 'upland', facet: ['topic|Soil', 'topic|Water'], page: 2 })

      const params = new URL(window.location.href).searchParams
      expect(params.get('term')).toBe('upland')
      expect(params.get('page')).toBe('2')
      expect(params.getAll('facet')).toEqual(['topic|(Soil OR Water)'])
    })

    it('is the search on arrival: shown in the box, and searched', () => {
      open('?term=rain&page=2')

      expect($('.search-form input[name=term]').val()).toBe('rain')
      searchRuns()
      expect(requested().get('term')).toBe('rain')
      expect(requested().get('page')).toBe('2')
    })

    it('isn\'t searched again on arrival when the server has rendered its results', () => {
      open('?term=rain', markup.replace('<div class="results"></div>', '<div class="results"><input type="hidden" id="num-records" value="1"></div>'))

      searchRuns()

      expect(app.get('term')).toBe('rain')
      expect(fakeServer.requestCount()).toBe(0)
    })

    it('is updated by the router where the browser can\'t replace the address in place', () => {
      open()
      window.history.replaceState = undefined
      try {
        const router = new SearchRouter({ model: app, location: window.location })
        spyOn(router, 'navigate')

        app.set('term', 'upland')

        expect(router.navigate).toHaveBeenCalledWith('term=upland', { replace: true })
      } finally {
        delete window.history.replaceState
      }
    })
  })

  describe('links', () => {
    it('follows a link within the search, such as the next page, without leaving', () => {
      open()
      app.set('term', 'upland')
      searchRuns()
      respond(fakeServer.lastRequest(), answer())

      const click = $.Event('click')
      $('a[aria-label=Next]').trigger(click)

      expect(click.isDefaultPrevented()).toBeTrue()
      expect(app.get('page')).toBe(3)
      expect(app.get('rows')).toBe(2)
      expect(app.get('term')).toBeUndefined()
    })

    it('clears the search for a link to the search page itself', () => {
      open('?term=rain')
      $('.results').append(`<a id="clear" href="${appUrl()}">Clear</a>`)

      $('#clear').trigger('click')

      expect(app.get('term')).toBeUndefined()
      expect(window.location.search).toBe('')
    })
  })

  describe('the map', () => {
    const rectangle = () => L.rectangle([[50, -3.5], [53, 1.8]])

    it('fits itself to its tab once the tab is shown', () => {
      open()
      const invalidateSize = spyOn(view.spatialFilterView.map, 'invalidateSize')
      fakeClock.install()
      try {
        $('.map-tab').trigger('click')
        expect(invalidateSize).not.toHaveBeenCalled()
        fakeClock.tick(1)

        expect(invalidateSize).toHaveBeenCalled()
      } finally {
        fakeClock.uninstall()
      }
    })

    it('searches the area drawn on it, then offers to delete it instead of drawing another', () => {
      open()
      const spatial = view.spatialFilterView
      const before = spatial.drawControl

      spatial.map.fire(L.Draw.Event.CREATED, { layer: rectangle() })
      searchRuns()

      expect(requested().get('bbox')).toBe('-3.500,1.800,53.000,50.000')
      expect(requested().get('op')).toBe('intersects')
      expect(spatial.drawnItems.getLayers().length).toBe(1)
      expect(spatial.drawControl).not.toBe(before)
      expect(spatial.drawControl.options.draw.rectangle).toBeFalse()
      expect(spatial.drawControl.options.edit.remove).toBeTruthy()
    })

    it('searches without the area once it is deleted, and offers drawing again', () => {
      open('?bbox=-3.5,1.8,53,50&op=iswithin')
      const spatial = view.spatialFilterView
      expect(spatial.drawnItems.getLayers().length).toBe(1)
      expect($('#opWithin').prop('checked')).toBeTrue()

      spatial.map.fire(L.Draw.Event.DELETED)
      searchRuns()

      expect(requested().has('bbox')).toBeFalse()
      expect(requested().has('op')).toBeFalse()
      // leaflet-draw turns rectangle: true into the rectangle's options
      expect(spatial.drawControl.options.draw.rectangle).toBeTruthy()
    })

    it('searches for records within the area rather than touching it, when chosen', () => {
      open('?bbox=-3.5,1.8,53,50')

      $('#opWithin').trigger('click')
      searchRuns()

      expect(app.get('op')).toBe('iswithin')
      expect(requested().get('op')).toBe('iswithin')
    })

    it('shows the operation chosen elsewhere, such as from the address', () => {
      open()

      app.set('op', 'iswithin')

      expect($('#opWithin').prop('checked')).toBeTrue()
      expect($('#opIntersects').prop('checked')).toBeFalse()
    })
  })

  describe('messages', () => {
    beforeEach(() => open())

    it('shows a message given as text', () => {
      app.trigger('error', 'Your session has expired')

      expect($('#message-panel .alert-danger').text()).toContain('Your session has expired')
    })

    it('falls back to a general message when the server gives no reason', () => {
      app.trigger('error', app, { status: 500 })
      app.trigger('error', app, null)

      expect($('#message-panel .alert-danger').map((i, alert) => alert.textContent.trim()).get())
        .toEqual(['A requested resource failed to load.', 'A requested resource failed to load.'])
    })

    it('shows information too, and hides the panel once the last message is dismissed', () => {
      app.trigger('info', 'Results are limited to 1000 records')
      app.trigger('error', 'Something failed')
      expect($('#message-panel .alert-info').text()).toContain('Results are limited to 1000 records')

      $('#message-panel .alert-info .fa-times').trigger('click')
      expect($('#message-panel').is(':visible')).toBeTrue()
      $('#message-panel .fa-times').trigger('click')

      expect($('#message-panel .alert').length).toBe(0)
      expect($('#message-panel').is(':visible')).toBeFalse()
    })
  })

  it('is told when results arrive, by an event on the search', () => {
    open()
    const synced = spy('results-sync')
    app.on('results-sync', synced)

    app.set('term', 'upland')
    searchRuns()
    respond(fakeServer.lastRequest(), answer())

    expect(synced).toHaveBeenCalledTimes(1)
  })
})
