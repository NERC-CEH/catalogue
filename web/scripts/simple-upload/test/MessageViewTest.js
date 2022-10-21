
import Backbone from 'backbone'
import { MessageView } from '../src/Message'
describe('Test MessageView', function () {
  let el = null
  let collection = null
  let model = null
  let view = null

  beforeEach(function () {
    el = $('ul').appendTo($('body'))
    spyOn(MessageView.prototype, 'remove').and.callThrough()
    model = new Backbone.Model({
      message: 'Hello World',
      type: 'info'
    })
    collection = new Backbone.Collection([model])
    view = new MessageView({
      el,
      model
    })
  })

  afterEach(() => el.remove())

  it('removed when model is destroyed', function () {
    // when
    collection.pop()

    // then
    expect(view.remove).toHaveBeenCalled()
  })
})
