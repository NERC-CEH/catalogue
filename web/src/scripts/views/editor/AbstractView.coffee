define [
  'backbone'
  'tpl!templates/editor/Abstract.tpl'
], (Backbone, template) -> Backbone.View.extend

  events:
    'change #inputAbstract': 'updateModel'

  initialize: ->
    do @render

  render: ->  @$el.html template
    value: @model.get 'description'

  updateModel: (event) ->
    target = event.currentTarget
    @model.set 'description', target.value
    console.log "new abstract: #{@model.get 'description'}"
