import { CatalogueView } from '../src/CatalogueApp'
import Backbone from 'backbone'

describe('Test EditorView', () => {
  let view = null
  let model = null

  beforeEach(() => {
    model = new Backbone.Model({
      options: [{ value: 'value', label: 'title' }, { value: 'value2', label: 'title2' },
        { value: 'value3', label: 'title3' }]
    })
    view = new CatalogueView({
      el: '#metadata',
      model
    })
  })

  it('View should be defined', () => {
    // then
    expect(view).toBeDefined()
  })
})
