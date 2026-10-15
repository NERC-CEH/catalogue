import { beforeEach, describe, expect, it } from 'vitest'
import { BoundingBoxView, BoundingBox } from '../src/geometryMap'
import template from '../src/geometryMap/geometryTemplate'

describe('Test BoundingBoxView', function () {
  let model = null
  let view = null

  beforeEach(function () {
    model = new BoundingBox({ title: 'some text', template })
    view = new BoundingBoxView({ model })
  })

  it('test map is rendered', () => {
    // when
    view.render()
    // then
    expect(view.$('.map').length).toEqual(1) // "view.$('.map')).toBeDefined()" is not used here since it returns true regardless of the jquery selector used
  })

  it('when view is constructing should exist', () => {
    // then
    expect(view).toBeDefined()
  })

  it('copies a drawn box into its inputs when the row has an index', () => {
    // Drawing on the map sets the model. Inputs are found by data-name, since their ids carry the
    // row index (NERC-CEH/dri-one#452)
    const indexed = new BoundingBoxView({ model: new BoundingBox(), index: 'c42' })

    indexed.model.set({ westBoundLongitude: -4.5, eastBoundLongitude: 1.75, southBoundLatitude: 50.1, northBoundLatitude: 58.6 })

    expect(indexed.$('#boundingBoxc42WestBoundLongitude').val()).toBe('-4.5')
    expect(indexed.$('#boundingBoxc42EastBoundLongitude').val()).toBe('1.75')
    expect(indexed.$('#boundingBoxc42SouthBoundLatitude').val()).toBe('50.1')
    expect(indexed.$('#boundingBoxc42NorthBoundLatitude').val()).toBe('58.6')
  })
})
