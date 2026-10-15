import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
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

    it('accepts a resource type that may have an online service agreement', () => {
      const model = new GeminiEditorMetadata({ id: 'rec1', title: 'A dataset', resourceType: { value: 'dataset' }, hasOnlineServiceAgreement: true })

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
      vi.spyOn(view.model, 'save').mockImplementation(() => { throw new Error('validation crashed') })

      expect(() => view.save()).toThrowError('validation crashed')

      expect(view.$('#editorSave').prop('disabled')).toBe(false)
    })
  })

  describe('validation', () => {
    const halfFilled = { westBoundLongitude: '-3', eastBoundLongitude: '1' }
    const message = BoundingBox.INCOMPLETE

    it('refuses a half-filled bounding box in a list', () => {
      const model = new EditorMetadata({ title: 'A record', boundingBoxes: [halfFilled] })

      expect(model.validate(model.attributes)).toEqual([message])
    })

    it('refuses a half-filled single bounding box', () => {
      const model = new EditorMetadata({ title: 'A record', boundingBox: halfFilled })

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
    it('knows whether it is empty, incomplete or complete', () => {
      expect(new BoundingBox().isEmpty()).toBe(true)
      expect(new BoundingBox({ westBoundLongitude: '', eastBoundLongitude: null }).isEmpty()).toBe(true)
      expect(new BoundingBox({ westBoundLongitude: '-3' }).isIncomplete()).toBe(true)
      const zeroes = new BoundingBox({ westBoundLongitude: 0, eastBoundLongitude: 0, southBoundLatitude: 0, northBoundLatitude: 0 })
      expect(zeroes.isIncomplete()).toBe(false)
      expect(zeroes.isEmpty()).toBe(false)
    })

    it('sends only its four coordinates, not the bounds and coordinates the server derives', () => {
      // A saved box comes back from the server with bounds (GeoJSON) and coordinates added
      const saved = {
        westBoundLongitude: -3.5,
        eastBoundLongitude: 0,
        southBoundLatitude: 50,
        northBoundLatitude: 55.25,
        bounds: '{"type": "Feature"}',
        coordinates: '[[[-3.5, 50]]]'
      }
      expect(new BoundingBox(saved).toJSON()).toEqual({ westBoundLongitude: -3.5, eastBoundLongitude: 0, southBoundLatitude: 50, northBoundLatitude: 55.25 })

      const cleared = new BoundingBox(saved)
      cleared.clearBounds()
      expect(cleared.toJSON()).toEqual({})
    })

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
