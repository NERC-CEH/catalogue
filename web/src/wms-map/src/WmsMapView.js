import $ from 'jquery'
import Backbone from 'backbone'
import L from 'leaflet'
import '../../map/markerIcons'
import { copyText } from '../../clipboard/copyText'
import { chooseCrs, crsLabel, getCapabilitiesUrl, getLegendUrl, getMapUrl, parseCapabilities } from './wmsRequests'

const GREAT_BRITAIN = [[49.8, -8.7], [60.9, 1.8]]
const COPIED_FOR_MS = 2000
const WMS_PANE = 'wms'

// A service's named layers, kept to those the record names unless none of those is offered
const allowedLayers = (layers, allowedNames) => {
  const allowed = layers.filter(layer => allowedNames.includes(layer.name))
  return allowed.length ? allowed : layers
}

const SWATCH_SHAPES = ['area', 'line', 'point']

// Legend entries by WMS layer name, from the service's map data definition (see WmsLegends.java)
const legendsOf = $service => new Map($service.find('.wms-legend-layer').map((i, layer) => [[
  $(layer).attr('data-layer'),
  $(layer).find('.wms-legend-class').map((j, entry) => ({
    label: $(entry).text().trim(),
    colour: $(entry).attr('data-colour'),
    shape: $(entry).attr('data-shape')
  })).get()
]]).get())

// The selected layer's legend, on the map and collapsible from its title: the layer's entries when the
// service defines them, else its GetLegendGraphic, which is hidden while it fails to load
const LegendControl = L.Control.extend({
  options: { position: 'bottomright' },

  onAdd () {
    const container = L.DomUtil.create('details', 'leaflet-control leaflet-bar wms-legend')
    container.open = true
    $(container).append(
      $('<summary class="wms-control-title">Legend</summary>'),
      $('<ul class="wms-legend-entries list-unstyled mb-0"></ul>'),
      $('<img>').on('error', () => $(container).addClass('d-none'))
    )
    L.DomEvent.disableClickPropagation(container)
    L.DomEvent.disableScrollPropagation(container)
    return container
  }
})

const unionOf = boundsList => boundsList.reduce(
  (union, bounds) => union ? union.extend(bounds) : L.latLngBounds(bounds), null)

/**
 * The Map section of a record page: the record's own WMS on a Leaflet map, with the selected layer's
 * legend and a basemap choice as map controls, a picker for its layers, and the GetCapabilities and
 * GetMap addresses to use it in a GIS. The browser asks only for same-origin /maps/{id}; the copyable
 * addresses use the record's public host.
 * `ready` resolves once the capabilities have been fetched and the map (or its unavailable notice) shown;
 * it never rejects.
 */
