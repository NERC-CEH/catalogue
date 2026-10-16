import { afterEach, describe, expect, it } from 'vitest'
import $ from 'jquery'
import Backbone from 'backbone'
import L from 'leaflet'
import iconUrl from 'leaflet/dist/images/marker-icon.png'
import shadowUrl from 'leaflet/dist/images/marker-shadow.png'
import { BoundingBox, BoundingBoxView, Geometry, GeometryView } from '../src/geometryMap'
import SingleObjectView from '../src/views/SingleObjectView'

// The editor's maps, as an editor draws on them or types coordinates (dri-one #473)
describe('Editor maps', () => {
  const bounds = layer => layer.getBounds().toBBoxString()

  afterEach(() => $(document.body).empty())

  describe('a bounding box', () => {
    let model, view, collection

    // As a row of the record's bounding boxes, which draws its map once its section is shown
    function box (attributes = {}) {
      collection = new Backbone.Collection([], { model: BoundingBox })
      model = collection.add(new BoundingBox(attributes))
      view = new BoundingBoxView({ model, index: 'b1' })
      $(document.body).append(view.el)
      return view
    }
    const input = name => view.$(`[data-name=${name}]`)
    const toolbar = () => view.drawControl.options

    afterEach(() => view.map?.remove())

    it('draws a saved box once its section is shown, with the map fitted to it', () => {
      box({ westBoundLongitude: '-8.6', eastBoundLongitude: '1.7', southBoundLatitude: '49.8', northBoundLatitude: '60.8' })
      expect(view.map).toBeUndefined()

      collection.trigger('visible')

      expect(view.drawnItems.getLayers().length).toBe(1)
      expect(bounds(view.boundingBox)).toBe('-8.6,49.8,1.7,60.8')
      expect(view.map.getBounds().contains(view.boundingBox.getBounds())).toBe(true)
      expect(toolbar().draw.rectangle).toBe(false)
      expect(toolbar().edit.remove).toBeTruthy()
    })

    it('draws a box on the equator at Greenwich, where coordinates are 0', () => {
      box({ westBoundLongitude: 0, eastBoundLongitude: 1, southBoundLatitude: 0, northBoundLatitude: 1 })

      collection.trigger('visible')

      expect(view.drawnItems.getLayers().length).toBe(1)
    })

    it('draws the coordinates typed in, when asked to', () => {
      box()
      collection.trigger('visible')
      expect(view.drawnItems.getLayers().length).toBe(0)

      for (const [name, value] of [['westBoundLongitude', '-3'], ['eastBoundLongitude', '-2'], ['southBoundLatitude', '54'], ['northBoundLatitude', '55']]) {
        input(name).val(value).trigger('change')
      }
      view.$('.update-map').trigger('click')

      expect(bounds(view.boundingBox)).toBe('-3,54,-2,55')
    })

    it('takes its coordinates from a box drawn on the map, to three places', () => {
      box()
      collection.trigger('visible')

      view.map.fire(L.Draw.Event.CREATED, { layer: L.rectangle([[50.12345, -4.98765], [58.6, 1.75]]) })

      expect(model.toJSON()).toEqual({ westBoundLongitude: '-4.988', eastBoundLongitude: '1.750', southBoundLatitude: '50.123', northBoundLatitude: '58.600' })
      expect(input('westBoundLongitude').val()).toBe('-4.988')
      expect(toolbar().draw.rectangle).toBe(false)
    })

    it('keeps one box: another drawn is ignored', () => {
      box()
      collection.trigger('visible')
      view.map.fire(L.Draw.Event.CREATED, { layer: L.rectangle([[50, -4], [58, 1]]) })

      view.map.fire(L.Draw.Event.CREATED, { layer: L.rectangle([[10, 10], [20, 20]]) })

      expect(view.drawnItems.getLayers().length).toBe(1)
      expect(model.get('westBoundLongitude')).toBe('-4.000')
    })

    it('follows the box as it is moved or resized', () => {
      box({ westBoundLongitude: '-3', eastBoundLongitude: '-2', southBoundLatitude: '54', northBoundLatitude: '55' })
      collection.trigger('visible')

      view.map.fire(L.Draw.Event.EDITRESIZE, { layer: L.rectangle([[54, -3], [56, -1]]) })
      expect(model.get('northBoundLatitude')).toBe('56.000')
      view.map.fire(L.Draw.Event.EDITMOVE, { layer: L.rectangle([[44, -13], [46, -11]]) })
      expect(model.get('westBoundLongitude')).toBe('-13.000')
    })

    it('forgets its coordinates once the box is deleted, and offers drawing again', () => {
      box({ westBoundLongitude: '-3', eastBoundLongitude: '-2', southBoundLatitude: '54', northBoundLatitude: '55' })
      collection.trigger('visible')

      view.map.fire(L.Draw.Event.DELETED)

      expect(model.isEmpty()).toBe(true)
      expect(model.toJSON()).toEqual({})
      expect(input('northBoundLatitude').val()).toBe('')
      expect(toolbar().draw.rectangle).toBeTruthy()
    })

    it('says what is wrong with a coordinate typed in', () => {
      box()

      input('westBoundLongitude').val('200').trigger('change')

      expect(view.$('.validation').text()).toContain('West Bounding Longitude should be between -180')
      expect(view.$('.validation').css('display')).not.toBe('none')
    })
  })

  // As the editor builds one: before its section is in the page, which is hidden until it is shown.
  // Leaflet measured its map there as 0x0 and kept that size, so the map came up grey, and once
  // shown it was never measured again (dri-one #475).
  describe('a geometry in a section of the editor', () => {
    const polygon = '{"type":"Polygon","coordinates":[[[-3,54],[-2,54],[-2,55],[-3,55],[-3,54]]]}'
    const point = '{"type":"Point","coordinates":[-2.7,54.1]}'
    let section

    function field (geometryString) {
      const record = new Backbone.Model(geometryString ? { geometry: { geometryString } } : {})
      section = new SingleObjectView({
        model: record,
        modelAttribute: 'geometry',
        ModelType: Geometry,
        label: 'Geometry',
        ObjectInputView: GeometryView
      })
      $(document.body).append(section.el)
      return section
    }
    const map = () => section.objectInputView.map

    afterEach(() => map()?.remove())

    it('is sized to its section once that is shown', () => {
      field()

      section.show()

      expect(map().getSize().x).toBeGreaterThan(0)
      expect(map().getSize().y).toBeGreaterThan(0)
    })

    it('is fitted to a saved polygon once shown', () => {
      field(polygon)

      section.show()

      expect(map().getZoom()).toBeGreaterThan(4)
      expect(map().getBounds().contains(section.objectInputView.drawnItems.getBounds())).toBe(true)
    })

    it('centres on a saved point, near enough to see what is around it', () => {
      field(point)

      section.show()

      expect(map().getCenter().lat).toBeCloseTo(54.1, 3)
      expect(map().getCenter().lng).toBeCloseTo(-2.7, 3)
      expect(map().getZoom()).toBe(12)
    })
  })

  describe('a geometry', () => {
    let model, view
    const polygon = '{"type":"Polygon","coordinates":[[[-3,54],[-2,54],[-2,55],[-3,55],[-3,54]]]}'

    function geometry (attributes = {}) {
      model = new Geometry(attributes)
      view = new GeometryView({ model, el: $('<div>').appendTo(document.body) })
      return view
    }
    const toolbar = () => view.drawControl.options

    afterEach(() => view.map?.remove())

    it('draws a saved polygon, with the map fitted to it, and offers to delete it', () => {
      geometry({ geometryString: polygon })

      expect(view.drawnItems.getLayers().length).toBe(1)
      expect(view.map.getBounds().contains(view.drawnItems.getBounds())).toBe(true)
      expect(toolbar().draw.polygon).toBe(false)
      expect(toolbar().edit.remove).toBeTruthy()
    })

    it('offers a point or a polygon to draw when there is none', () => {
      geometry()

      expect(toolbar().draw.polygon).toBeTruthy()
      expect(toolbar().draw.marker).toBeTruthy()
      expect(toolbar().edit.remove).toBe(false)
    })

    it('keeps a point drawn on the map as GeoJSON, to five places', () => {
      geometry()

      view.map.fire(L.Draw.Event.CREATED, { layer: L.marker([54.1234567, -2.7654321]) })

      expect(JSON.parse(model.get('geometryString')).geometry).toEqual({ type: 'Point', coordinates: [-2.76543, 54.12346] })
      expect(view.$('#box').val()).toBe(model.get('geometryString'))
      expect(toolbar().edit.remove).toBeTruthy()
    })

    it('forgets the geometry once it is deleted', () => {
      geometry({ geometryString: polygon })

      view.map.fire(L.Draw.Event.DELETED)

      expect(model.hasGeometry()).toBe(false)
      expect(toolbar().draw.marker).toBeTruthy()
    })

    it('draws a point with Leaflet\'s marker, from the bundled images', () => {
      geometry({ geometryString: '{"type":"Point","coordinates":[-2.7,54.1]}' })

      // The images as bundled, whatever the bundler names them, not the bare marker-icon.png Leaflet
      // would otherwise fetch beside the page
      const icon = view.drawnItems.getLayers()[0].getLayers()[0].getIcon()
      expect(icon._getIconUrl('icon')).toBe(iconUrl)
      expect(icon._getIconUrl('shadow')).toBe(shadowUrl)
    })

    it('redraws the GeoJSON typed in', () => {
      geometry({ geometryString: polygon })

      view.$('#box').val('{"type":"Point","coordinates":[1,52]}').trigger('change')

      expect(model.get('geometryString')).toBe('{"type":"Point","coordinates":[1,52]}')
      expect(view.drawnItems.toGeoJSON().features[0].geometry).toEqual({ type: 'Point', coordinates: [1, 52] })
    })

    it('removes the shape when the GeoJSON is cleared', () => {
      geometry({ geometryString: polygon })

      view.$('#box').val('').trigger('change')

      expect(model.hasGeometry()).toBe(false)
      expect(view.drawnItems.getLayers().length).toBe(0)
    })

    it('says so, and keeps the shape, when what is typed is JSON but not a geometry', () => {
      geometry({ geometryString: polygon })

      view.$('#box').val('{"name":"Wales"}').trigger('change')

      expect(view.drawnItems.getLayers().length).toBe(1)
      expect(view.$('.validation').text()).toContain('not GeoJSON')
    })

    it('keeps the shape, and says so, when what is typed isn\'t GeoJSON', () => {
      geometry({ geometryString: polygon })

      expect(() => view.$('#box').val('{"type":"Point","coordinates":[1,').trigger('change')).not.toThrow()

      expect(view.drawnItems.getLayers().length).toBe(1)
      expect(view.$('.validation').text()).toContain('not GeoJSON')
      expect(view.$('.validation').css('display')).not.toBe('none')
    })
  })
})
