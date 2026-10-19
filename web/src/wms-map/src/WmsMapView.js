import $ from 'jquery'
import Backbone from 'backbone'
import L from 'leaflet'
import '../../map/markerIcons'
import { copyText } from '../../clipboard/copyText'
import { getCapabilitiesUrl, getLegendUrl, getMapUrl, parseCapabilities } from './wmsRequests'

const GREAT_BRITAIN = [[49.8, -8.7], [60.9, 1.8]]
const COPIED_FOR_MS = 2000

// A service's named layers, kept to those the record names unless none of those is offered
const allowedLayers = (layers, allowedNames) => {
  const allowed = layers.filter(layer => allowedNames.includes(layer.name))
  return allowed.length ? allowed : layers
}

const unionOf = boundsList => boundsList.reduce(
  (union, bounds) => union ? union.extend(bounds) : L.latLngBounds(bounds), null)

/**
 * The Map section of a record page: the record's own WMS on a Leaflet map, a picker for its layers, the
 * layer's legend, and the GetCapabilities and GetMap addresses to use it in a GIS. The browser asks
 * only for same-origin /maps/{id}; the copyable addresses use the record's public host.
 * `ready` resolves once the capabilities have been fetched and the map (or its unavailable notice) shown.
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
        allowedNames: $service.find('.wms-layer-name').map((j, name) => $(name).text().trim()).get()
      }
    }).get()

    this.ready = Promise.all(this.services.map(service => this.fetchLayers(service)))
      .then(() => this.render())
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
      this.$('.wms-map-unavailable').removeClass('d-none')
      this.$('.wms-map, .wms-layers').addClass('d-none')
      return this
    }
    this.renderPicker()
    this.createMap()
    this.showLayer(this.choices[0])
    return this
  },

  renderPicker () {
    const $layers = this.$('.wms-layers').empty()
    const grouped = this.services.length > 1
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
    const $legend = $('<div class="wms-legend mt-2"><img alt="Legend"></div>').appendTo($layers)
    $legend.find('img').on('error', () => $legend.addClass('d-none'))
  },

  createMap () {
    this.map = new L.Map(this.$('.wms-map')[0])
    const baseMaps = {
      Map: L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 18,
        attribution: '&copy; <a href="https://openstreetmap.org/copyright">OpenStreetMap contributors</a>'
      }).addTo(this.map),
      Satellite: L.tileLayer('https://www.google.cn/maps/vt?lyrs=s@189&gl=cn&x={x}&y={y}&z={z}', {
        attribution: 'google'
      })
    }
    L.control.layers(baseMaps, {}, { position: 'topright', collapsed: false }).addTo(this.map)
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
      version: '1.3.0'
    }).addTo(this.map)
    this.$('.wms-legend').removeClass('d-none').find('img').attr('src', getLegendUrl(service.localPath, layer.name))
    this.$('.wms-layer-label').text(layer.name)
    this.$('.wms-capabilities-url').val(service.capabilitiesUrl)
    this.updateGetMap()
  },

  updateGetMap () {
    const bounds = this.map.getBounds()
    const size = this.map.getSize()
    this.$('.wms-getmap-url').val(getMapUrl({
      baseUrl: this.selected.service.publicUrl,
      layer: this.selected.layer.name,
      south: bounds.getSouth(),
      west: bounds.getWest(),
      north: bounds.getNorth(),
      east: bounds.getEast(),
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
