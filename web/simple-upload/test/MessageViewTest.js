import Backbone from 'backbone'
import { MessageView } from '../src/Message'
import template from '../src/Message/Message.tpl'

describe('Test MessageView', () => {
  it('View should be defined', () => {
    // when
    const view = new MessageView()

    // then
    expect(view).toBeDefined()
  })

  it('Remove should be triggered', () => {
    // given
    const model = new Backbone.Model({
      type: 'any',
      message: 'test'
    })
    const view = new MessageView({ model: model })
    view.template = template
    spyOn(view, 'remove')
    view.initialize()
    view.delegateEvents()
    view.render()

    // when
    model.trigger('remove')

    // then
    expect(view.remove).toHaveBeenCalled()
  })
})
