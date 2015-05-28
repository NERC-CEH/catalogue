define [
  'backbone'
  'tpl!templates/editor/TopicCategoriesItem.tpl'
], (Backbone, template) -> Backbone.View.extend

  className: 'row'

  events:
    'click button': 'remove'
    'change select': 'modify'

  initialize: ->
    if not @model
      throw new Error('model is required')

  render: ->
    @$el.html template
    @$('select').val @model.get 'value'
    return @

  remove: ->
    @model.collection.remove @model

  modify: ->
    @model.set 'value', @$(':selected').val()