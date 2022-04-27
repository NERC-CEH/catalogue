import Backbone from 'backbone'
import _ from 'underscore'
import 'leaflet/dist/leaflet.css'
import L from 'leaflet'
import $ from 'jquery'
export default Backbone.View.extend({

  initialize () {
    this.render()
  },

  createMap () {
    this.overlay = L.featureGroup()
    const studyArea = JSON.parse(this.getStudyArea()[0])

    const feature = {
      type: 'Feature',
      properties: {},
      geometry: {
        type: 'Polygon',
        coordinates: studyArea
      }
    }
    console.log(feature)
    const polygon = L.geoJson(feature)
    const center = polygon.getBounds().getCenter()
    const map = new L.Map($('#studyarea-map')[0], { center: center, zoom: 4 })

    const baseMaps = {
      OSM: L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 18,
        attribution: '&copy; <a href="https://openstreetmap.org/copyright">OpenStreetMap contributors</a>'
      }).addTo(map),
      Google: L.tileLayer('http://www.google.cn/maps/vt?lyrs=s@189&gl=cn&x={x}&y={y}&z={z}', {
        attribution: 'google'
      })
    }

    L.control.layers(baseMaps, {}, { position: 'topright', collapsed: false }).addTo(map)
    polygon.addTo(map)
  },

  getStudyArea () {
    const studyArea = this.$('[dataType="geoJson"]')
    return _.map(studyArea, el => $(el).attr('content'))
  },

  render () {
    this.createMap()
    return this
  }
})
