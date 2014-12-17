define [
  'backbone'
  'tpl!templates/editor/Lineage.tpl'
], (Backbone, template) -> Backbone.View.extend

  events:
    'change #inputLineage': 'updateModel'

  initialize: ->
    do @render

  render: ->  @$el.html template
    value: @model.get 'lineage'

  updateModel: (evt) ->
    target = evt.currentTarget
    @model.set 'lineage', target.value
    console.log "new lineage: #{@model.get 'lineage'}"
