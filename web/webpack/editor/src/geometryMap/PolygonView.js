import _ from 'underscore'
import { ObjectInputView } from '../views'
import 'leaflet/dist/leaflet.css'
import 'leaflet-draw/dist/leaflet.draw-src.css'
import L from 'leaflet'
import 'leaflet-draw'
import template from './Polygon.tpl'
import $ from 'jquery'
export default ObjectInputView.extend({

  events: {
    'click #update': 'viewMap'
  },

  initialize () {
    L.Icon.Default.imagePath = 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.4.0/images'
    this.template = _.template(template)
    this.render()
    // this.listenTo(this.model.collection, 'visible', this.viewMap)
  },

  createMap () {
    this.map = new L.Map(this.$('.map')[0], { center: new L.LatLng(51.513, -0.09), zoom: 4 })
    this.drawnItems = L.featureGroup()
    if (this.model.get('polygon')) {
      this.polygon = L.geoJson(JSON.parse(this.model.get('polygon')))
      this.drawnItems.addLayer(this.polygon)
      this.polygonButton = false
    } else {
      this.polygonButton = true
    }
    if (this.model.get('marker')) {
      const marker = JSON.parse(this.model.get('marker'))
      this.marker = L.marker([marker.lat, marker.lng])
      this.drawnItems.addLayer(this.marker)
      this.markerButton = false
    } else {
      this.markerButton = true
    }
    this.drawControl = this.createToolbar()
    this.drawnItems.addTo(this.map)
    L.control.layers({
      OSM: L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 18,
        attribution: '&copy; <a href="https://openstreetmap.org/copyright">OpenStreetMap contributors</a>'
      }).addTo(this.map),
      Google: L.tileLayer('http://www.google.cn/maps/vt?lyrs=s@189&gl=cn&x={x}&y={y}&z={z}', {
        attribution: 'google'
      })
    }, { drawlayer: this.drawnItems }, { position: 'topright', collapsed: false }).addTo(this.map)

    this.map.addControl(this.drawControl)

    this.listenTo(this.map, L.Draw.Event.CREATED, function (event) {
      const type = event.layerType
      const layer = event.layer
      if (type === 'marker') {
        this.model.set('marker', JSON.stringify(layer.getLatLng()))
        this.markerButton = false
      }
      if (type === 'polygon') {
        this.model.set('polygon', JSON.stringify(layer.toGeoJSON()))
        this.polygonButton = false
      }
      this.map.removeControl(this.drawControl)
      this.drawControl = this.createToolbar()
      this.map.addControl(this.drawControl)

      this.drawnItems.addLayer(layer)
    })

    this.listenTo(this.map, L.Draw.Event.DELETED, function (event) {
      const layers = event.layers
      const that = this
      layers.eachLayer(function (layer) {
        const type = that.getShapeType(layer)
        if (type === 'marker') {
          that.model.set('marker', null)
          that.markerButton = true
        }
        if (type === 'polygon') {
          that.model.set('polygon', null)
          that.polygonButton = true
        }
      })
      this.map.removeControl(this.drawControl)
      this.drawControl = this.createToolbar()
      this.map.addControl(this.drawControl)
    })
  },

  getShapeType (layer) {
    if (layer instanceof L.Marker) {
      return 'marker'
    }

    if ((layer instanceof L.Polygon) && !(layer instanceof L.Rectangle)) {
      return 'polygon'
    }
  },

  createToolbar () {
    if (this.polygonButton === true && this.markerButton === true) {
      this.deleteButton = false
    } else {
      this.deleteButton = true
    }
    return new L.Control.Draw({
      position: 'topleft',
      edit: {
        featureGroup: this.drawnItems,
        edit: false,
        remove: this.deleteButton
      },
      draw: {
        rectangle: false,
        polygon: this.polygonButton,
        polyline: false,
        marker: this.markerButton,
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
    const that = this
    $(document).ready(function () {
      ObjectInputView.prototype.render.apply(that)
      that.createMap()
      return that
    })
  }
})
