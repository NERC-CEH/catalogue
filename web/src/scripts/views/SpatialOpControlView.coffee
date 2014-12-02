define [
  'jquery'
  'backbone'
  'tpl!templates/SpatialOpControl.tpl'
], ($, Backbone, template) -> Backbone.View.extend
  events:
    "click a": "updateModel"

  ###
  This is the drawing control view. If not bounding box is currently set, it
  will show a drawing tool so that a boundbox area can be selected
  ###
  initialize: ->
    do @render
    do @updateDisabled
    @listenTo @model, 'change:op', @updateButtonText
    @listenTo @model, 'change:bbox', @updateDisabled

  ###
  Update the model with the current spatial operation
  ###
  updateModel: (event) ->
    do event.preventDefault
    @model.set 'op', $(event.target).data('op')

  ###
  Update the button text
  ###
  updateButtonText: ->
    @$("#spatialOp").button @model.get 'op'

  ###
  Update the disabled state of the SpatialOp button.
  ###
  updateDisabled: ->
    @$('.dropdown-toggle').prop 'disabled', not (@model.get 'bbox')?

  render: -> @$el.html template