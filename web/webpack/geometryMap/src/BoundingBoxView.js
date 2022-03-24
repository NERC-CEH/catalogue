import _ from 'underscore'
import { ObjectInputView } from '../../editor/src/views'
import 'leaflet/dist/leaflet.css'
import 'leaflet-draw/dist/images/spritesheet.png'
import 'leaflet-draw/dist/leaflet.draw.css'
import L from 'leaflet'
import 'leaflet-draw'
import template from './BoundingBox.tpl'
import $ from 'jquery'

let map
let boundingBox
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
    boundingBox = L.rectangle([
      [this.model.attributes.southBoundLatitude, this.model.attributes.westBoundLongitude],
      [this.model.attributes.northBoundLatitude, this.model.attributes.eastBoundLongitude]
    ])
    map = L.map(this.$('.map')[0], { drawControl: true }).setView(boundingBox.getBounds().getCenter(), 4)
    L.tileLayer('https://api.mapbox.com/styles/v1/{id}/tiles/{z}/{x}/{y}?access_token={accessToken}', {
      attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors, Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
      maxZoom: 18,
      id: 'mapbox/streets-v11',
      tileSize: 512,
      zoomOffset: -1,
      accessToken: 'pk.eyJ1IjoidGhvc3RhY2VoIiwiYSI6ImNsMTJiNmtvdzAxaHEzZXF1Zjl2YTJ3dWQifQ.aEAYlPpuHr1a4mVWORRCJA'
    }).addTo(map)

    // FeatureGroup is to store editable layers
    const drawnItems = new L.FeatureGroup()
    map.addLayer(drawnItems)
    const drawControl = new L.Control.Draw({
      edit: {
        featureGroup: drawnItems
      }
    })
    map.addControl(drawnItems)
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
    console.log('handle transformed feature')
    boundingBox = L.rectangle([
      [this.model.attributes.southBoundLatitude, this.model.attributes.westBoundLongitude],
      [this.model.attributes.northBoundLatitude, this.model.attributes.eastBoundLongitude]
    ]).update(map)
  },

  render () {
    ObjectInputView.prototype.render.apply(this)
    this.createMap()
    return this
  }
})
