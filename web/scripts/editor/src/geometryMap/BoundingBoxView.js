import _ from 'underscore'
import { ObjectInputView } from '../views'
import 'leaflet-draw/dist/leaflet.draw-src.css'
import 'leaflet/dist/leaflet.css'
import L from 'leaflet'
import 'leaflet-draw'
import template from './BoundingBox.tpl'

export default ObjectInputView.extend({

  events: function () {
    return _.extend({}, ObjectInputView.prototype.events, {
      'click #update' () { return this.viewMap() }
    })
  },

  initialize () {
    this.template = _.template(template)
    ObjectInputView.prototype.initialize.apply(this)
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
    const baseMaps = {
      Map: L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 18,
        attribution: '&copy; <a href="https://openstreetmap.org/copyright">OpenStreetMap contributors</a>'
      }),
      Satellite: L.tileLayer('http://www.google.cn/maps/vt?lyrs=s@189&gl=cn&x={x}&y={y}&z={z}', {
        attribution: 'google'
      })
    }

    this.map = new L.Map(this.$('.map')[0], { center: new L.LatLng(51.513, -0.09), zoom: 4 })
    this.map.addLayer(baseMaps.Map)

    this.drawnItems = L.featureGroup()
    if (this.model.get('northBoundLatitude') && this.model.get('westBoundLongitude') &&
        this.model.get('southBoundLatitude') && this.model.get('eastBoundLongitude')) {
      this.shapeDrawn = true
      this.boundingBox = L.rectangle([[[this.model.get('northBoundLatitude'), this.model.get('westBoundLongitude')],
        [this.model.get('southBoundLatitude'), this.model.get('eastBoundLongitude')]]])
      this.drawnItems.addLayer(this.boundingBox)
      this.boundingBox.editing.enable()
      this.drawControl = this.deleteToolbar()
    } else {
      this.drawControl = this.createToolbar()
      this.shapeDrawn = false
    }
    if (this.shapeDrawn === true) {
      this.map.fitBounds(this.boundingBox.getBounds())
    }
    this.drawnItems.addTo(this.map)

    L.control.layers(baseMaps, { Drawlayer: this.drawnItems }, { position: 'topright', collapsed: false }).addTo(this.map)

    this.map.addControl(this.drawControl)
    baseMaps.Map.addTo(this.map)

    this.listenTo(this.map, L.Draw.Event.CREATED, function (event) {
      if (this.shapeDrawn === false) {
        this.boundingBox = event.layer
        this.boundingBox.editing.enable()
        this.drawnItems.addLayer(this.boundingBox)
        this.shapeDrawn = true
        this.map.removeControl(this.drawControl)
        this.drawControl = this.deleteToolbar()
        this.map.addControl(this.drawControl)
        this.model.setBounds(event.layer.getBounds())
      }
    })

    this.listenTo(this.map, L.Draw.Event.DELETED, function () {
      this.shapeDrawn = false
      this.map.removeControl(this.drawControl)
      this.drawControl = this.createToolbar()
      this.map.addControl(this.drawControl)
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

  createToolbar () {
    return new L.Control.Draw({
      position: 'topleft',
      edit: {
        featureGroup: this.drawnItems,
        edit: false,
        remove: false
      },
      draw: {
        rectangle: true,
        polygon: false,
        polyline: false,
        marker: false,
        circle: false,
        circlemarker: false
      }
    })
  },

  deleteToolbar () {
    return new L.Control.Draw({
      position: 'topleft',
      edit: {
        featureGroup: this.drawnItems,
        edit: false,
        remove: true
      },
      draw: {
        rectangle: false,
        polygon: false,
        polyline: false,
        marker: false,
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
    if (this.shapeDrawn === true) {
      this.viewMap()
    }
    return this
  }
})
