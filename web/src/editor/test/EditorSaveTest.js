import Backbone from 'backbone'
import Swal from 'sweetalert2'
import { EditorView, EditorMetadata, GeminiEditorMetadata } from '../src'
import { BoundingBox, BoundingBoxView } from '../src/geometryMap'

/**
 * Records that could not be saved at all (NERC-CEH/dri-one#457): an unselected resource type made
 * validation throw and left the Save button stuck, and a cleared or half-filled bounding box was
 * sent as nulls, which the server could not read.
 */
describe('Saving a record', () => {
  describe('with no resource type selected', () => {
    // The server sends an unselected resource type back as {} (Keyword's "" values are dropped)
    it('validates without throwing', () => {
      const model = new GeminiEditorMetadata({ title: 'A dataset', resourceType: {} })

      expect(() => model.validate(model.attributes)).not.toThrow()
      expect(model.validate(model.attributes)).toBeUndefined()
    })

    // A saved record (with an id): initialize resets the flag on new ones
    it('still refuses an online service agreement', () => {
      const model = new GeminiEditorMetadata({ id: 'rec1', title: 'A dataset', resourceType: {}, hasOnlineServiceAgreement: true })

      expect(model.validate(model.attributes)).toEqual(['Invalid resource type to have an online service agreement'])
    })

    it('reads a resource type the editor has set as a model', () => {
      const model = new GeminiEditorMetadata({ id: 'rec1', title: 'A dataset', resourceType: new Backbone.Model({ value: 'dataset' }), hasOnlineServiceAgreement: true })

      expect(model.validate(model.attributes)).toBeUndefined()
    })
  })

  describe('the Save button', () => {
    let view

    beforeEach(() => {
      const ViewType = EditorView.extend({
        initialize () {
          this.sections = [{ label: 'One', title: 'One', views: [] }]
          return EditorView.prototype.initialize.apply(this)
        }
      })
      view = new ViewType({ model: new EditorMetadata({ title: 'A record' }) })
    })

    afterEach(() => { document.body.classList.remove('edit-mode') })

    it('is reset when saving throws', () => {
      spyOn(view.model, 'save').and.throwError('validation crashed')

      expect(() => view.save()).toThrowError('validation crashed')

      expect(view.$('#editorSave').prop('disabled')).toBe(false)
    })
  })

  describe('a single bounding box (monitoring records)', () => {
    let view
    let sent

    beforeEach(() => {
      const ViewType = EditorView.extend({
        initialize () {
          this.sections = [{ label: 'One', title: 'One', views: [] }]
          return EditorView.prototype.initialize.apply(this)
        }
      })
      view = new ViewType({ model: new EditorMetadata({ title: 'A record' }) })
      spyOn(view.model, 'save').and.callFake(function () {
        sent = JSON.parse(JSON.stringify(this.toJSON()))
        return {}
      })
      spyOn(Swal, 'fire')
    })

    afterEach(() => { document.body.classList.remove('edit-mode') })

    it('is left out once it has been cleared', () => {
      // SingleObjectView stores the box model itself on the record
      const box = new BoundingBox({ westBoundLongitude: '-3', eastBoundLongitude: '1', southBoundLatitude: '50', northBoundLatitude: '55' })
      box.clearBounds()
      view.model.set('boundingBox', box)

      view.save()

      expect(sent.boundingBox).toBeUndefined()
    })

    it('is sent as numbers when complete, including a zero coordinate', () => {
      view.model.set('boundingBox', new BoundingBox({ westBoundLongitude: '-3.5', eastBoundLongitude: '0', southBoundLatitude: '50', northBoundLatitude: '55.25' }))

      view.save()

      expect(sent.boundingBox).toEqual({ westBoundLongitude: -3.5, eastBoundLongitude: 0, southBoundLatitude: 50, northBoundLatitude: 55.25 })
    })
  })

  describe('validation', () => {
    const halfFilled = { westBoundLongitude: '-3', eastBoundLongitude: '1' }
    const message = 'A bounding box needs all four coordinates: west, east, south and north'

    it('refuses a half-filled bounding box in a list', () => {
      const model = new EditorMetadata({ title: 'A record', boundingBoxes: [halfFilled] })

      expect(model.validate(model.attributes)).toEqual([message])
    })

    it('refuses a half-filled single bounding box, held as a model', () => {
      const model = new EditorMetadata({ title: 'A record', boundingBox: new BoundingBox(halfFilled) })

      expect(model.validate(model.attributes)).toEqual([message])
    })

    it('accepts complete boxes, including zero coordinates', () => {
      const model = new EditorMetadata({
        title: 'A record',
        boundingBoxes: [{ westBoundLongitude: 0, eastBoundLongitude: 1, southBoundLatitude: 0, northBoundLatitude: 2 }]
      })

      expect(model.validate(model.attributes)).toBeUndefined()
    })
  })

  describe('a bounding box model', () => {
    it('forgets its coordinates when cleared', () => {
      const box = new BoundingBox({ westBoundLongitude: '-3', eastBoundLongitude: '1', southBoundLatitude: '50', northBoundLatitude: '55' })

      box.clearBounds()

      expect(box.toJSON()).toEqual({})
    })

    it('draws a box with a zero coordinate', () => {
      const view = new BoundingBoxView({
        // The server sends numbers, so a real zero arrives as 0, not '0'
        model: new BoundingBox({ westBoundLongitude: 0, eastBoundLongitude: 1, southBoundLatitude: 0, northBoundLatitude: 2 })
      })
      document.body.appendChild(view.el)

      view.viewMap()

      expect(view.shapeDrawn).toBe(true)
      view.el.remove()
    })
  })
})
