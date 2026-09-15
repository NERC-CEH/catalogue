import $ from 'jquery'
import { StudyAreaView } from '../src/View'

describe('Test StudyAreaView', function () {
  let el

  afterEach(() => el.remove())

  it('renders point', () => {
    // Given
    const templatePoint = '<div id="studyarea-map" class="studyarea-map"><span content="{&quot;type&quot;:&quot;Feature&quot;,&quot;properties&quot;:{},&quot;geometry&quot;:{&quot;type&quot;:&quot;Point&quot;,&quot;coordinates&quot;:[-2.64501,54.5261]}}" dataType="geoJson"/></div>'

    // When
    el = $(templatePoint).appendTo($('body'))
    const view = new StudyAreaView({
      el
    })

    // Then
    expect(view).toBeDefined()
    expect(el.hasClass('leaflet-container')).toBeTrue()
  })

  it('renders polygon', () => {
    // Given
    const templatePolygon = '<div id="studyarea-map" class="studyarea-map"><span content="{&quot;type&quot;:&quot;Feature&quot;,&quot;properties&quot;:{},&quot;geometry&quot;:{&quot;type&quot;:&quot;Polygon&quot;,&quot;coordinates&quot;:[[[-2.64866,54.52705],[-2.64363,54.52698],[-2.64406,54.52419],[-2.64872,54.52449],[-2.64866,54.52705]]]}}" dataType="geoJson"/></div>'

    // When
    el = $(templatePolygon).appendTo($('body'))
    const view = new StudyAreaView({
      el
    })

    // Then
    expect(view).toBeDefined()
    expect(el.hasClass('leaflet-container')).toBeTrue()
  })

  it('renders feature collection', () => {
    // Given
    const templateFeatureCollection = '<div id="studyarea-map" class="studyarea-map"><span content="{&quot;type&quot;:&quot;FeatureCollection&quot;,&quot;features&quot;:[{&quot;type&quot;:&quot;Feature&quot;,&quot;properties&quot;:{},&quot;geometry&quot;:{&quot;type&quot;:&quot;Point&quot;,&quot;coordinates&quot;:[-2.64406,54.52601]}},{&quot;type&quot;:&quot;Feature&quot;,&quot;properties&quot;:{},&quot;geometry&quot;:{&quot;type&quot;:&quot;Point&quot;,&quot;coordinates&quot;:[-2.64501,54.5261]}}]}" dataType="geoJson"/></div>'

    // When
    el = $(templateFeatureCollection).appendTo($('body'))
    const view = new StudyAreaView({
      el
    })

    // Then
    expect(view).toBeDefined()
    expect(el.hasClass('leaflet-container')).toBeTrue()
  })

  // Styling helpers are exercised directly: constructing the view runs the full
  // Leaflet render, which is not needed to check what style a feature resolves to.
  describe('marker styling', () => {
    const styling = Object.create(StudyAreaView.prototype)

    it('gives an active point a heavier outline than an inactive one', () => {
      // When
      const active = styling.getPointStyle({ properties: { availability: 'Active' } })
      const inactive = styling.getPointStyle({ properties: { availability: 'Inactive' } })

      // Then
      // `null` does not fall back to Leaflet's default of 3 - setOptions copies it
      // over the prototype default and the SVG renderer emits stroke-width="null",
      // which the browser discards, leaving 1px on the *active* marker.
      expect(typeof active.weight).toBe('number')
      expect(active.weight).toBeGreaterThan(inactive.weight)
    })

    it('tolerates a feature with no properties', () => {
      // Then
      expect(() => styling.getPointStyle({})).not.toThrow()
    })

    it('carries the source properties onto a polygon centroid', () => {
      // Given
      const polygon = {
        getBounds: () => ({ getCenter: () => ({ lat: 54.5, lng: -2.6 }) }),
        feature: { properties: { availability: 'Inactive', title: 'Test site' } }
      }

      // When
      const centroid = styling.centerPointOfPolygon(polygon)

      // Then
      // Without these, an inactive polygon's centroid renders as active blue at the
      // zoom levels where the centroid is all that is shown.
      expect(centroid.properties).toEqual({ availability: 'Inactive', title: 'Test site' })
    })

    it('tolerates a polygon layer with no feature', () => {
      // Given
      const polygon = { getBounds: () => ({ getCenter: () => ({ lat: 54.5, lng: -2.6 }) }) }

      // Then
      expect(() => styling.centerPointOfPolygon(polygon)).not.toThrow()
    })
  })
})
