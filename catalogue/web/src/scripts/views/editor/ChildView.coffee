define [
  'underscore'
  'backbone'
  'tpl!templates/editor/Child.tpl'
], (_, Backbone, template) -> Backbone.View.extend

  className: 'row'

  events:
    'click button.remove': 'delete'

  initialize: (options) ->
    @index = @model.collection.indexOf @model
    do @render
    new options.ObjectInputView
      el: @$('.dataentry')
      model: @model
      index: @index

  render: ->
    @$el.html template index: @index
    @

  delete: ->
    @model.collection.remove @model
    do @remove
