define [
  'jquery'
  'underscore'
  'backbone'
  'openlayers'
  'cs!helpers/OpenLayersLayerFactory'
], ($, _, Backbone, OpenLayers, OpenLayersLayerFactory) -> Backbone.View.extend
  initialize: ->
    @map = new OpenLayers.Map
      div: @el
      maxExtent: new OpenLayers.Bounds(-20037508.34,-20037508.34,20037508.34,20037508.34)
      displayProjection: new OpenLayers.Projection("EPSG:3857")
      theme: null

    @map.addLayers [new OpenLayers.Layer.OSM()]
    @map.zoomToExtent new OpenLayers.Bounds(-1885854.36, 6623727.12, 1245006.31, 7966572.83)

    @listenTo @model.getLayers(), "add", @addLayer
    @listenTo @model.getLayers(), "reset", @resetLayers
    @listenTo @model.getLayers(), "remove", @removeLayer

  ###
  Add the given layer to the map by creating a new OpenLayers WMS layer and 
  then appending this to the layer object. Position this layer such that it
  appears above the baselayer and any other layers
  ###
  addLayer: (layer) ->
    @map.addLayer layer._openlayersWMS = OpenLayersLayerFactory.createLayer layer
    @map.setLayerIndex layer._openlayersWMS, @map.getNumLayers() - 1
    
  ###
  Remove the wms layer associated with the given layer
  ###
  removeLayer: (layer)-> @map.removeLayer layer._openlayersWMS

  ###
  Remove all the old wms layers and replace with the reset collection
  ###
  resetLayers: (layers, options) ->
    _.each options.previousModels, (layer) => @removeLayer layer
    layers.forEach (layer) => @addLayer layer