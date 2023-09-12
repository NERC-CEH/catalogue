import { InputView, EditorMetadata } from '../src'
import template from '../src/Input.tpl'

describe('Test InputView', function () {
  let model = null
  let view = null

  beforeEach(function () {
    model = new EditorMetadata({ title: 'some text', template })
    view = new InputView({ model })
  })

  it('when view is constructing should exist', () => {
    // then
    expect(view).toBeDefined()
  })
})
