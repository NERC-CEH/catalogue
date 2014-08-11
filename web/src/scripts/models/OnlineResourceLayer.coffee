define [
  'underscore'
  'cs!models/Layer'
], (_, Layer) -> Layer.extend
  defaults:
    opacity: 1
    visibility: 1

  initialize: (attr, options) ->
    @onlineResource = attr.onlineResource

  ###
  Returns the name of the layer as should be displayed in the legend
  ###
  getName:-> @get "name"

  ###
  Returns the address to the wms end point which should be used when
  making wms requests
  ###
  getWMS:-> "#{@onlineResource.url()}/wms"

  ###
  Gets the name of the wms layer which should be requested on each
  wms request
  ###
  getLayer:-> @get "name"