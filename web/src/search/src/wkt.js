/*
 * A search result's locations, as the catalogue indexes them in Solr: well-known text written by
 * BoundingBox, PointGeometry, PolygonGeometry and MultiPolygonGeometry (java …/geometry), so only
 * those three types. Returns the GeoJSON geometry, or null for anything else, including the blank
 * a server-rendered result without a location carries.
 */
const TYPES = { POINT: 'Point', POLYGON: 'Polygon', MULTIPOLYGON: 'MultiPolygon' }
// Java writes a double as 1.0E-4 when small enough
const NUMBER = String.raw`-?\d+(?:\.\d+)?(?:[eE][-+]?\d+)?`
const PAIR = new RegExp(`(${NUMBER})\\s+(${NUMBER})`, 'g')

export function wktToGeoJson (wkt) {
  const match = /^\s*([A-Z]+)\s*(\(.*\))\s*$/i.exec(wkt || '')
  const type = match && TYPES[match[1].toUpperCase()]
  if (!type) return null
  try {
    // Each "x y" a [x,y], and the brackets JSON's: POLYGON((0 0, 1 0, …)) is [[[0,0],[1,0],…]]
    const coordinates = JSON.parse(match[2].replace(PAIR, '[$1,$2]').replaceAll('(', '[').replaceAll(')', ']'))
    return { type, coordinates: type === 'Point' ? coordinates[0] : coordinates }
  } catch {
    return null
  }
}
