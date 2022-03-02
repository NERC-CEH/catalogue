import { InputView, EditorMetadata } from '../src/Editor'
import _ from 'underscore'
import template from '../src/Editor/Input.tpl'

describe('Test InputView', function () {
  let model = null
  let view = null

  beforeEach(function () {
    model = new EditorMetadata({ title: 'some text', template: template })
    view = new InputView({ model })
  })

  it('when view is constructing should exist', () => {
    // then
    expect(view).toBeDefined()
  })
})
