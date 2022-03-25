import _ from 'underscore'
import { ObjectInputView } from '../../editor/src/views'
import 'leaflet/dist/leaflet.css'
import 'leaflet-draw/dist/leaflet.draw-src.css'
import L from 'leaflet'
import 'leaflet-draw'
import template from './BoundingBox.tpl'
import $ from 'jquery'

let map
let shapeDrawn
export default ObjectInputView.extend({

  events () {
    return _.extend({}, ObjectInputView.prototype.events,
      { 'click button': 'viewMap' })
  },

  initialize (options) {
    this.template = _.template(template)

    _.bindAll(this,
      'handleDrawnFeature',
      'handleTransformedFeature'
    )
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
    map = L.map(this.$('.map')[0]).setView([51.513, -0.09], 4)

    const osmLayer = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; <a href="https://openstreetmap.org/copyright">OpenStreetMap contributors</a>'
    })

    const googleLayer = L.tileLayer('http://{s}.google.com/vt/lyrs=s&x={x}&y={y}&z={z}', {
      maxZoom: 19,
      subdomains: ['mt0', 'mt1', 'mt2', 'mt3'],
      attribution: '&copy; <a href="www.google.com">Google</a>'
    })

    const baseLayers = {
      OpenStreetMap: osmLayer,
      Google: googleLayer
    }

    L.control.layers(baseLayers).addTo(map)
    osmLayer.addTo(map)

    // FeatureGroup is to store editable layers
    const drawnItems = new L.FeatureGroup()
    map.addLayer(drawnItems)
    const drawControl = new L.Control.Draw({
      position: 'topright',
      draw: {
        polygon: true,
        circle: true,
        rectangle: true,
        marker: false,
        polyline: false,
        circlemarker: false
      },
      edit: {
        featureGroup: drawnItems,
        edit: true,
        delete: true
      }
    })
    map.addControl(drawControl)
    map.on('draw:created', function (e) {
      const type = e.layerType
      const layer = e.layer

      if (type === 'marker') {
        // Do marker specific actions
      }

      // Do whatever else you need to. (save to db, add to map etc)

      if (shapeDrawn !== true) {
        // layer.editing.enable()
        drawnItems.addLayer(layer)
        shapeDrawn = true
      }
    })
  },

  viewMap () {
    map.off()
    map.remove()
    this.createMap()
  },

  handleDrawnFeature (obj) {
    console.log('handle drawn feature')
  },

  handleTransformedFeature (obj) {

  },

  render () {
    ObjectInputView.prototype.render.apply(this)
    this.createMap()
    return this
  }
})
