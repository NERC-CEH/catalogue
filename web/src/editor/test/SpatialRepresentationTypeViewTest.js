import { EditorMetadata } from '../src'
import { SpatialRepresentationTypeView } from '../src/views'

describe('Test SpatialRepresentationTypeView', function () {
  let model = null
  let view = null

  beforeEach(function () {
    model = new EditorMetadata({ title: 'some text' })
    view = new SpatialRepresentationTypeView({
      model,
      modelAttribute: 'spatialRepresentationTypes',
      label: 'Spatial Representation Types'
    })
  })

  it('when view is constructing should exist', () => {
    // then
    expect(view).toBeDefined()
  })

  it('renders', () => {
    view.render()
    expect(view.$('input')).toBeDefined()
  })
})
