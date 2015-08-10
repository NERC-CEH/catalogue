define [
  'underscore'
  'backbone'
  'tpl!templates/editor/Child.tpl'
], (_, Backbone, template) -> Backbone.View.extend

  className: 'row'

  events:
    'click button.remove': 'delete'

  initialize: (options) ->
    @data = options
    @listenTo @model, 'remove', -> do @remove
    @index = @model.collection.indexOf @model
    do @render
    new @data.ObjectInputView _.extend {}, @data,
      el: @$('.dataentry')
      model: @model

  render: ->
    @$el.html template index: @index
    @

  delete: ->
    @model.collection.remove @model
