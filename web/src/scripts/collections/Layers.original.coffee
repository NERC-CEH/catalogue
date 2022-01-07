define [
  'cs!collections/Positionable'
  'cs!models/OnlineResourceLayer'
], (Positionable, OnlineResourceLayer) -> Positionable.extend
  model: (attr, options) ->
    return new OnlineResourceLayer attr,options if attr.onlineResource

  initialize:->
    @on "change:opacity change:visibility reset", @hideLayerInfo

  ###
  Scans over each of the layers contained within this collection and then hides
  the info display
  ###
  hideLayerInfo: ->
    @forEach (layer) -> layer.set 'infoVisible', false