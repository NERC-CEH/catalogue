import { afterEach, beforeEach, describe, expect, it, onTestFinished, vi } from 'vitest'
import $ from 'jquery'
import L from 'leaflet'
import leafletCss from 'leaflet/dist/leaflet.css?raw'
import { WmsMapView } from '../src'
import { getLegendUrl } from '../src/wmsRequests'
import { copyText } from '../../clipboard/copyText'
import { CAPABILITIES_XML, capabilitiesXml } from './capabilitiesFixture'
import { fakeServer } from '../../test/support'

vi.mock('../../clipboard/copyText', () => ({ copyText: vi.fn() }))

// The markup templates/html/dataResource/_mapPreview.ftlh renders
const service = ({ id = 'abc', title = 'Land cover', layerNames = [] } = {}) => `
  <div class="wms-service" data-title="${title}" data-local-path="/maps/${id}"
       data-public-url="https://catalogue.ceh.ac.uk/maps/${id}"
       data-capabilities-url="https://catalogue.ceh.ac.uk/maps/${id}?SERVICE=WMS&amp;REQUEST=GetCapabilities">
    ${layerNames.map(name => `<span class="wms-layer-name">${name}</span>`).join('')}
  </div>`

const preview = ({ services = [service()], extents = '', style = '' } = {}) => `
  <div class="wms-map-preview" style="${style}">
    ${extents}
    ${services.join('')}
    <div class="wms-map-unavailable d-none">Map preview unavailable.</div>
    <div class="wms-map" style="height: 300px; width: 400px"></div>
    <div class="wms-layers"></div>
    <details class="wms-gis-help">
      <summary><h3 class="h5">Use this map in your GIS</h3></summary>
      <strong class="wms-layer-label"></strong>
      <div class="input-group">
        <input id="wms-capabilities-url" type="text" readonly class="wms-capabilities-url" value="https://catalogue.ceh.ac.uk/maps/abc?SERVICE=WMS&amp;REQUEST=GetCapabilities">
        <button type="button" class="btn">Copy</button>
      </div>
      <label class="wms-getmap-label" for="wms-getmap-url">GetMap for this view (British National Grid, EPSG:27700)</label>
      <div class="input-group">
        <input id="wms-getmap-url" type="text" readonly class="wms-getmap-url">
        <button type="button" class="btn">Copy</button>
      </div>
    </details>
  </div>`

