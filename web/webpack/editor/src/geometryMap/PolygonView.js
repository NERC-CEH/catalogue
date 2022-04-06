import _ from 'underscore'
import { ObjectInputView } from '../views'
import 'leaflet/dist/leaflet.css'
import L from 'leaflet'
import 'leaflet-draw'
import template from './Polygon.tpl'
import iconRetinaUrl from 'leaflet/dist/images/marker-icon-2x.png'
import iconUrl from 'leaflet/dist/images/marker-icon.png'
import shadowUrl from 'leaflet/dist/images/marker-shadow.png'

export default ObjectInputView.extend({

  events: {
    'click #update': 'viewMap'
  },

  initialize () {
    L.Icon.Default.imagePath = 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.4.0/images'
    L.Marker.prototype.options.icon = L.icon({
      iconRetinaUrl,
      iconUrl,
      shadowUrl,
      iconSize: [25, 41],
      iconAnchor: [12, 41],
      popupAnchor: [1, -34],
      tooltipAnchor: [16, -28],
      shadowSize: [41, 41]
    })
    this.template = _.template(template)
    this.render()
    this.listenTo(this.model.collection, 'visible', this.viewMap)
  },

  createMap () {
    this.map = new L.Map(this.$('.map')[0], { center: new L.LatLng(51.513, -0.09), zoom: 4 })
    this.drawnItems = L.featureGroup()
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
        // Do marker specific actions
      }

      // Do whatever else you need to. (save to db; add to map etc)
      this.model.setPolygon(layer.toGeoJSON())
      this.map.addLayer(layer)
    })

    this.listenTo(this.map, L.Draw.Event.DELETED, function () {
      this.model.clearPolygon()
    })

    this.listenTo(this.map, L.Draw.Event.EDITED, function (event) {
      const layer = event.layer
      this.model.setPolygon(layer.toGeoJSON())
    })
  },

  createToolbar () {
    return new L.Control.Draw({
      position: 'topleft',
      edit: {
        featureGroup: this.drawnItems,
        edit: true,
        remove: true
      },
      draw: {
        rectangle: false,
        polygon: true,
        polyline: false,
        marker: true,
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
    this.createMap()
    return this
  }
})
