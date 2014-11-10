define [
  'jquery'
  'backbone'
  'cs!views/ErrorMessageView'
], ($, Backbone, ErrorMessageView) ->
  describe "ErrorMessageView", ->
    el = null
    model = null

    beforeEach ->
      el = $('<div></div>').hide().appendTo $('body') #el is hidden at start
      model = new Backbone.Model

    afterEach ->
      do el.remove
    
    it "appends the default message when no specific message is supplied", ->
      view = new ErrorMessageView model: model, el: el
      view.defaultMessage = 'some default message'
      model.trigger 'error'

      expect($(el).html()).toContain 'some default message'

    it "is visible when a message is added", ->
      view = new ErrorMessageView model: model, el: el
      model.trigger 'error'

      expect(el.is ':visible').toBe true

    it "removes message when dismissed", ->
      view = new ErrorMessageView model: model, el: el
      model.trigger 'error'

      do $('.glyphicon-remove', el).click

      expect(el.children().length).toBe 0

    it "is hidden when all messages are dismissed", ->
      view = new ErrorMessageView model: model, el: el
      model.trigger 'error'

      do $('.glyphicon-remove', el).click
      expect(el.is ':visible').toBe false