describe('WmsMapView', () => {
  let $el
  let view

  beforeEach(() => fakeServer.install())

  afterEach(() => {
    view?.remove()
    view = null
    $el?.remove()
    fakeServer.uninstall()
    vi.mocked(copyText).mockReset()
  })

  // Starts the view and answers each capabilities request in turn
  const start = (html, ...responses) => {
    $el = $(html).appendTo(document.body)
    view = new WmsMapView({ el: $el[0] })
    fakeServer.requests().forEach((request, i) => request.respondWith(responses[i] || { status: 200, contentType: 'text/xml', responseText: CAPABILITIES_XML }))
    return view.ready
  }
  const radioLabels = () => view.$('.wms-layers label').map((i, label) => $(label).text()).get()
  const getMap = () => view.$('.wms-getmap-url').val()
  const getMapLabel = () => view.$('.wms-getmap-label').text()
  const headings = () => view.$('.wms-layers .wms-service-title').map((i, h) => $(h).text()).get()

  it('builds the picker from the capabilities, first layer drawn', async () => {
    await start(preview())

    expect(fakeServer.requests()[0].url).toContain('/maps/abc?')
    expect(fakeServer.requests()[0].url).toContain('REQUEST=GetCapabilities')
    expect(radioLabels()).toEqual(['Woodland', 'Conifer'])
    expect(view.$('.wms-layers input[type=radio]').first().prop('checked')).toBe(true)
    expect(view.wmsLayer.wmsParams.layers).toBe('LC.A')
    expect(view.wmsLayer._url).toBe('/maps/abc')
    expect(view.map.hasLayer(view.wmsLayer)).toBe(true)
  })

  it('lists only the layers the record allows', async () => {
    await start(preview({ services: [service({ layerNames: ['LC.B'] })] }))

    expect(radioLabels()).toEqual(['Conifer'])
    expect(view.wmsLayer.wmsParams.layers).toBe('LC.B')
  })

  it('lists every layer when none the record names is offered', async () => {
    await start(preview({ services: [service({ layerNames: ['NOPE'] })] }))

    expect(radioLabels()).toEqual(['Woodland', 'Conifer'])
  })

  it('groups the layers by service when there are several', async () => {
    await start(preview({ services: [service({ title: 'First' }), service({ id: 'def', title: 'Second' })] }))

    expect(headings()).toEqual(['First', 'Second'])
    expect(radioLabels()).toEqual(['Woodland', 'Conifer', 'Woodland', 'Conifer'])
  })

  it('leaves out a service whose capabilities fail, without headings for the one left', async () => {
    await start(preview({ services: [service({ title: 'First' }), service({ id: 'def', title: 'Second' })] }), { status: 500, responseText: '' })

    expect(headings()).toEqual([])
    expect(radioLabels()).toEqual(['Woodland', 'Conifer'])
    expect(view.wmsLayer._url).toBe('/maps/def')
    expect(view.$('.wms-map-unavailable').hasClass('d-none')).toBe(true)
  })

  it('updates the layer, GetMap, legend, label and capabilities on a layer change', async () => {
    await start(preview({ services: [service({ title: 'First' }), service({ id: 'def', title: 'Second' })] }))

    view.$('.wms-layers input[type=radio]').eq(3).prop('checked', true).trigger('change')

    expect(view.wmsLayer.wmsParams.layers).toBe('LC.B')
    expect(view.wmsLayer._url).toBe('/maps/def')
    expect(getMap().startsWith('https://catalogue.ceh.ac.uk/maps/def?')).toBe(true)
    expect(getMap()).toContain('LAYERS=LC.B')
    expect(view.$('.wms-legend img').attr('src')).toBe(getLegendUrl('/maps/def', 'LC.B'))
    expect(view.$('.wms-layer-label').text()).toBe('LC.B')
    expect(view.$('.wms-capabilities-url').val()).toBe('https://catalogue.ceh.ac.uk/maps/def?SERVICE=WMS&REQUEST=GetCapabilities')
  })

  it('shows the legend in a map control titled Legend, not in the picker', async () => {
    await start(preview())

    const $legend = view.$('.wms-legend')
    expect($legend.closest('.leaflet-control-container').length).toBe(1)
    expect(view.$('.wms-layers .wms-legend').length).toBe(0)
    expect($legend.find('.wms-control-title').text()).toBe('Legend')
    expect($legend.find('img').attr('src')).toBe(getLegendUrl('/maps/abc', 'LC.A'))
    expect($legend.find('img').attr('alt')).toBe('Legend for Woodland')
  })

  it('offers the basemaps under a Basemap title, and None takes the basemap off', async () => {
    await start(preview())

    const $control = view.$('.leaflet-control-layers')
    const basemapLabels = () => $control.find('.leaflet-control-layers-base label').map((i, label) => $(label).text().trim()).get()
    const tileLayers = () => Object.values(view.map._layers).filter(layer => layer instanceof L.TileLayer && !(layer instanceof L.TileLayer.WMS))
    expect($control.find('.wms-control-title').text()).toBe('Basemap')
    expect(basemapLabels()).toEqual(['Map', 'Satellite', 'None'])
    expect(tileLayers().length).toBe(1)

    $control.find('.leaflet-control-layers-base input').eq(2).trigger('click')

    expect(tileLayers()).toEqual([])
    expect(view.map.hasLayer(view.wmsLayer)).toBe(true)
    expect($control.find('.wms-control-title').text()).toBe('Basemap')
  })

  it('keeps the WMS layer drawn above whichever basemap is chosen, however often it changes', async () => {
    // The page has Leaflet's stylesheet (via the SCSS bundle), which sets the panes' stacking; specs do not
    const $leafletCss = $('<style>').text(leafletCss).appendTo(document.head)
    onTestFinished(() => $leafletCss.remove())
    await start(preview())
    const basemapInputs = () => view.$('.leaflet-control-layers-base input')
    const zIndexOf = pane => Number(getComputedStyle(pane).zIndex)
    // Panes stack by z-index; layers within one pane by their own z-index, then by DOM order
    const wmsDrawnAboveBasemap = () => {
      const wms = view.wmsLayer.getContainer()
      const basemaps = Object.values(view.map._layers)
        .filter(layer => layer instanceof L.TileLayer && layer !== view.wmsLayer)
        .map(layer => layer.getContainer())
      return basemaps.every(basemap => {
        if (wms.parentElement !== basemap.parentElement) {
          return zIndexOf(wms.parentElement) > zIndexOf(basemap.parentElement)
        }
        const byZ = Number(wms.style.zIndex) - Number(basemap.style.zIndex)
        return byZ > 0 || (byZ === 0 && !!(basemap.compareDocumentPosition(wms) & Node.DOCUMENT_POSITION_FOLLOWING))
      })
    }

    for (const choice of [1, 2, 0, 2, 1, 0]) {
      basemapInputs().eq(choice).trigger('click')
      expect(wmsDrawnAboveBasemap()).toBe(true)
    }
    view.$('.wms-layers input[type=radio]').eq(1).prop('checked', true).trigger('change')
    expect(wmsDrawnAboveBasemap()).toBe(true)
  })

  it('leaves the GIS help collapsed, still kept up to date for when it is opened', async () => {
    await start(preview())

    expect(view.$('.wms-gis-help').prop('open')).toBe(false)
    expect(getMap()).toContain('LAYERS=LC.A')
    expect(view.$('.wms-layer-label').text()).toBe('LC.A')
  })

  it('hides the legend when it fails to load, and shows it again for the next layer', async () => {
    await start(preview())

    view.$('.wms-legend img').trigger('error')
    expect(view.$('.wms-legend').hasClass('d-none')).toBe(true)

    view.$('.wms-layers input[type=radio]').eq(1).prop('checked', true).trigger('change')
    expect(view.$('.wms-legend').hasClass('d-none')).toBe(false)
  })

  it('starts on the record extent', async () => {
    const extent = '<span class="wms-extent" data-west="-3" data-east="-2" data-south="54" data-north="55"></span>'
    await start(preview({ extents: extent }))

    const centre = view.map.getCenter()
    expect(centre.lat).toBeCloseTo(54.5, 0)
    expect(centre.lng).toBeCloseTo(-2.5, 0)
  })

  it('starts on the layer bounds when the record has no extent', async () => {
    await start(preview())

    const centre = view.map.getCenter()
    expect(centre.lat).toBeCloseTo(55, 0)
    expect(centre.lng).toBeCloseTo(-3, 0)
  })

  it('starts on Great Britain when neither has bounds', async () => {
    await start(preview({ services: [service({ layerNames: ['LC.B'] })] }))

    const centre = view.map.getCenter()
    expect(centre.lat).toBeGreaterThan(49.8)
    expect(centre.lat).toBeLessThan(60.9)
    expect(centre.lng).toBeGreaterThan(-8.7)
    expect(centre.lng).toBeLessThan(1.8)
  })

  it('updates the GetMap bounding box when the map moves', async () => {
    await start(preview())
    const before = getMap().match(/BBOX=[^&]+/)[0]

    view.map.setView([52, -1], 10, { animate: false })
    view.map.fire('moveend')

    expect(getMap().match(/BBOX=[^&]+/)[0]).not.toBe(before)
    expect(getMap()).toContain('WIDTH=400&HEIGHT=300')
  })

  it('names the CRS in the label and switches CRS when panning from Great Britain to France', async () => {
    await start(preview())
    view.map.setView([52, -1], 8, { animate: false })
    view.map.fire('moveend')

    expect(getMap()).toContain('&CRS=EPSG:27700&')
    expect(getMapLabel()).toBe('GetMap for this view (British National Grid, EPSG:27700)')

    view.map.setView([47, 2.5], 8, { animate: false })
    view.map.fire('moveend')

    expect(getMap()).toContain('&CRS=EPSG:3035&')
    expect(getMapLabel()).toBe('GetMap for this view (EPSG:3035)')
  })

  it('keeps British National Grid for Great Britain in a wide map', async () => {
    await start(preview())
    view.$('.wms-map').css({ width: '1600px', height: '300px' })
    view.map.invalidateSize()
    view.map.fitBounds([[49.8, -8.7], [60.9, 1.8]], { animate: false })
    view.map.fire('moveend')

    expect(view.map.getBounds().getWest()).toBeLessThan(-9.01)
    expect(getMap()).toContain('&CRS=EPSG:27700&')
  })

  it('leaves GetMap empty when the layer lists no supported CRS', async () => {
    await start(preview(), { status: 200, contentType: 'text/xml', responseText: capabilitiesXml({ crs: ['EPSG:4258'] }) })

    expect(view.wmsLayer.wmsParams.layers).toBe('LC.A')
    expect(getMap()).toBe('')
    expect(getMapLabel()).toBe('GetMap for this view')
  })

  it('shows the map is unavailable if drawing it fails', async () => {
    const error = new Error('boom')
    const createMap = vi.spyOn(WmsMapView.prototype, 'createMap').mockImplementation(() => { throw error })
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {})
    try {
      await expect(start(preview())).resolves.toBeUndefined()

      expect(consoleError).toHaveBeenCalledWith(error)
      expect(view.$('.wms-map-unavailable').hasClass('d-none')).toBe(false)
      expect(view.$('.wms-map').hasClass('d-none')).toBe(true)
      expect(view.$('.wms-layers').hasClass('d-none')).toBe(true)
    } finally {
      createMap.mockRestore()
      consoleError.mockRestore()
    }
  })

  it('leaves GetMap empty while the map has no size', async () => {
    await start(preview({ style: 'display: none' }))

    expect(getMap()).toBe('')
  })

  it('shows the map is unavailable when the capabilities fail', async () => {
    await start(preview(), { status: 500, responseText: '' })

    expect(view.$('.wms-map-unavailable').hasClass('d-none')).toBe(false)
    expect(view.$('.wms-map').hasClass('d-none')).toBe(true)
    expect(view.$('.wms-layers').hasClass('d-none')).toBe(true)
    expect(view.$('.wms-capabilities-url').val()).toBe('https://catalogue.ceh.ac.uk/maps/abc?SERVICE=WMS&REQUEST=GetCapabilities')
    expect(view.map).toBeUndefined()
  })

  it('shows the map is unavailable when the capabilities name no layers', async () => {
    await start(preview(), { status: 200, contentType: 'text/xml', responseText: '<WMS_Capabilities/>' })

    expect(view.$('.wms-map-unavailable').hasClass('d-none')).toBe(false)
  })

  it('copies with the clipboard helper and says so', async () => {
    vi.mocked(copyText).mockResolvedValue(true)
    await start(preview({ style: 'display: none' }), { status: 500, responseText: '' })
    const $copy = view.$('.wms-capabilities-url').siblings('button')

    $copy.trigger('click')
    await vi.waitFor(() => expect($copy.text()).toBe('Copied'))

    expect(copyText).toHaveBeenCalledWith(view.$('.wms-capabilities-url')[0])
  })

  it('copies the GetMap URL, then says Copy again', async () => {
    vi.mocked(copyText).mockResolvedValue(true)
    await start(preview())
    vi.useFakeTimers()
    try {
      const $copy = view.$('.wms-getmap-url').siblings('button')

      $copy.trigger('click')
      await vi.waitFor(() => expect($copy.text()).toBe('Copied'))
      vi.advanceTimersByTime(2000)

      expect(copyText).toHaveBeenCalledWith(view.$('.wms-getmap-url')[0])
      expect($copy.text()).toBe('Copy')
    } finally {
      vi.useRealTimers()
    }
  })

  it('does not claim a copy that failed', async () => {
    vi.mocked(copyText).mockResolvedValue(false)
    await start(preview())
    const $copy = view.$('.wms-getmap-url').siblings('button')

    $copy.trigger('click')
    await vi.waitFor(() => expect(copyText).toHaveBeenCalled())
    await Promise.resolve()

    expect($copy.text()).toBe('Copy')
  })
})
