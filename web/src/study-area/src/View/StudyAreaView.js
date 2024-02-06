import _ from 'underscore'
import $ from 'jquery'
import Backbone from 'backbone'
import L from 'leaflet'

export default Backbone.View.extend({

  initialize () {
    // TODO: check if this is still needed
    // L.Icon.Default.imagePath = 'https://unpkg.com/leaflet-draw@1.0.2/dist/images/' // fix for leaflet draw marker bug
    this.render()
  },

  createMap () {
    const studyArea = JSON.parse(this.getStudyArea()[0])
    const boundingBox = L.geoJson(studyArea)
    const map = new L.Map($('#studyarea-map')[0], { center: boundingBox.getBounds().getCenter() })
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
    boundingBox.addTo(map)
    map.fitBounds(boundingBox.getBounds())
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
