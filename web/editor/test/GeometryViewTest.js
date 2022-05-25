import { Geometry, GeometryView } from '../src/geometryMap'

describe('Test GeometryView', function () {
  let model = null
  let view = null

  beforeEach(function () {
    model = new Geometry({ title: 'some text' })
    view = new GeometryView({ model })
  })

  it('test map is rendered', () => {
    // when
    view.render()
    view.createMap()
    // then
    expect(view.$('.map')).toBeDefined()
  })

  it('when view is constructing should exist', () => {
    // then
    setTimeout(expect(view).toBeDefined(), 5000)
  })
})
