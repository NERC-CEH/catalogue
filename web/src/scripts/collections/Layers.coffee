define [
  'backbone'
  'cs!models/OnlineResourceLayer'
], (Backbone, OnlineResourceLayer) -> Backbone.Collection.extend
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

  ###
  Moves an existing element in the the collection from position index 
  to newPosition. Any "position" listeners of this instance will be 
  notified with the arguments: 
    model - the model which moved
    collection - this Layers instance
    newPosition - the new position of the model
    oldPosition - the position the model was in
  ###
  position: (index, newPosition) ->
    toMove = (@models.splice index, 1)[0]
    @models.splice newPosition, 0, toMove
    @trigger "position", toMove, @, newPosition, index