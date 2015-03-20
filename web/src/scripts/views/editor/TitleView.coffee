define [
  'backbone'
  'tpl!templates/editor/Title.tpl'
], (Backbone, template) -> Backbone.View.extend

  events:
    'change': 'updateModel'

  initialize: ->
    if not @model
      throw new Error('model is required')

  render: ->
    @$el.html template
      value: @model.get 'title'
    return @

  updateModel: ->
    @model.set 'title', @$("#input-title").val()