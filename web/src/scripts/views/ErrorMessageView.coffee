define [
  'jquery'
  'backbone'
  'tpl!templates/ErrorMessage.tpl'
], ($, Backbone, template) -> Backbone.View.extend
  el: '#message-panel'

  defaultMessage: 'A requested resource failed to load.'

  events:
    "click span.glyphicon": "dismissMessage"

  initialize: ->
    @listenTo @model, 'error', @appendMessage

  ###
  Handle the error event. 
  ###
  appendMessage: (model, res)->
    do @$el.show
    @$el.append template
      message: res?.responseJSON?.message or @defaultMessage

  dismissMessage: (evt) -> 
    do $(evt.target).parent('.alert').remove
    do @$el.hide if @$el.children().length is 0