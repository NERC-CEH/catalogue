import Backbone from 'backbone'
import { MessageListView } from '../src/Message'
describe('Test MessageListView', function () {
  const template =
    `<div> 
<div id="messages-tools"></div> 
<ul id="messages-list"></ul> 
</div>`
  let el = null
  let messages = null
  let view = null

  beforeEach(function () {
    el = $(template).appendTo($('body'))
    spyOn(MessageListView.prototype, 'addMessage').and.callThrough()
    messages = new Backbone.Collection()
    view = new MessageListView({
      el,
      messages
    })
  })

  afterEach(() => el.remove())

  it('renders Clear button', () => expect($('#messages-tools button')).toBeDefined())

  it('message added to collection triggers subview render', function () {
    // when
    messages.add({ message: 'test', type: 'info' })

    // then
    expect($('#messages-list li').length).toEqual(1)
    expect(view.addMessage).toHaveBeenCalled()
  })

  it('has clear DOM events', function () {
    expect(view.events['click .clear-all']).toBeDefined()
    expect(view.events['click .clear-all']).toEqual('clearAll')
  })
})
