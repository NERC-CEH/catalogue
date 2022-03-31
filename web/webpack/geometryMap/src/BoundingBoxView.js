import _ from 'underscore'
import { ObjectInputView } from '../../editor/src/views'
import 'leaflet/dist/leaflet.css'
import 'leaflet-draw/dist/leaflet.draw-src.css'
import L from 'leaflet'
import 'leaflet-draw'
import template from './BoundingBox.tpl'

export default ObjectInputView.extend({

  // events () {
  //   _.extend({}, ObjectInputView.prototype.events, { 'click #update': 'viewMap' })
  // },

  events: {
    'click #update': 'viewMap'
  },

  initialize (options) {
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

    this.listenTo(this.map, L.Draw.Event.CREATED, function (event) {
      console.log('created')
      if (this.shapeDrawn !== true) {
        const layer = event.layer
        this.drawnItems.addLayer(layer)
        this.shapeDrawn = true
        this.setBounds(event.layer.getBounds())
      }
    })

    this.listenTo(this.map, 'draw:deleted', function (event) {
      console.log('deleted')
      this.shapeDrawn = false
      this.clearBounds()
    })

    this.listenTo(this.map, 'draw:editmove', function (event) {
      console.log('editmove')
      const layer = event.layer
      this.setBounds(layer.getBounds())
    })
  },

  createMap () {
    this.map = new L.Map(this.$('.map')[0], { center: new L.LatLng(51.513, -0.09), zoom: 4 })
    this.drawnItems = L.featureGroup()
    if (this.model.get('northBoundLatitude') !== null && this.model.get('westBoundLongitude') !== null &&
    this.model.get('southBoundLatitude') !== null && this.model.get('eastBoundLongitude') !== null) {
      console.log('shape drawn true')
      this.shapeDrawn = true
      this.addBoundingBox()
    } else {
      console.log('shape drawn false')
      this.drawnItems.addTo(this.map)
      this.shapeDrawn = false
    }
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
        featureGroup: this.drawnItems
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

    // this.map.on(L.Draw.Event.CREATED, function (event) {
    //   const layer = event.layer
    //   this.drawnItems.addLayer(layer)
    // })
  },

  addBoundingBox () {
    const bounds = [[this.model.get('northBoundLatitude'), this.model.get('westBoundLongitude')],
      [this.model.get('southBoundLatitude'), this.model.get('eastBoundLongitude')]]
    this.rectangle = L.rectangle(bounds)
    this.drawnItems.addLayer(this.rectangle)
    this.drawnItems.addTo(this.map)
  },

  setBounds (bounds) {
    console.log('setbounds')
    console.log(bounds)
    this.model.set('northBoundLatitude', bounds.getNorth().toFixed(3))
    this.model.set('eastBoundLongitude', bounds.getEast().toFixed(3))
    this.model.set('southBoundLatitude', bounds.getSouth().toFixed(3))
    this.model.set('westBoundLongitude', bounds.getWest().toFixed(3))
  },

  clearBounds () {
    this.model.set('northBoundLatitude', null)
    this.model.set('eastBoundLongitude', null)
    this.model.set('southBoundLatitude', null)
    this.model.set('westBoundLongitude', null)
  },

  viewMap () {
    console.log('viewmap')
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
