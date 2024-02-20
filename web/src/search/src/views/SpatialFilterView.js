import _ from 'underscore'
import L from 'leaflet'
import 'leaflet-draw'
import template from '../templates/spatialFilterTemplate'
import Backbone from "backbone";

export default Backbone.View.extend({

  events: {
    'click .spatialOp': 'updateSpatialOp'
  },

  initialize () {
    this.template = template
    this.render()

    // If 'bbox' or 'op' are in the hash arguments of the querystring (ie backbone router controlled), then the model will emit a change event for the changed arg, which must be updated on the UI
    this.listenTo(this.model, 'change:op', function (model, value) { this.updateOp(value) })
    this.listenTo(this.model, 'change:bbox', function (model, value) { this.updateBbox(value) })
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

    this.map = new L.Map(this.$('.map')[0], { center: new L.LatLng(54, 1), zoom: 4 })
    this.map.addLayer(baseMaps.Map)
    this.drawnItems = L.featureGroup()
    this.drawControl = this.createToolbar()
    this.drawnItems.addTo(this.map)
    L.control.layers(baseMaps, { Drawlayer: this.drawnItems }, { position: 'topright', collapsed: false }).addTo(this.map)
    this.map.addControl(this.drawControl)
    baseMaps.Map.addTo(this.map)

    this.listenTo(this.map, L.Draw.Event.CREATED, function (event) {
      this.boundingBox = event.layer
      this.drawnItems.addLayer(this.boundingBox)
      this.map.removeControl(this.drawControl)
      this.drawControl = this.deleteToolbar()
      this.map.addControl(this.drawControl)
      this.setBbox(this.boundingBox)
    })

    this.listenTo(this.map, L.Draw.Event.DELETED, function () {
      this.map.removeControl(this.drawControl)
      this.drawControl = this.createToolbar()
      this.map.addControl(this.drawControl)
      this.model.clearBbox()
    })
  },

  updateOp(op) {
    this.$el.find('#opWithin').prop('checked', (op === 'iswithin'))
    this.$el.find('#opIntersects').prop('checked', (op === 'intersects'))
  },

  // Add the bounding box to the map if it has been added to the model and there isn't yet a bounding box on the map
  // The incomming bbox is a comma separated string of lat/lon of the form west-lon,east-lon,north-lat,south-lat
  updateBbox(bbox){
    if((bbox !== undefined) && (this.drawnItems.getLayers().length === 0)){

      const coords = bbox.split(',')
      const bounds = [[coords[3],coords[0]],[coords[2],coords[1]]]
      this.drawnItems.addLayer(L.rectangle(bounds, {color: "#ff7800", weight: 1}))

      this.map.removeControl(this.drawControl)
      this.drawControl = this.deleteToolbar()
      this.map.addControl(this.drawControl)
    }
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

  updateSpatialOp (e) {
    this.model.set('op', e.target.dataset.op)
  },

  /**
   * The bounding boxes of a single metadata record are indexed in solr in the field 'locations'
   * Using this 'locations' field, the simplest spatial query to find the records within a search box is: 'locations:"Intersects(ENVELOPE(minX, maxX, maxY, minY)"
   * So the Bbox property of the model for doing the search, needs to be the String: minX,maxX,maxY,minY
   */
  setBbox(bounds){
    this.model.setBbox([bounds._bounds._southWest.lng,bounds._bounds._northEast.lng,bounds._bounds._northEast.lat,bounds._bounds._southWest.lat].toString())
  },

  viewMap () {
    if (this.map) {
      this.map.off()
      this.map.remove()
    }
    this.createMap()
  },

  render () {
    this.$el.empty().append(this.template(this.model.attributes))
    this.viewMap()
    return this
  }
})
