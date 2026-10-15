import $ from 'jquery'
import Backbone from 'backbone'
import L from 'leaflet'
import { BoundingBox, BoundingBoxView, Geometry, GeometryView } from '../src/geometryMap'
import CatalogueViewView from '../../catalogue/src/CatalogueApp/CatalogueViewView'
import CatalogueViewModel from '../../catalogue/src/CatalogueApp/CatalogueViewModel'
import { fakeServer, spy } from '../../test/support'

// The editor's maps, as an editor draws on them or types coordinates, and the dialog that shares a
// record into other catalogues (dri-one #473)
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
      expect(view.map.getBounds().contains(view.boundingBox.getBounds())).toBeTrue()
      expect(toolbar().draw.rectangle).toBeFalse()
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
      expect(toolbar().draw.rectangle).toBeFalse()
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

      expect(model.isEmpty()).toBeTrue()
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
      expect(view.map.getBounds().contains(view.drawnItems.getBounds())).toBeTrue()
      expect(toolbar().draw.polygon).toBeFalse()
      expect(toolbar().edit.remove).toBeTruthy()
    })

    it('offers a point or a polygon to draw when there is none', () => {
      geometry()

      expect(toolbar().draw.polygon).toBeTruthy()
      expect(toolbar().draw.marker).toBeTruthy()
      expect(toolbar().edit.remove).toBeFalse()
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

      expect(model.hasGeometry()).toBeFalse()
      expect(toolbar().draw.marker).toBeTruthy()
    })

    it('draws a point with Leaflet\'s marker, from the bundled images', () => {
      geometry({ geometryString: '{"type":"Point","coordinates":[-2.7,54.1]}' })

      const icon = view.drawnItems.getLayers()[0].getLayers()[0].getIcon()
      const url = name => new URL(icon._getIconUrl(name), window.location.href).pathname
      expect(url('icon')).toMatch(/\/marker-icon\.[0-9a-f]+\.png$|\/[0-9a-f]+\.png$/)
      expect(url('shadow')).not.toMatch(/\/marker-shadow\.png$/)
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

      expect(model.hasGeometry()).toBeFalse()
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

describe('Sharing a record into other catalogues', () => {
  let model, view

  const catalogues = [{ id: 'ukceh', title: 'UKCEH digital assets' }, { id: 'assist', title: 'About ASSIST' }]

  function open (attributes, offered = catalogues) {
    $(document.body).html('<div id="metadata"></div>')
    model = new CatalogueViewModel(attributes)
    model.catalogues = offered
    model.setRevision('r1')
    view = new CatalogueViewView({ el: '#metadata', model })
  }
  const box = id => view.$(`input[type=checkbox][value=${id}]`)

  beforeEach(() => {
    fakeServer.install()
    $('html').attr('data-catalogue', 'eidc')
  })

  afterEach(() => {
    fakeServer.uninstall()
    $('html').removeAttr('data-catalogue').removeData('catalogue')
    $('body').removeClass('edit-mode')
    $(document.body).empty()
  })

  it('offers each other catalogue, ticked where the record is shown already', () => {
    open({ id: 'abc', value: ['ukceh'] })

    expect(view.$('.form-check label').map((i, label) => label.textContent).get()).toEqual(['UKCEH digital assets', 'About ASSIST'])
    expect(box('ukceh').prop('checked')).toBeTrue()
    expect(box('assist').prop('checked')).toBeFalse()
  })

  it('reads a record shown in no other catalogue, which comes without a value', () => {
    open({ id: 'abc' })

    expect(view.$('input[type=checkbox]:checked').length).toBe(0)
  })

  it('says so when there is no other catalogue', () => {
    open({ id: 'abc' }, [])

    expect(view.$('.dataentry').text()).toBe('No other catalogues available.')
  })

  it('shows a catalogue\'s title as text', () => {
    open({ id: 'abc' }, [{ id: 'odd', title: 'Rivers <b>& lakes</b>' }])

    expect(view.$('.form-check label').text()).toBe('Rivers <b>& lakes</b>')
    expect(view.$('.form-check label b').length).toBe(0)
  })

  it('saves the catalogues ticked, against the revision it was opened at', () => {
    open({ id: 'abc', value: ['ukceh'] })

    box('assist').prop('checked', true).trigger('change')
    box('ukceh').prop('checked', false).trigger('change')
    expect(model.get('value')).toEqual(['assist'])
    view.$('#editorSave').trigger('click')

    const request = fakeServer.lastRequest()
    expect(request.method).toBe('PUT')
    expect(request.url).toBe('/documents/abc/catalogue-view')
    expect(JSON.parse(request.body).value).toEqual(['assist'])
    expect(request.headers['If-Match']).toBe('r1')
    const saved = spy('saved')
    model.on('sync', saved)
    request.respondWith({ status: 200, contentType: 'application/json', responseText: '{"id":"abc","value":["assist"]}', responseHeaders: { ETag: '"r2"' } })
    expect(saved).toHaveBeenCalled()
    expect(model.getRevision()).toBe('r2')
  })
})
