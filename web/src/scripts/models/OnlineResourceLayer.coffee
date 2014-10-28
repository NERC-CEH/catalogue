define [
  'underscore'
  'cs!models/Layer'
], (_, Layer) -> Layer.extend
  defaults:
    opacity:     0.5
    visibility:  false
    infoVisible: false

  initialize: (attr, options) ->
    @onlineResource = attr.onlineResource

    Layer.prototype.initialize.call this, arguments #Initialize parent
    
  ###
  Returns the address to the tms end point which should be used when
  making wms requests
  ###
  getTMS:-> "#{@onlineResource.url()}/tms/"

  ###
  Gets the address to the legend
  ###
  getLegend: -> "#{@onlineResource.url()}/#{@getName()}/legend"