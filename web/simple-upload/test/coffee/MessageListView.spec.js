/* eslint-disable
    no-multi-str,
    no-return-assign,
    no-undef,
*/
// TODO: This file was created by bulk-decaffeinate.
// Fix any style issues and re-enable lint.
/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/master/docs/suggestions.md
 */
define([
  'jquery',
  'backbone',
  'cs!views/upload/simple/MessageListView'
], ($, Backbone, MessageListView) => describe('MessageListView', function () {
  const template =
    '<div> \
<div id="messages-tools"></div> \
<ul id="messages-list"></ul> \
</div>'
  let el = null
  let messages = null
  let view = null

  beforeEach(function () {
    el = $(template).appendTo($('body'))
    spyOn(MessageListView.prototype, 'addMessage').and.callThrough()
    messages = new Backbone.Collection()
    return view = new MessageListView({
      el,
      messages
    })
  })

  afterEach(() => el.remove())

  it('renders Clear button', () => expect($('#messages-tools button').length).toEqual(1))

  it('message added to collection triggers subview render', function () {
    // when
    messages.add({ message: 'test', type: 'info' })

    // then
    expect($('#messages-list li').length).toEqual(1)
    return expect(view.addMessage).toHaveBeenCalled()
  })

  return it('has clear DOM events', function () {
    expect(view.events['click .clear-all']).toBeDefined()
    return expect(view.events['click .clear-all']).toEqual('clearAll')
  })
}))
