import _ from 'underscore'
import $ from 'jquery'
import Backbone from 'backbone'
import L from 'leaflet'

export default Backbone.View.extend({

  initialize () {
    this.render()
  },

  createMap: function () {
    const studyArea = JSON.parse(this.getStudyArea()[0])
    const feature = L.geoJson(studyArea)
    const map = new L.Map($('#studyarea-map')[0], { center: feature.getBounds().getCenter() })
    const baseMaps = {
      Map: L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 18,
        attribution: '&copy; <a href="https://openstreetmap.org/copyright">OpenStreetMap contributors</a>'
      }).addTo(map),
      Satellite: L.tileLayer('https://www.google.cn/maps/vt?lyrs=s@189&gl=cn&x={x}&y={y}&z={z}', {
        attribution: 'google'
      })
    }
    L.control.layers(baseMaps, {}, { position: 'topright', collapsed: false }).addTo(map)
    map.fitBounds(feature.getBounds())
    feature.addTo(map)

    // Zoom out when feature is a point
    if (studyArea.geometry.type.toLowerCase() === 'point') {
      map.setZoom(9)
    }

    // Add zoom dependent point display for polygons
    this.polygonDisplay(feature, studyArea, map)
  },

  getStudyArea () {
    const studyArea = this.$('[dataType="geoJson"]')
    return _.map(studyArea, el => $(el).attr('content'))
  },

  // If this is a polygon, then set a zoom threshold whereby, zooming-out will show
  // the polygon's centroid as a point marker, and zooming-in will show the actual
  // polygon.  This zoom threshold is '3 map zoom-outs' beyond the feature's bounding
  // box zoom level.
  polygonDisplay (feature, studyArea, map) {
    const isPolygon = studyArea.geometry.type.toLowerCase() === 'polygon'
    if (isPolygon) {
      let zoomThreshold = map.getBoundsZoom(feature.getBounds()) - 3
      zoomThreshold = zoomThreshold < 0 ? 0 : zoomThreshold
      const centroidCoords = L.marker(feature.getBounds().getCenter())
      const centroid = L.geoJson({
        type: 'Feature',
        geometry: {
          type: 'Point',
          coordinates: [centroidCoords._latlng.lng, centroidCoords._latlng.lat]
        }
      })
      map.on('zoomend', function () {
        if (map.getZoom() < zoomThreshold) {
          if (map.hasLayer(feature)) feature.remove()
          if (!map.hasLayer(centroid)) centroid.addTo(map)
        } else {
          if (!map.hasLayer(feature)) feature.addTo(map)
          if (map.hasLayer(centroid)) centroid.remove()
        }
      })
    }
  },

  render () {
    this.createMap()
    return this
  }
})
