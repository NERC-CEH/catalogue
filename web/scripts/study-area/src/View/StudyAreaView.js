import Backbone from 'backbone'
import _ from 'underscore'
import 'leaflet-draw/dist/leaflet.draw-src.css'
import 'leaflet/dist/leaflet.css'
import L from 'leaflet'
import $ from 'jquery'
export default Backbone.View.extend({

  initialize () {
    L.Icon.Default.imagePath = 'https://unpkg.com/leaflet-draw@1.0.2/dist/images/' // fix for leaflet draw marker bug
    this.render()
  },

  createMap () {
    this.overlay = L.featureGroup()

    const studyArea = JSON.parse(this.getStudyArea()[0])
    const polygon = L.geoJson(studyArea)
    const center = polygon.getBounds().getCenter()
    const map = new L.Map($('#studyarea-map')[0], { center: center, zoom: 4 })

    const baseMaps = {
      Map: L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 18,
        attribution: '&copy; <a href="https://openstreetmap.org/copyright">OpenStreetMap contributors</a>'
      }).addTo(map),
      Satellite: L.tileLayer('http://www.google.cn/maps/vt?lyrs=s@189&gl=cn&x={x}&y={y}&z={z}', {
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
