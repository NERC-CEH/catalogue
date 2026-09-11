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

    const map = new L.Map($('#studyarea-map')[0], {
      center: feature.getBounds().getCenter()
    })

    const baseMaps = {
      Map: L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 18,
        attribution: '&copy; <a href="https://openstreetmap.org/copyright">OpenStreetMap contributors</a>'
      }).addTo(map),
      Satellite: L.tileLayer('https://www.google.cn/maps/vt?lyrs=s@189&gl=cn&x={x}&y={y}&z={z}', {
        attribution: 'google'
      })
    }

    L.control.layers(baseMaps, {}, {
      position: 'topright',
      collapsed: false
    }).addTo(map)

    map.fitBounds(feature.getBounds())

    switch (studyArea.type.toLowerCase()) {
      case 'feature': {
        feature.addTo(map)

        const geometryType = studyArea.geometry.type.toLowerCase()

        if (geometryType === 'point') {
          this.pointDisplay(feature, studyArea, map)
        } else if (geometryType === 'polygon') {
          this.polygonDisplay(feature, studyArea, map)
        } else if (geometryType === 'multipolygon') {
          this.multiPolygonDisplay(feature, studyArea, map)
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

    const geoJsonStrings = _.map(studyArea, el => $(el).attr('content'))

    return _.map(geoJsonStrings, geoJsonStr => {
      const geoJson = JSON.parse(geoJsonStr)

      return geoJsonStr
    })
  },

  getPointStyle (feature) {
    const inactive = feature.properties?.availability === 'Inactive'

    return {
      radius: 10,
      color: '#000000',
      fillColor: inactive ? '#999999' : '#0000DD',
      weight: inactive ? 2 : null,
      opacity: 1,
      fillOpacity: inactive ? 0.75 : 1
    }
  },

  createCircleMarker (feature, latlng) {
    return L.circleMarker(latlng, this.getPointStyle(feature))
  },

  pointDisplay (feature, studyArea, map) {
    map.setZoom(9)
  },

  addFeatureToMap (featureLayer, features, studyArea, map, style, enableZoomThreshold = false) {
    const numberOfLayers = Object.keys(featureLayer._layers).length

    const layer = L.geoJson(features, {
      style,

      pointToLayer: (feature, latlng) => {
        return this.createCircleMarker(feature, latlng)
      },

      onEachFeature: (feature, layer) => {
        const title = feature.properties.title
        const availability = feature.properties.availability

        let content = `<p>${title}</p>`
        if (typeof feature.properties.link !== 'undefined') {
          const link = feature.properties.link
          content = `<p><a href=${link}>${title}</a></p>`
        }

        if (availability === 'Inactive') {
          content = content + '<p class="text-body-tertiary">(INACTIVE)</p>'
        }

        layer.bindPopup(content, { offset: [0, -5] })

        const geomType = feature.geometry.type.toLowerCase()

        if (enableZoomThreshold && ['polygon', 'multipolygon'].includes(geomType)) {
          const centroidLayer = L.geoJSON(this.centerPointOfPolygon(layer), {
            pointToLayer: (feature, latlng) =>
              this.createCircleMarker(feature, latlng)
          })

          centroidLayer.bindPopup(content)

          let zoomThreshold = map.getBoundsZoom(layer.getBounds()) - 3
          zoomThreshold = zoomThreshold < 0 ? 0 : zoomThreshold

          const updateHandler = () => {
            if (map.getZoom() < zoomThreshold) {
              if (map.hasLayer(layer)) map.removeLayer(layer)
              if (!map.hasLayer(centroidLayer)) map.addLayer(centroidLayer)
            } else {
              if (!map.hasLayer(layer)) map.addLayer(layer)
              if (map.hasLayer(centroidLayer)) map.removeLayer(centroidLayer)
            }
          }

          updateHandler()
          map.on('zoomend', updateHandler)
        }
      }
    })

    const markers = L.markerClusterGroup()

    markers.addLayer(layer)
    map.addLayer(markers)

    if (numberOfLayers === 1) {
      this.pointDisplay(featureLayer, studyArea, map)
    }
  },

  polygonDisplay (feature, studyArea, map) {
    let zoomThreshold = map.getBoundsZoom(feature.getBounds()) - 3
    zoomThreshold = zoomThreshold < 0 ? 0 : zoomThreshold

    const centroid = L.geoJson(this.centerPointOfPolygon(feature), {
      pointToLayer: (feature, latlng) =>
        this.createCircleMarker(feature, latlng)
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
  },

  multiPolygonDisplay (feature, studyArea, map) {
    map.fitBounds(feature.getBounds())

    let zoomThreshold = map.getBoundsZoom(feature.getBounds()) - 3
    zoomThreshold = zoomThreshold < 0 ? 0 : zoomThreshold

    const centroid = L.geoJson(this.centerPointOfPolygon(feature), {
      pointToLayer: (feature, latlng) =>
        this.createCircleMarker(feature, latlng)
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

    feature.addTo(map)
  },

  featureCollectionDisplay (feature, studyArea, map) {
    const parentFeatures = []
    const childFeatures = []

    studyArea.features.forEach(feature => {
      if (
        (feature.geometry.type === 'Polygon' ||
          feature.geometry.type === 'MultiPolygon') &&
        feature.properties.showPolygon
      ) {
        parentFeatures.push(feature)
      } else {
        childFeatures.push(feature)
      }
    })

    if (childFeatures.length > 0) {
      this.addFeatureToMap(
        feature,
        parentFeatures,
        studyArea,
        map,
        { interactive: false },
        false
      )

      this.addFeatureToMap(
        feature,
        childFeatures,
        studyArea,
        map,
        {
          interactive: true,
          color: '#89A1FA'
        },
        true
      )
    } else {
      this.addFeatureToMap(
        feature,
        parentFeatures,
        studyArea,
        map,
        { interactive: false },
        true
      )
    }
  },

  centerPointOfPolygon (polygon) {
    const centroidCoords = L.marker(polygon.getBounds().getCenter())

    return {
      type: 'Feature',
      geometry: {
        type: 'Point',
        coordinates: [
          centroidCoords._latlng.lng,
          centroidCoords._latlng.lat
        ]
      }
    }
  },

  render () {
    this.createMap()
    return this
  }
})
