define [
  'backbone'
  'tpl!templates/editor/AlternateTitlesItem.tpl'
], (Backbone, template) -> Backbone.View.extend
  tagName: 'tr'

  events:
    'click button': 'remove'
    'change input': 'modify'

  initialize: ->
    if not @model
      throw new Error('model is required')

  render: ->
    @$el.html template @model.toJSON()
    return @

  remove: ->
    @model.collection.remove @model

  modify: ->
    @model.set 'value', @$(':input').val()