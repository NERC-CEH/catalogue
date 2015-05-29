define [
  'backbone'
  'tpl!templates/editor/Description.tpl'
], (Backbone, template) -> Backbone.View.extend

  events:
    'change': 'updateModel'

  render: ->
    @$el.html template
      value: @model.get 'description'
    return @

  updateModel: ->
    @model.set 'description', @$("#input-description").val()