import L from 'leaflet'
import 'leaflet-draw'
import { ObjectInputView } from '../views'
import template from './geometryTemplate'

export default ObjectInputView.extend({

  events: {
    'change #box': 'handleInput',
    'change #locationConfidential': 'handleLocationConfidentialCheckbox'
  },

  initialize (options) {
    this.template = template
    ObjectInputView.prototype.initialize.apply(this, arguments)
    this.render()
    this.viewMap()
    this.listenTo(this.model, 'change:geometryString', function (model, value) {
      this.$('#box').val(value)
    })
  },

  getGeometry () {
    const parsedJson = JSON.parse(this.model.get('geometryString'))
    return L.geoJson(parsedJson)
  },

  handleInput () {
    this.model.setGeometry(this.$('#box').val())

    // Redraw shape
    this.drawnItems.clearLayers()
    this.drawnItems.addLayer(this.getGeometry())
  },

  // Records the editor's intent and nothing else. The geometry is reduced to a
  // grid cell server-side, on save, by LocationObfuscationService - doing it here
  // meant the raw-JSON field below the map, a direct API PUT, and every geometry
  // type except Point all saved the precise location.
  handleLocationConfidentialCheckbox () {
    const isChecked = this.$('#locationConfidential').is(':checked')

    if (isChecked) {
      const confirmed = window.confirm(
        'The saved location will be reduced to an approximate area of about ' +
        '11 km by 7 km, and the precise location will not be kept.\n\n' +
        'Do you want to continue?'
      )

      if (!confirmed) {
        this.$('#locationConfidential').prop('checked', false)
        return
      }
    }

    this.model.set('locationConfidential', isChecked)
  },

  createMap () {
    this.map = new L.Map(this.$('.map')[0], { center: new L.LatLng(51.513, -0.09), zoom: 4 })

    this.drawnItems = L.featureGroup()
    if (this.model.getGeometry()) {
      this.geometryString = this.getGeometry()
      this.drawButtons = false
      this.drawnItems.addLayer(this.geometryString)

      // Zoom to polygon if one was provided
      if (this.model.hasGeometry()) {
        if (this.model.getGeometry().toLowerCase().includes('polygon')) {
          this.map.fitBounds(this.drawnItems.getBounds())
        }
      }
    } else {
      this.drawButtons = true
    }
    this.drawControl = this.createToolbar()
    this.drawnItems.addTo(this.map)

    const baseMaps = {
      Map: L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 18,
        attribution: '&copy; <a href="https://openstreetmap.org/copyright">OpenStreetMap contributors</a>'
      }),
      Satellite: L.tileLayer('http://www.google.cn/maps/vt?lyrs=s@189&gl=cn&x={x}&y={y}&z={z}', {
        attribution: 'google'
      })
    }

    L.control.layers(baseMaps, { drawlayer: this.drawnItems }, {
      position: 'topright',
      collapsed: false
    }).addTo(this.map)

    this.map.addControl(this.drawControl)
    baseMaps.Map.addTo(this.map)

    // Five decimal places is roughly half a metre; four would be about six.
    const rounding = function (key, val) {
      if (typeof val === 'number') { return Number(val.toFixed(5)) }
      return val
    }

    this.listenTo(this.map, L.Draw.Event.CREATED, function (event) {
      const layer = event.layer
      const geoJson = JSON.stringify(layer.toGeoJSON(), rounding)
      this.model.setGeometry(geoJson)
      this.drawButtons = false
      this.map.removeControl(this.drawControl)
      this.drawControl = this.createToolbar()
      this.map.addControl(this.drawControl)
      this.drawnItems.addLayer(layer)
    })

    this.listenTo(this.map, L.Draw.Event.DELETED, function () {
      this.model.clearGeometry()
      this.drawButtons = true
      this.map.removeControl(this.drawControl)
      this.drawControl = this.createToolbar()
      this.map.addControl(this.drawControl)
    })
  },

  createToolbar () {
    this.deleteButton = this.drawButtons !== true

    return new L.Control.Draw({
      position: 'topleft',
      edit: {
        featureGroup: this.drawnItems,
        edit: false,
        remove: this.deleteButton
      },
      draw: {
        rectangle: false,
        polygon: this.drawButtons,
        polyline: false,
        marker: this.drawButtons,
        circle: false,
        circlemarker: false
      }
    })
  },

  viewMap () {
    if (this.map) {
      this.map.off()
      this.map.remove()
    }
    this.createMap()
  },

  render () {
    ObjectInputView.prototype.render.apply(this)
    return this
  }
})
