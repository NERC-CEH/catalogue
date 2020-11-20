define [
  'jquery'
  'backbone'
  'cs!views/upload/simple/MessageListView'
], ($, Backbone, MessageListView) ->

  describe 'MessageListView', ->
    template =
      '<div>
        <div id="messages-tools"></div>
        <ul id="messages-list"></ul>
      </div>'
    el = null
    messages = null
    view = null

    beforeEach ->
      el = $(template).appendTo($('body'))
      spyOn(MessageListView.prototype, 'addMessage').and.callThrough()
      messages = new Backbone.Collection()
      view = new MessageListView
        el: el
        messages: messages

    afterEach ->
      el.remove()

    it 'renders Clear button', ->
      expect($('#messages-tools button').length).toEqual(1)

    it 'message added to collection triggers subview render', ->
      #when
      messages.add({message: 'test', type: 'info'})

      #then
      expect($('#messages-list li').length).toEqual(1)
      expect(view.addMessage).toHaveBeenCalled()

    it 'has clear DOM events', ->
      expect(view.events['click .clear-all']).toBeDefined()
      expect(view.events['click .clear-all']).toEqual('clearAll')
