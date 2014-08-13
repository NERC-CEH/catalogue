define [
  'jquery'
  'underscore'
  'backbone'
  'cs!views/LayerControlsView'
], ($, _, Backbone, LayerControlsView) -> Backbone.View.extend

  initialize: ->
    @listenTo @collection, 'add', @addLayer
    @listenTo @collection, 'remove', @removeLayer
    @listenTo @collection, 'reset', @resetLayers
    ###
    @$el.sortable
      start: (event, ui) => 
        @_oldPosition = (@collection.length - 1) - ui.item.index()
      update: (event, ui) =>
        newPosition = (@collection.length - 1) - ui.item.index()
        @collection.position @_oldPosition, newPosition
    ###
  addLayer: (layer) -> 
    layer._layerView = new LayerControlsView
      model: layer
      el: $('<li class="list-group-item"></li>').prependTo(@$el)

  removeLayer: (layer) -> do layer._layerView.remove

  resetLayers: (layers, options) ->
    _.each options.previousModels, (layer) => @removeLayer layer
    layers.forEach (layer) => @addLayer layer