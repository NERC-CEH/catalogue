import { GeometryView, Geometry } from '../src/geometryMap/index'
import template from '../src/geometryMap/Geometry.tpl'

describe('Test GeometryView', function () {
  let model = null
  let view = null

  beforeEach(function () {
    model = new Geometry({ title: 'some text', template: template })
    view = new GeometryView({ model })
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
