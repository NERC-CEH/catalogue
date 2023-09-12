import { BoundingBoxView, BoundingBox } from '../src/geometryMap'
import template from '../src/geometryMap/Geometry.tpl'

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
    expect(view.$('.map')).toBeDefined()
  })

  it('when view is constructing should exist', () => {
    // then
    expect(view).toBeDefined()
  })
})
