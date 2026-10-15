import { wktToGeoJson } from '../src/wkt'

// A search result's locations, as the catalogue's geometry classes write them for Solr
describe('Reading a result\'s location', () => {
  it('reads a bounding box, as BoundingBox writes it', () => {
    expect(wktToGeoJson('POLYGON((-8.648 49.864, -8.648 60.861, 1.768 60.861, 1.768 49.864, -8.648 49.864))')).toEqual({
      type: 'Polygon',
      coordinates: [[[-8.648, 49.864], [-8.648, 60.861], [1.768, 60.861], [1.768, 49.864], [-8.648, 49.864]]]
    })
  })

  it('reads a point', () => {
    expect(wktToGeoJson('POINT(-1.2 51.6)')).toEqual({ type: 'Point', coordinates: [-1.2, 51.6] })
  })

  it('reads a polygon with a hole', () => {
    expect(wktToGeoJson('POLYGON((0 0, 4 0, 4 4, 0 0), (1 1, 2 1, 2 2, 1 1))').coordinates.length).toBe(2)
  })

  it('reads a multipolygon', () => {
    expect(wktToGeoJson('MULTIPOLYGON(((0 0, 1 0, 1 1, 0 0)), ((5 5, 6 5, 6 6, 5 5)))')).toEqual({
      type: 'MultiPolygon',
      coordinates: [[[[0, 0], [1, 0], [1, 1], [0, 0]]], [[[5, 5], [6, 5], [6, 6], [5, 5]]]]
    })
  })

  it('reads numbers as Java writes small doubles', () => {
    expect(wktToGeoJson('POINT(1.0E-4 -2.5E-5)').coordinates).toEqual([0.0001, -0.000025])
  })

  it('reads nothing from a blank, another type, or something broken', () => {
    expect(wktToGeoJson('')).toBeNull()
    expect(wktToGeoJson(undefined)).toBeNull()
    expect(wktToGeoJson('LINESTRING(0 0, 1 1)')).toBeNull()
    expect(wktToGeoJson('POLYGON((0 0, 1 0')).toBeNull()
  })
})
