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

  ###
  Sets the info visibility for this layer to the value specified. If we are 
  going to show then hide any other layers info in the collection
  ###
  setInfoVisibility: (visible)->
    do @collection.hideLayerInfo if visible
    @set 'infoVisible', visible

  ###
  Returns the address to an image which represents the legend of this layer
  ###
  getLegend: -> "#{@getWMS()}?REQUEST=GetLegendGraphic&VERSION=1.1.1&FORMAT=image/png&LAYER=#{@getLayer()}&SERVICE=WMS&"
