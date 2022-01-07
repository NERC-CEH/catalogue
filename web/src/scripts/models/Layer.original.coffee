define [
  'underscore'
  'backbone'
], (_, Backbone) -> Backbone.Model.extend

  initialize: ->
    @on 'change:opacity', -> @set 'visibility', true

  ###
  Get the current opacity value between 0-1 for this layer
  ###
  getOpacity: -> @get "opacity"

  ###
  Check if this layer is visible and should be renedered
  ###
  isVisible: -> @get "visibility"

  ###
  Returns the name of the layer which should be used to reference this layer
  when called agaist its tms end point
  ###
  getName:-> @get "name"

  ###
  Returns the name of the layer as should be displayed in the legend
  ###
  getTitle:-> @get "title"

  ###
  Returns the address to the tms end point which should be used when
  making tms requests
  ###
  getTMS:-> @get "tms"

  ###
  Returns the address to an image which represents the legend of this layer
  ###
  getLegend: -> @get "legend"

  ###
  Sets the info visibility for this layer to the value specified. If we are 
  going to show then hide any other layers info in the collection
  ###
  setInfoVisibility: (visible)->
    do @collection.hideLayerInfo if visible
    @set 'infoVisible', visible
