define [
  'jquery'
  'backbone'
  'cs!views/upload/simple/MessageView'
], ($, Backbone, MessageView) ->

  describe "MessageView", ->
    el = null
    collection = null
    model = null
    view = null

    beforeEach ->
      el = $('ul').appendTo($('body'))
      spyOn(MessageView.prototype, 'remove').and.callThrough()
      model = new Backbone.Model
        message: 'Hello World'
        type: 'info'
      collection = new Backbone.Collection([model])
      view = new MessageView
        el: el
        model: model

    afterEach ->
      el.remove()

    it 'removed when model is destroyed', ->
      #when
      collection.pop()

      #then
      expect(view.remove).toHaveBeenCalled()
