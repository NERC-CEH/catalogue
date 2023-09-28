import { ObjectInputView } from '../views'
import L from 'leaflet'
import 'leaflet-draw'
import template from './geometryTemplate'
export default ObjectInputView.extend({

  events: {
    'click #update': 'viewMap',
    'click button.showhide': 'showHide',
    'change #box': 'handleInput'
  },

  initialize () {
    // TODO: check if this is still needed
    // L.Icon.Default.imagePath = 'https://unpkg.com/leaflet-draw@1.0.2/dist/images/' // fix for leaflet draw image bug
    this.template = template
    ObjectInputView.prototype.initialize.apply(this)
    this.render()
    this.listenTo(this.model, 'change:geometryString', function (model, value) {
      this.$('#geometryString').val(value)
      this.render()
      this.viewMap()
    })

    this.listenTo(this.model, 'showhide', function () { this.showHide() })
  },

  handleInput () {
    this.model.set('geometryString', this.$('#box').val())
    this.viewMap()
  },

  createMap () {
    this.map = new L.Map(this.$('.map')[0], { center: new L.LatLng(51.513, -0.09), zoom: 4 })

    this.drawnItems = L.featureGroup()
    if (this.model.get('geometryString')) {
      const parsedJson = JSON.parse(this.model.get('geometryString'))
      this.drawButtons = false

      this.geometry = L.geoJson(parsedJson)
      this.drawnItems.addLayer(this.geometry)
      this.shapeDrawn = true
    } else {
      this.drawButtons = true
      this.shapeDrawn = false
    }
    if (this.shapeDrawn === true) {
      this.map.setView(this.geometry.getBounds().getCenter(), 4)
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

    L.control.layers(baseMaps, { drawlayer: this.drawnItems }, { position: 'topright', collapsed: false }).addTo(this.map)

    this.map.addControl(this.drawControl)
    baseMaps.Map.addTo(this.map)

    this.listenTo(this.map, L.Draw.Event.CREATED, function (event) {
      const layer = event.layer
      this.drawButtons = false
      this.model.set('geometryString', JSON.stringify(layer.toGeoJSON()))
      this.$('#geometryString').val(JSON.stringify(layer.toGeoJSON()))
      this.map.removeControl(this.drawControl)
      this.drawControl = this.createToolbar()
      this.map.addControl(this.drawControl)

      this.drawnItems.addLayer(layer)
    })

    this.listenTo(this.map, L.Draw.Event.DELETED, function () {
      this.model.set('geometryString', null)
      this.$('#geometryString').val(null)
      this.drawButtons = true
      this.map.removeControl(this.drawControl)
      this.drawControl = this.createToolbar()
      this.map.addControl(this.drawControl)
    })

    this.listenTo(this.map, L.Draw.Event.EDITMOVE, function (event) {
      const layer = event.layer
      this.model.set('geometryString', JSON.stringify(layer.toGeoJSON()))
      this.$('#geometryString').val(JSON.stringify(layer.toGeoJSON()))
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

  showHide () {
    this.$('.extended').toggleClass('hidden')
    this.$('#box').toggleClass('hidden')
    this.$('.showhide span').toggleClass('fa-chevron-down')
    this.$('.showhide span').toggleClass('fa-chevron-up')
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
