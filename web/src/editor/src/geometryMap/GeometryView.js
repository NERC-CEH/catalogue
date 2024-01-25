import { ObjectInputView } from '../views'
import L from 'leaflet'
import 'leaflet-draw'
import template from './geometryTemplate'
export default ObjectInputView.extend({

  events: {
    'change #box': 'handleInput'
  },

  initialize () {
    this.template = template
    ObjectInputView.prototype.initialize.apply(this)
    this.render()
    this.viewMap()
    this.listenTo(this.model, 'change:geometry', function (model, value) {
      this.$('#box').val(value)
    })
  },

  getGeometry () {
    const parsedJson = JSON.parse(this.model.get('geometry'))
    return L.geoJson(parsedJson)
  },

  handleInput () {
    this.model.setGeometry(this.$('#box').val())

    // Redraw shape
    this.drawnItems.clearLayers()
    this.drawnItems.addLayer(this.getGeometry())
  },

  createMap () {
    this.map = new L.Map(this.$('.map')[0], { center: new L.LatLng(51.513, -0.09), zoom: 4 })

    this.drawnItems = L.featureGroup()
    if (this.model.get('geometry')) {
      this.geometry = this.getGeometry()
      this.drawButtons = false
      this.drawnItems.addLayer(this.geometry)

      //Zoom to polygon if one was provided
      if(this.model.has("geometry")){
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

    L.control.layers(baseMaps, { drawlayer: this.drawnItems }, { position: 'topright', collapsed: false }).addTo(this.map)

    this.map.addControl(this.drawControl)
    baseMaps.Map.addTo(this.map)

    this.listenTo(this.map, L.Draw.Event.CREATED, function (event) {
      const layer = event.layer
      this.drawButtons = false
      const geoJson = JSON.stringify(layer.toGeoJSON())
      this.model.setGeometry(geoJson)
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
