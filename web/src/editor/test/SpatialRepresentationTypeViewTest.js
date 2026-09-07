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

  /**
   * removeChild re-renders to renumber the surviving rows (NERC-CEH/dri-one#298). This subclass
   * overrides render() to re-apply the select values, so the re-render must go through the override
   * or the survivors would come back blank.
   */
  describe('after a mid-list delete', () => {
    beforeEach(() => {
      model.set('spatialRepresentationTypes', ['grid', 'textTable', 'vector'])
      view = new SpatialRepresentationTypeView({
        model,
        modelAttribute: 'spatialRepresentationTypes',
        label: 'Spatial Representation Types'
      })
      view.$('#inputspatialRepresentationTypes1 button.remove').trigger('click')
    })

    it('renumbers the surviving rows', () => {
      expect(view.$('select').map((i, el) => el.getAttribute('data-index')).get()).toEqual(['0', '1'])
    })

    it('keeps the surviving rows selected', () => {
      expect(view.$('select').map((i, el) => el.value).get()).toEqual(['grid', 'vector'])
      expect(model.get('spatialRepresentationTypes')).toEqual(['grid', 'vector'])
    })
  })
})
