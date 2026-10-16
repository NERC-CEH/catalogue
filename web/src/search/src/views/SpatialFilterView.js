import _ from 'underscore'
import L from 'leaflet'
import 'leaflet-draw'
import template from '../templates/spatialFilterTemplate'
import { wktToGeoJson } from '../wkt'
import Backbone from 'backbone'

// The current page's results on the map, and the one hovered or chosen in the list
const RESULT = { color: '#1c5b8a', weight: 1, fillOpacity: 0.08 }
const HIGHLIGHTED = { color: '#c2410c', weight: 3, fillOpacity: 0.25 }

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
    this.listenTo(this.model, 'results-sync', this.showResults)
    this.listenTo(this.model, 'cleared:results', this.clearResults)
    this.listenTo(this.model, 'results-change:selected results-change:hovered', this.highlightResults)

    // On a direct URL load, op is set on the model (via SearchAppView.setState) before this view
    // exists, so its change event fires too early for the listener above. A bbox likewise, which
    // is drawn once there is a map.
    this.updateOp(this.model.get('op'))
  },

  /*
   * The map is made the first time its tab is shown, not with the page: hidden, it was built for a
   * page most visitors never open, and Leaflet can't size a map it can't see. Afterwards it is fitted
   * to its tab each time it is shown, as the window may have changed meanwhile.
   */
  showMap () {
    if (this.map) {
      this.map.invalidateSize()
      return
    }
    this.createMap()
    this.updateBbox(this.model.get('bbox'))
    this.showResults()
  },

  createMap () {
    const baseMaps = {
      Map: L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 18,
        attribution: '&copy; <a href=\'https://openstreetmap.org/copyright\'>OpenStreetMap contributors</a>'
      }),
      Satellite: L.tileLayer('https://www.google.cn/maps/vt?lyrs=s@189&gl=cn&x={x}&y={y}&z={z}', {
        attribution: 'google'
      })
    }

    this.map = new L.Map(this.$('.map')[0], { center: new L.LatLng(55, -3), zoom: 5 })
    this.map.addLayer(baseMaps.Map)
    this.resultLayers = L.featureGroup().addTo(this.map)
    this.drawnItems = L.featureGroup()
    this.drawControl = this.createToolbar()
    this.drawnItems.addTo(this.map)
    L.control.layers(baseMaps, { Drawlayer: this.drawnItems, Results: this.resultLayers }, { position: 'topright', collapsed: false }).addTo(this.map)
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

    // The results are put aside while a box is drawn: they hid where it was being dragged, and a
    // press on one picked that record instead of starting the box. Only if they were showing, as the
    // layer control can turn them off.
    this.listenTo(this.map, L.Draw.Event.DRAWSTART, function () {
      this.resultsHiddenForDrawing = this.map.hasLayer(this.resultLayers)
      this.map.removeLayer(this.resultLayers)
    })

    this.listenTo(this.map, L.Draw.Event.DRAWSTOP, function () {
      if (this.resultsHiddenForDrawing) this.map.addLayer(this.resultLayers)
      this.resultsHiddenForDrawing = false
    })

    this.listenTo(this.map, L.Draw.Event.DELETED, function () {
      this.map.removeControl(this.drawControl)
      this.drawControl = this.createToolbar()
      this.map.addControl(this.drawControl)
      this.model.clearBbox()
    })
  },

  /*
   * Each result's locations on the current page, as a layer that names the record and, clicked,
   * picks it out in the list. Drawn behind the area being searched, which stays on top to be edited.
   */
  showResults () {
    if (!this.map) return
    this.resultLayers.clearLayers()
    const results = this.model.getResults()?.get('results') || []
    results.forEach(result => {
      const geometries = (result.locations || []).map(wktToGeoJson).filter(Boolean)
      if (!geometries.length) return
      const layer = L.geoJSON(geometries, {
        style: RESULT,
        // A point as a circle, which can be highlighted as a box is
        pointToLayer: (feature, latlng) => L.circleMarker(latlng, { ...RESULT, radius: 6 })
      })
      layer.identifier = result.identifier
      // Leaflet writes a tooltip's string as HTML, and titles come from depositors
      layer.bindTooltip(_.escape(result.title), { sticky: true })
      layer.on('click', () => this.model.getResults().set('selected', result.identifier))
      this.resultLayers.addLayer(layer)
    })
    this.drawnItems.bringToFront()
    this.highlightResults()
  },

  clearResults () {
    this.resultLayers?.clearLayers()
  },

  highlightResults () {
    if (!this.map) return
    const page = this.model.getResults()
    const picked = new Set([page?.get('selected'), page?.get('hovered')].filter(Boolean))
    this.resultLayers.eachLayer(layer => {
      const highlighted = picked.has(layer.identifier)
      layer.setStyle(highlighted ? HIGHLIGHTED : RESULT)
      if (highlighted) layer.bringToFront()
    })
  },

  updateOp (op) {
    this.$el.find('#opWithin').prop('checked', (op === 'iswithin'))
    this.$el.find('#opIntersects').prop('checked', (op === 'intersects'))
  },

  // Add the bounding box to the map if it has been added to the model and there isn't yet a bounding box on the map
  // The incomming bbox is a comma separated string of lat/lon of the form west-lon,east-lon,north-lat,south-lat
  updateBbox (bbox) {
    if (!this.map) return // drawn when the map is made
    if ((bbox !== undefined) && (this.drawnItems.getLayers().length === 0)) {
      const coords = bbox.split(',')
      const bounds = [[coords[3], coords[0]], [coords[2], coords[1]]]
      this.drawnItems.addLayer(L.rectangle(bounds))

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
        // Without the area its tooltip would show: Leaflet.draw 1.0.4 works it out by assigning an
        // undeclared variable, which throws in the strict mode of the ES module it is bundled into, so
        // the box stopped growing after the first move of the mouse
        rectangle: { showArea: false },
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
  setBbox (bounds) {
    const b = bounds.getBounds()
    this.model.setBbox([b.getWest(), b.getEast(), b.getNorth(), b.getSouth()].map(coord => coord.toFixed(3)).toString())
  },

  render () {
    this.$el.empty().append(this.template(this.model.attributes))
    return this
  }
})
