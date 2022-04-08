import _ from 'underscore'
import { ObjectInputView } from '../views'
import 'leaflet/dist/leaflet.css'
import 'leaflet-draw/dist/leaflet.draw-src.css'
import L from 'leaflet'
import 'leaflet-draw'
import template from './Geometry.tpl'
import $ from 'jquery'
export default ObjectInputView.extend({

  events: {
    'click #update': 'viewMap'
  },

  initialize () {
    L.Icon.Default.imagePath = 'https://unpkg.com/leaflet-draw@1.0.2/dist/images/' // fix for leaflet draw image bug
    this.template = _.template(template)
    this.render()
  },

  createMap () {
    this.map = new L.Map(this.$('.map')[0], { center: new L.LatLng(51.513, -0.09), zoom: 4 })
    this.drawnItems = L.featureGroup()
    if (this.model.get('geometry')) {
      const parsedJson = JSON.parse(this.model.get('geometry'))
      this.drawButtons = false

      this.geometry = L.geoJson(parsedJson)
      this.drawnItems.addLayer(this.geometry)
    } else {
      this.drawButtons = true
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
      const layer = event.layer
      this.drawButtons = false
      this.model.set('geometry', JSON.stringify(layer.toGeoJSON()))
      this.map.removeControl(this.drawControl)
      this.drawControl = this.createToolbar()
      this.map.addControl(this.drawControl)

      this.drawnItems.addLayer(layer)
    })

    this.listenTo(this.map, L.Draw.Event.DELETED, function () {
      this.model.set('geometry', null)
      this.drawButtons = true
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
    if (this.drawButtons === true) {
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
    const that = this
    $(document).ready(function () {
      ObjectInputView.prototype.render.apply(that)
      that.createMap()
      return that
    })
  }
})
