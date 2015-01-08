define [
  'jquery'
  'backbone'
  'tpl!templates/editor/Textarea.tpl'
], ($, Backbone, template) -> Backbone.View.extend

  events:
    'change': 'updateModel'

  initialize: ->
    @parent = @model.get 'parent'
    do @render

  render: ->  @$el.html template _.clone @model.attributes

  updateModel: (event) ->
    text = $("#input#{@model.id}", event.currentTarget).val()
    metadata = @parent.getMetadata()
    metadata.set @model.id, text
    @parent.set 'metadata', metadata