define [
  'backbone'
  'tpl!templates/LayerControls.tpl'
  'jquery.ui/slider'
], (Backbone, template) -> Backbone.View.extend

  events:
    "change input.visibility": "updateVisibility"
    "slide .slider" : (evt, ui) -> @model.set 'opacity', ui.value

  initialize: ->
    do @render

    @listenTo @model, 'change:visibility', @updateToggle
    @listenTo @model, 'change:opacity', @updateOpacity
  
  ###
  Updates the toggle based upon the state of the visibility property
  ###
  updateToggle: ->
    @$("input.visibility").prop "checked", model.get "visibility"

  ###
  Updates the visibility property on the model based upon the state of the of 
  the ui toggle 
  ###
  updateVisibility: ->
    toggle = @$('input.visibility').is ':checked'
    @model.set 'visibility', toggle

  ###
  Syncs the value for opacity set in the model to the value the slider is
  currently showing
  ###
  updateOpacity: ->
    @opacity = @model.getOpacity()
    @opacitySlider.slider "value", @opacity

  render: ->
    @$el.html template @model.toJSON()

    @opacitySlider = @$('.slider').slider max: 1, step: 0.01

    do @updateOpacity