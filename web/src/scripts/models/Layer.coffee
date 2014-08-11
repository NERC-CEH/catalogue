define [
  'underscore'
  'backbone'
], (_, Backbone) -> Backbone.Model.extend

  ###
  Get the current opacity value between 0-1 for this layer
  ###
  getOpacity: -> @get "opacity"

  ###
  Check if this layer is visible and should be renedered
  ###
  isVisible: -> @get "visibility"

  ###
  Returns the name of the layer as should be displayed in the legend
  ###
  getName:-> @get "name"

  ###
  Returns the address to the wms end point which should be used when
  making wms requests
  ###
  getWMS:-> @get "wms"

  ###
  Gets the name of the wms layer which should be requested on each
  wms request
  ###
  getLayer:-> @get "layer"