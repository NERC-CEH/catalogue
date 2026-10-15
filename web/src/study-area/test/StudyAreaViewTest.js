import { afterEach, describe, expect, it, vi } from 'vitest'
import $ from 'jquery'
import L from 'leaflet'
import { StudyAreaView } from '../src/View'

describe('Test StudyAreaView', function () {
  let el

  afterEach(() => el?.remove())

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
    expect(el.hasClass('leaflet-container')).toBe(true)
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
    expect(el.hasClass('leaflet-container')).toBe(true)
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
    expect(el.hasClass('leaflet-container')).toBe(true)
  })
})

// Styling helpers are exercised directly: constructing the view runs the full
// Leaflet render, which is not needed to check what style a feature resolves to.
//
// Deliberately a top-level describe rather than nested inside the one above.
// That one owns a DOM fixture and tears it down with afterEach(() => el?.remove()),
// which would also run after every test nested within it - and these tests never
// assign `el`. The specs run in a random order, so nesting them made the suite pass
// or fail depending on the seed: green locally, four failures on CI.
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

  it('shows a linked record title as text in its popup (dri-one #454)', () => {
    vi.spyOn(L.Layer.prototype, 'bindPopup')
    const feature = {
      type: 'Feature',
      properties: { title: '<img src=x onerror="window.__xss454=1">', link: 'https://example.com/" onmouseover="x' },
      geometry: { type: 'Point', coordinates: [-2.64501, 54.5261] }
    }
    const el = $('<div id="studyarea-map" class="studyarea-map"><span dataType="geoJson"/></div>')
    // Linked records arrive as a feature collection, the only path that adds popups
    el.find('span').attr('content', JSON.stringify({ type: 'FeatureCollection', features: [feature] }))
    el.appendTo($('body'))

    new StudyAreaView({ el }) // eslint-disable-line no-new
    el.remove()

    const popup = $('<div>').html(L.Layer.prototype.bindPopup.mock.calls[0][0])
    expect(popup.find('img').length).toBe(0)
    expect(popup.find('a').text()).toBe(feature.properties.title)
    expect(popup.find('a').attr('href')).toBe(feature.properties.link)
  })

  it('links a popup only to an http(s) address (dri-one #454)', () => {
    // Escaping does not stop a javascript: URL from running when the link is clicked
    vi.spyOn(L.Layer.prototype, 'bindPopup')
    const feature = {
      type: 'Feature',
      properties: { title: 'Site A', link: 'javascript:window.__xss454=1' },
      geometry: { type: 'Point', coordinates: [-2.64501, 54.5261] }
    }
    const el = $('<div id="studyarea-map" class="studyarea-map"><span dataType="geoJson"/></div>')
    el.find('span').attr('content', JSON.stringify({ type: 'FeatureCollection', features: [feature] }))
    el.appendTo($('body'))

    new StudyAreaView({ el }) // eslint-disable-line no-new
    el.remove()

    const popup = $('<div>').html(L.Layer.prototype.bindPopup.mock.calls[0][0])
    expect(popup.find('a').length).toBe(0)
    expect(popup.text()).toContain('Site A')
  })
})
