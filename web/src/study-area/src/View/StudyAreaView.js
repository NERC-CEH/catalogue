import _ from 'underscore'
import $ from 'jquery'
import Backbone from 'backbone'
import L from 'leaflet'
import 'leaflet.markercluster/dist/leaflet.markercluster.js'

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

    switch (studyArea.type.toLowerCase()) {
      case 'feature': {
        feature.addTo(map)
        const geometryType = studyArea.geometry.type.toLowerCase()
        if (geometryType === 'point') {
          this.pointDisplay(feature, studyArea, map)
        } else if (geometryType === 'polygon') {
          this.polygonDisplay(feature, studyArea, map)
        }
        break
      }
      case 'featurecollection':
        this.featureCollectionDisplay(feature, studyArea, map)
        break
      default:
        console.log('Unknown geoJSON type.')
    }
  },

  getStudyArea () {
    const studyArea = this.$('[dataType="geoJson"]')
    return _.map(studyArea, el => $(el).attr('content'))
  },

  pointDisplay (feature, studyArea, map) {
    map.setZoom(9)
  },

  // If this is a polygon, then set a zoom threshold whereby, zooming-out will show
  // the polygon's centroid as a point marker, and zooming-in will show the actual
  // polygon.  This zoom threshold is '3 map zoom-outs' beyond the feature's bounding
  // box zoom level.
  polygonDisplay (feature, studyArea, map) {
    let zoomThreshold = map.getBoundsZoom(feature.getBounds()) - 3
    zoomThreshold = zoomThreshold < 0 ? 0 : zoomThreshold
    const centroid = L.geoJson(this.centerPointOfPolygon(feature))

    map.on('zoomend', function () {
      if (map.getZoom() < zoomThreshold) {
        if (map.hasLayer(feature)) feature.remove()
        if (!map.hasLayer(centroid)) centroid.addTo(map)
      } else {
        if (!map.hasLayer(feature)) feature.addTo(map)
        if (map.hasLayer(centroid)) centroid.remove()
      }
    })
  },

  featureCollectionDisplay (feature, studyArea, map) {
    const numberOfLayers = Object.keys(feature._layers).length

    // Convert all features in feature collection to points in order to play
    // nicely with Leaflet.ClusterMap library
    const pointFeatureCollection = studyArea.features.map(feature => {
      if (feature.geometry.type === 'Polygon') {
        const polygon = L.geoJSON(feature.geometry)
        const centroid = this.centerPointOfPolygon(polygon)
        return {
          ...feature,
          geometry: centroid.geometry
        }
      } else {
        return feature
      }
    })
    const featureInPoints = L.geoJson(pointFeatureCollection, {
      onEachFeature: (feature, layer) => {
        const title = feature.properties.title
        const link = feature.properties.link
        const content = `<h5><a href=${link}>${title}</a></h5>`
        layer.bindPopup(content)
      }
    })

    const markers = L.markerClusterGroup()
    markers.addLayer(featureInPoints)
    map.addLayer(markers)
    if (numberOfLayers === 1) {
      this.pointDisplay(feature, studyArea, map)
    }
  },

  // TODO: Consider returning centroid of polygon within the polygon itself
  centerPointOfPolygon (polygon) {
    const centroidCoords = L.marker(polygon.getBounds().getCenter())
    return {
      type: 'Feature',
      geometry: {
        type: 'Point',
        coordinates: [centroidCoords._latlng.lng, centroidCoords._latlng.lat]
      }
    }
  },

  render () {
    this.createMap()
    return this
  }
})
