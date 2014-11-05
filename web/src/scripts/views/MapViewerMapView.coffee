define [
  'jquery'
  'underscore'
  'backbone'
  'cs!views/OpenLayersView'
  'openlayers'
], ($, _, Backbone, OpenLayersView, OpenLayers) -> OpenLayersView.extend
  initialize: ->
    OpenLayersView.prototype.initialize.call this, arguments #Initialize super
    
    @listenTo @collection, "add", @addLayer
    @listenTo @collection, "position", @positionLayer
    @listenTo @collection, "reset", @resetLayers
    @listenTo @collection, "remove", @removeLayer

  ###
  Add the given layer to the map by creating a new OpenLayers WMS layer and 
  then appending this to the layer object. Position this layer such that it
  appears above the baselayer and any other layers
  ###
  addLayer: (layer) ->
    @map.addLayer layer._openlayersTMS = @_createLayer layer
    @map.setLayerIndex layer._openlayersTMS, @map.getNumLayers() - 1
    
  ###
  Listens to when layers have been repositioned. Notify the OpenLayers Map and set the 
  new index for that layer
  ###
  positionLayer: (layer, collection, newPosition)->
    @map.setLayerIndex layer._openlayersTMS, newPosition + 1

  ###
  Remove the wms layer associated with the given layer
  ###
  removeLayer: (layer)-> @map.removeLayer layer._openlayersTMS

  ###
  Remove all the old wms layers and replace with the reset collection
  ###
  resetLayers: (layers, options) ->
    _.each options.previousModels, (layer) => @removeLayer layer
    layers.forEach (layer) => @addLayer layer

  ###
  Create an openlayers layer given some model/Layer which updates when different parts
  of the layer change
  ###
  _createLayer: (layer) -> 
    tmsLayer = new OpenLayers.Layer.TMS layer.getName(), layer.getTMS(),
        layername:   layer.getName()
        type:        'png'
        isBaseLayer: false
        opacity:     layer.getOpacity()
        visibility:  layer.isVisible()

    layer.on 'change:opacity', -> tmsLayer.setOpacity layer.getOpacity()
    layer.on 'change:visibility', -> tmsLayer.setVisibility layer.isVisible()
    return tmsLayer