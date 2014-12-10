define [
  'jquery'
  'backbone'
  'tpl!templates/DrawingControl.tpl'
], ($, Backbone, template) -> Backbone.View.extend
  events:
    "click button .glyphicon-pencil": "toggleDrawing"
    "click button":                   "open"

  ###
  This is the drawing control view. If not bounding box is currently set, it 
  will show a drawing tool so that a boundbox area can be selected
  ###
  initialize: ->
    do @render
    @listenTo @model, 'results-change', @render
    @listenTo @model, 'change:drawing', @updateDrawingToggle

  ###
  Ensure that the panel is open
  ###
  open:-> @model.set mapsearch:true

  ###
  Toggle the drawing mode of the model and ensure app is in map search mode
  ###
  toggleDrawing: -> 
    @model.set
      drawing:   not @model.get 'drawing'
      mapsearch: true

  ###
  Update the state of the drawing toggle button. Add and remove the active
  class depending on the drawing state of the model
  ###
  updateDrawingToggle:-> 
    toggle = if @model.get 'drawing' then 'addClass' else 'removeClass'
    @$('button')[toggle] 'active'

  render: -> 
    self = @model.getResults().get 'url'
    overlappingUrl = @model.getResults().get('overlappingBBox')
    @$el.html template
      removeBbox: @model.getResults().get 'withoutBBox'
      spatialOp:  if overlappingUrl then 'Intersecting' else 'Entirely Within'
      iswithin:   overlappingUrl or self
      intersects: @model.getResults().get('intersectingBBox') or self