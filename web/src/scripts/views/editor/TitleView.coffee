define [
  'backbone'
  'tpl!templates/editor/Title.tpl'
], (Backbone, template) -> Backbone.View.extend

  events:
    'change #inputTitle': 'updateModel'

  initialize: ->
    do @render

  render: ->  @$el.html template
    value: @model.get 'title'

  updateModel: (event) ->
    target = event.currentTarget
    @model.set 'title', target.value
    console.log "new title: #{@model.get 'title'}"
