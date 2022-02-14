import { MessageListView } from '../src/Message'
import template from '../src/Message/Messages.tpl'
import Backbone from 'backbone'

describe('Test MessageListView', () => {
  it('View should be defined', () => {
    // given
    const messages = new Backbone.Collection()

    // when
    const view = new MessageListView({
      el: '#messages',
      messages
    })

    // then
    expect(view).toBeDefined()
  })

  it('clearAll should be called', () => {
    // given
    const options = new Backbone.Model({
      messages: ['message', 'message']
    })
    const view = new MessageListView({ options: options })
    view.template = template
    spyOn(view, 'clearAll')
    view.delegateEvents()
    view.render()

    // when
    view.$('.clear-all').trigger('click')

    // then
    expect(view.clearAll).toHaveBeenCalled()
  })
})
