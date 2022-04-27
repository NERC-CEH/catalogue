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
    let center = new L.LatLng(51.513, -0.09)
    this.overlay = L.featureGroup()
    const studyArea = JSON.parse(this.getStudyArea()[0])

    const feature = { type: 'Feature', properties: {}, geometry: studyArea }
    console.log(feature)
    const rectangle = L.rectangle(studyArea)
    center = rectangle.getBounds().getCenter()
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
    rectangle.addTo(map)
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
