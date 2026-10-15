import Backbone from 'backbone'

const GEOJSON_TYPES = new Set([
  'Point', 'MultiPoint', 'LineString', 'MultiLineString', 'Polygon', 'MultiPolygon', 'GeometryCollection',
  'Feature', 'FeatureCollection'
])

export default Backbone.Model.extend({

  hasGeometry () {
    return this.has('geometryString')
  },

  getGeometry () {
    return this.get('geometryString')
  },

  setGeometry (geometryString) {
    this.set('geometryString', geometryString)
  },

  clearGeometry () {
    this.unset('geometryString')
  },

  // The GeoJSON can be typed in below the map: say when it can't be read, rather than fail to draw it
  validate (attrs) {
    const geometryString = attrs.geometryString
    if (geometryString == null || geometryString === '') return
    let geoJson
    try {
      geoJson = JSON.parse(geometryString)
    } catch (error) {
      return [{ message: `The geometry is not GeoJSON: ${error.message}` }]
    }
    if (!GEOJSON_TYPES.has(geoJson?.type)) {
      return [{ message: 'The geometry is not GeoJSON: it needs a type, such as Point or Polygon' }]
    }
  }
})
