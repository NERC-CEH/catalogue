define [
  'backbone'
  'tpl!templates/LayerControls.tpl'
  'tpl!templates/LayerInfo.tpl'
  'jquery-ui/slider'
], (Backbone, controlsTemplate, infoTemplate) -> Backbone.View.extend

  events:
    "change input.visibility": "updateVisibility"
    "slide .slider" : (evt, ui) -> @model.set 'opacity', ui.value
    "click .info" : "toggleLayerInfo"

  initialize: ->
    do @render

    @listenTo @model, 'change:visibility', @updateToggle
    @listenTo @model, 'change:opacity', @updateOpacity
    @listenTo @model, 'change:infoVisible', @updateInfoVisibility
  
  toggleLayerInfo: ->
    @model.setInfoVisibility not @model.get 'infoVisible'

  updateInfoVisibility:-> 
    visibility = if @model.get 'infoVisible' then 'show' else 'hide'
    @$('.info').popover visibility

  ###
  Updates the toggle based upon the state of the visibility property
  ###
  updateToggle: ->
    @$("input.visibility").prop "checked", @model.get "visibility"

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
    @$el.html controlsTemplate @model.toJSON()
    @opacitySlider = @$('.slider').slider max: 1, step: 0.01
    
    @infoPopover = @$('.info').popover
      placement: 'right'
      content:    infoTemplate @model # Generate some info content
      trigger:    'manual'            # We will handle the popover in backbone
      html:       true
      animation:  false
      viewport:   '#mapviewer'
      container:  '#mapviewer'

    do @updateOpacity #Ensure that the opacity value is set correctly
    do @updateInfoVisibility