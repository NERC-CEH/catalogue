define [
  'underscore'
  'cs!models/Layer'
], (_, Layer) -> Layer.extend
  defaults:
    opacity: 0.5
    visibility: false
    infoVisible: false

  initialize: (attr, options) ->
    @onlineResource = attr.onlineResource

    Layer.prototype.initialize.call this, arguments #Initialize parent

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