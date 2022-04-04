import _ from 'underscore'
import { ObjectInputView } from '../views'
import 'leaflet/dist/leaflet.css'
import 'leaflet-draw/dist/leaflet.draw-src.css'
import L from 'leaflet'
import 'leaflet-draw'
import template from './BoundingBox.tpl'

export default ObjectInputView.extend({

  events: {
    'click #update': 'viewMap'
  },

  initialize () {
    this.template = _.template(template)

    this.render()
    this.listenTo(this.model, 'change:westBoundLongitude', function (model, value) {
      return this.$('#boundingBoxWestBoundLongitude').val(value)
    })
    this.listenTo(this.model, 'change:southBoundLatitude', function (model, value) {
      return this.$('#boundingBoxSouthBoundLatitude').val(value)
    })
    this.listenTo(this.model, 'change:eastBoundLongitude', function (model, value) {
      return this.$('#boundingBoxEastBoundLongitude').val(value)
    })
    this.listenTo(this.model, 'change:northBoundLatitude', function (model, value) {
      return this.$('#boundingBoxNorthBoundLatitude').val(value)
    })
    this.listenTo(this.model.collection, 'visible', this.viewMap)
  },

  createMap () {
    this.map = new L.Map(this.$('.map')[0], { center: new L.LatLng(51.513, -0.09), zoom: 4 })
    this.drawnItems = L.featureGroup()
    if (this.model.boundsExist()) {
      this.shapeDrawn = true
      this.rectangle = L.rectangle(this.model.getBoundingBox())
      this.drawnItems.addLayer(this.rectangle)
      this.rectangle.editing.enable()
    } else {
      this.shapeDrawn = false
    }
    this.drawnItems.addTo(this.map)
    L.control.layers({
      osm: L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; <a href="https://openstreetmap.org/copyright">OpenStreetMap contributors</a>'
      }).addTo(this.map),
      google: L.tileLayer('http://www.google.cn/maps/vt?lyrs=s@189&gl=cn&x={x}&y={y}&z={z}', {
        attribution: 'google'
      })
    }, { drawlayer: this.drawnItems }, { position: 'topright', collapsed: false }).addTo(this.map)

    this.map.addControl(new L.Control.Draw({
      position: 'topleft',
      edit: {
        featureGroup: this.drawnItems,
        edit: false
      },
      draw: {
        rectangle: true,
        polygon: false,
        polyline: false,
        marker: false,
        circle: false,
        circlemarker: false
      }
    }))

    this.listenTo(this.map, L.Draw.Event.CREATED, function (event) {
      if (this.shapeDrawn !== true) {
        this.rectangle = event.layer
        this.rectangle.editing.enable()
        this.drawnItems.addLayer(this.rectangle)
        this.shapeDrawn = true
        this.model.setBounds(event.layer.getBounds())
      }
    })

    this.listenTo(this.map, L.Draw.Event.DELETED, function () {
      this.shapeDrawn = false
      this.model.clearBounds()
    })

    this.listenTo(this.map, L.Draw.Event.EDITMOVE, function (event) {
      const layer = event.layer
      this.model.setBounds(layer.getBounds())
    })

    this.listenTo(this.map, L.Draw.Event.EDITRESIZE, function (event) {
      const layer = event.layer
      this.model.setBounds(layer.getBounds())
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
    this.createMap()
    return this
  }
})
