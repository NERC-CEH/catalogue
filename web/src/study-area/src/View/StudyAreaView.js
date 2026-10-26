import $ from 'jquery'
import _ from 'underscore'
import Backbone from 'backbone'
import L from 'leaflet'
import '../../../map/markerIcons'
import 'leaflet.markercluster/dist/leaflet.markercluster.js'

export default Backbone.View.extend({
  initialize () {
    this.render()
  },

  createMap: function () {
    const geoJsonString = this.$('[dataType="geoJson"]').first().attr('content')
    const studyArea = JSON.parse(geoJsonString)
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

  getPointStyle (feature, zoom) {
    const inactive = feature.properties?.availability === 'Inactive'
    const pending = feature.properties?.availability === 'Pending'
    const radius = zoom < 8
      ? 10
      : zoom < 11
        ? 12
        : 16

    const colour = inactive
      ? '#999999'
      : pending
        ? '#cc7a00'
        : '#0000DD'

    return {
      radius,
      color: colour,
      fillColor: colour,
      weight: 1,
      opacity: 1,
      fillOpacity: inactive ? 0.75 : 1
    }
  },

  createCircleMarker (feature, latlng, map) {
    return L.circleMarker(latlng, this.getPointStyle(feature, map.getZoom()))
  },

  pointDisplay (feature, studyArea, map) {
    map.setZoom(9)
  },

  getPolygonStyle (feature) {
    const inactive = feature.properties?.availability === 'Inactive'
    const pending = feature.properties?.availability === 'Pending'

    return {
      interactive: true,
      color: inactive ? '#444444' : pending ? '#885508' : '#0000DD',
      fillColor: inactive ? '#999999' : pending ? '#cc7a00' : '#0000DD',
      weight: 1,
      opacity: 1,
      dashArray: pending ? '10,5' : null,
      fillOpacity: 0.2
    }
  },

  addFeatureToMap (featureLayer, features, studyArea, map, style, enableZoomThreshold = false) {
    const numberOfLayers = Object.keys(featureLayer._layers).length

    const layer = L.geoJson(features, {
      style,

      pointToLayer: (feature, latlng) => {
        return this.createCircleMarker(feature, latlng, map)
      },

      onEachFeature: (feature, layer) => {
        const title = feature.properties.title
        const availability = feature.properties.availability

        // Leaflet sets popup content as HTML; the title comes from a linked record
        let content = `<div>${_.escape(title)}</div>`
        // Only http(s) links: escaping doesn't stop a javascript: URL from running when clicked
        const link = feature.properties.link
        if (typeof link === 'string' && /^https?:\/\//i.test(link)) {
          content = `<div><a href="${_.escape(link)}">${_.escape(title)}</a></div>`
        }

        if (availability === 'Inactive') {
          content = content + '<div class="text-body-tertiary">(INACTIVE)</div>'
        }
        if (availability === 'Pending') {
          content = content + '<div class="text-body-secondary">(PENDING)</div>'
        }

        layer.bindPopup(content, { offset: [0, -5] })

        const geomType = feature.geometry.type.toLowerCase()

        if (enableZoomThreshold && ['polygon', 'multipolygon'].includes(geomType)) {
          const centroidLayer = L.geoJSON(this.centerPointOfPolygon(layer), {
            pointToLayer: (feature, latlng) =>
              this.createCircleMarker(feature, latlng, map)
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
        this.createCircleMarker(feature, latlng, map)
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
        this.createCircleMarker(feature, latlng, map)
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
    const childPolygons = []
    const childPoints = []

    studyArea.features.forEach(feature => {
      const isPolygon =
        feature.geometry.type === 'Polygon' ||
        feature.geometry.type === 'MultiPolygon'

      if (isPolygon && feature.properties.showPolygon) {
        parentFeatures.push(feature)
      } else if (isPolygon) {
        childPolygons.push(feature)
      } else {
        childPoints.push(feature)
      }
    })

    if (childPoints.length > 0) {
      this.addFeatureToMap(
        feature,
        childPoints,
        studyArea,
        map,
        {},
        true
      )
    }

    if (childPolygons.length > 0) {
      this.addFeatureToMap(
        feature,
        childPolygons,
        studyArea,
        map,
        (feature) => this.getPolygonStyle(feature),
        true
      )
    }

    if (parentFeatures.length > 0) {
      this.addFeatureToMap(
        feature,
        parentFeatures,
        studyArea,
        map,
        (feature) => ({
          ...this.getPolygonStyle(feature),
          interactive: false
        }),
        true
      )
    }
  },

  centerPointOfPolygon (polygon) {
    const centroidCoords = L.marker(polygon.getBounds().getCenter())

    return {
      type: 'Feature',
      // Carried through so the centroid styles like the polygon it stands for;
      // without them an inactive facility renders as active at low zoom.
      properties: polygon.feature?.properties ?? {},
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