export default Backbone.View.extend({
  events: {
    'change .wms-layers input[type=radio]': 'selectLayer',
    'click .wms-gis-help .input-group button': 'copy'
  },

  initialize () {
    this.copiedTimers = new Map()
    this.services = this.$('.wms-service').map((i, el) => {
      const $service = $(el)
      return {
        // attr, not data: data() would turn a title such as "2020" into a number
        title: $service.attr('data-title'),
        localPath: $service.attr('data-local-path'),
        publicUrl: $service.attr('data-public-url'),
        capabilitiesUrl: $service.attr('data-capabilities-url'),
        allowedNames: $service.find('.wms-layer-name').map((j, name) => $(name).text().trim()).get(),
        legends: legendsOf($service)
      }
    }).get()

    this.ready = Promise.all(this.services.map(service => this.fetchLayers(service)))
      .then(() => this.render())
      .catch(error => {
        console.error(error)
        this.showUnavailable()
      })
  },

  // A service whose capabilities fail to load or parse offers no layers
  fetchLayers (service) {
    return Promise.resolve($.ajax({ url: getCapabilitiesUrl(service.localPath), dataType: 'text' }))
      .then(xml => allowedLayers(parseCapabilities(xml), service.allowedNames), () => [])
      .then(layers => { service.layers = layers })
  },

  render () {
    this.choices = this.services.flatMap(service => service.layers.map(layer => ({ service, layer })))
    if (!this.choices.length) {
      this.showUnavailable()
      return this
    }
    this.renderPicker()
    this.createMap()
    this.showLayer(this.choices[0])
    return this
  },

  showUnavailable () {
    this.$('.wms-map-unavailable').removeClass('d-none')
    this.$('.wms-map, .wms-layers').addClass('d-none')
  },

  renderPicker () {
    const $layers = this.$('.wms-layers').empty()
    const grouped = this.services.filter(service => service.layers.length).length > 1
    this.choices.forEach(({ service, layer }, i) => {
      if (grouped && service.layers[0] === layer) {
        $layers.append($('<h3 class="h6 wms-service-title"></h3>').text(service.title))
      }
      const id = `${this.cid}-layer-${i}`
      $layers.append($('<div class="form-check"></div>').append(
        $('<input class="form-check-input" type="radio">')
          .attr({ id, name: `${this.cid}-layer`, value: i })
          .prop('checked', i === 0),
        $('<label class="form-check-label"></label>').attr('for', id).text(layer.title || layer.name)
      ))
    })
  },

  createMap () {
    this.map = new L.Map(this.$('.wms-map')[0])
    // Above the basemaps' tilePane (200), below overlays (400). In the tilePane a basemap re-added by the
    // layers control would come later in the DOM, or get a higher z-index from it, and cover the WMS layer
    this.map.createPane(WMS_PANE).style.zIndex = 250
    const baseMaps = {
      Map: L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 18,
        attribution: '&copy; <a href="https://openstreetmap.org/copyright">OpenStreetMap contributors</a>'
      }).addTo(this.map),
      Satellite: L.tileLayer('https://www.google.cn/maps/vt?lyrs=s@189&gl=cn&x={x}&y={y}&z={z}', {
        attribution: 'google'
      }),
      // An empty base layer, so choosing it leaves only the WMS layer on the map
      None: L.layerGroup()
    }
    const basemaps = L.control.layers(baseMaps, {}, { position: 'topright', collapsed: false }).addTo(this.map)
    // Into the section, which Leaflet keeps when it rebuilds the radio list
    $(basemaps.getContainer()).find('.leaflet-control-layers-list')
      .prepend('<div class="wms-control-title">Basemap</div>')
    // A closed legend has no widths to measure, so cut-off entries are found again when it opens
    $(new LegendControl().addTo(this.map).getContainer()).on('toggle', () => this.titleCutOffEntries())
    this.map.fitBounds(this.initialBounds())
    this.map.on('moveend zoomend resize', () => this.updateGetMap())
  },

  // The record's extent, else the first service's layer bounds, else Great Britain
  initialBounds () {
    const extents = this.$('.wms-extent').map((i, el) => {
      const { south, west, north, east } = $(el).data()
      return [[[south, west], [north, east]]]
    }).get()
    const layerBounds = this.choices[0].service.layers.map(layer => layer.bounds).filter(Boolean)
    return unionOf(extents) || unionOf(layerBounds) || L.latLngBounds(GREAT_BRITAIN)
  },

  selectLayer (event) {
    this.showLayer(this.choices[Number(event.target.value)])
  },

  showLayer (choice) {
    this.selected = choice
    const { service, layer } = choice
    if (this.wmsLayer) this.wmsLayer.remove()
    this.wmsLayer = L.tileLayer.wms(service.localPath, {
      layers: layer.name,
      format: 'image/png',
      transparent: true,
      version: '1.3.0',
      pane: WMS_PANE
    }).addTo(this.map)
    this.showLegend(service, layer)
    this.$('.wms-layer-label').text(layer.name)
    this.$('.wms-capabilities-url').val(service.capabilitiesUrl)
    this.updateGetMap()
  },

  showLegend (service, layer) {
    const entries = service.legends.get(layer.name) || []
    const $legend = this.$('.wms-legend').removeClass('d-none')
    const $entries = $legend.find('.wms-legend-entries').empty().toggleClass('d-none', !entries.length)
    const $image = $legend.find('img').toggleClass('d-none', entries.length > 0)
    if (!entries.length) {
      $image.attr({ src: getLegendUrl(service.localPath, layer.name), alt: `Legend for ${layer.title || layer.name}` })
      return
    }
    // Removing src, unlike emptying it, does not fire the image's error handler
    $image.removeAttr('src')
    entries.forEach(({ label, colour, shape }) => $entries.append($('<li class="wms-legend-entry"></li>').append(
      $('<span class="wms-legend-swatch"></span>')
        .addClass(`wms-legend-swatch-${SWATCH_SHAPES.includes(shape) ? shape : 'area'}`)
        // Set through the CSSOM, so a stored value that is not a colour is simply dropped
        .css('background-color', colour || ''),
      $('<span class="wms-legend-label"></span>').text(label)
    )))
    this.titleCutOffEntries()
  },

  // Only entries the legend's width cuts short get their full label as a tooltip
  titleCutOffEntries () {
    this.$('.wms-legend-entry').each((i, entry) => {
      const label = $(entry).find('.wms-legend-label')[0]
      if (label.scrollWidth > label.clientWidth) {
        entry.title = label.textContent
      } else {
        entry.removeAttribute('title')
      }
    })
  },

  // The CRS follows the view and the layer's capabilities, so it is chosen afresh on every update
  updateGetMap () {
    const bounds = this.map.getBounds()
    const size = this.map.getSize()
    const view = { south: bounds.getSouth(), west: bounds.getWest(), north: bounds.getNorth(), east: bounds.getEast() }
    const crs = chooseCrs(this.selected.layer.crs, view)
    this.$('.wms-getmap-label').text(crs ? `GetMap for this view (${crsLabel(crs)})` : 'GetMap for this view')
    this.$('.wms-getmap-url').val(getMapUrl({
      baseUrl: this.selected.service.publicUrl,
      layer: this.selected.layer.name,
      crs,
      ...view,
      width: size.x,
      height: size.y
    }))
  },

  // On failure the text stays selected (copyText leaves it so) for copying by hand
  copy (event) {
    const button = event.currentTarget
    const input = $(button).siblings('input')[0]
    return copyText(input).then(copied => {
      if (!copied) return
      $(button).text('Copied')
      clearTimeout(this.copiedTimers.get(button))
      this.copiedTimers.set(button, setTimeout(() => {
        $(button).text('Copy')
        this.copiedTimers.delete(button)
      }, COPIED_FOR_MS))
    })
  },

  remove () {
    this.copiedTimers.forEach(timer => clearTimeout(timer))
    this.copiedTimers.clear()
    this.map?.remove()
    return Backbone.View.prototype.remove.call(this)
  }
})
