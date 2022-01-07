define [
  'underscore'
  'jquery'
  'backbone'
  'tpl!templates/Message.tpl'
], (_, $, Backbone, template) -> Backbone.View.extend
  el: '#message-panel'

  defaultMessage: 'A requested resource failed to load.'

  events:
    "click span.fas": "dismissMessage"

  initialize: ->
    @listenTo @model, 'error', @appendError
    @listenTo @model, 'info', @appendInfo

  ###
  Handle the error event.
  ###
  appendError: (model, res)->
    do @$el.show

    if _.isString model
      message = model

    @$el.append template
      type: 'danger'
      message: message or res?.responseJSON?.message or @defaultMessage

  ###
  Handle the info event.
  ###
  appendInfo: (message)->
    do @$el.show

    @$el.append template
      type: 'info'
      message: message

  dismissMessage: (evt) -> 
    do $(evt.target).parent('.alert').remove
    do @$el.hide if @$el.children().length is 0