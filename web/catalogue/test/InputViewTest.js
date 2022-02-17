import { InputView, EditorMetadata } from '../src/Editor'
import _ from 'underscore'
import template from '../src/Editor/Input.tpl'

describe('Test InputView', function () {
  let model = null
  let view = null

  beforeEach(function () {
    model = new EditorMetadata({ title: 'some text' })
    view = new InputView({ model })
    view.template = _.template(template)
  })

  describe('when view is constructing', () => it('should exist', () => expect(view).toBeDefined()))
})
