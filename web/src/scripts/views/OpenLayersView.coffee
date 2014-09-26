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
      eventListeners: 
        moveend: => @model.set "bbox", @getOpenlayersViewport()

    @map.addLayers [new OpenLayers.Layer.OSM()]
    @map.addLayers OpenLayersLayerFactory.createSearchResultsLayers(@model.getSearchResults())
    @map.zoomToExtent new OpenLayers.Bounds(-1885854.36, 6623727.12, 1245006.31, 7966572.83)

    @listenTo @model.getLayers(), "add", @addLayer
    @listenTo @model.getLayers(), "position", @positionLayer
    @listenTo @model.getLayers(), "reset", @resetLayers
    @listenTo @model.getLayers(), "remove", @removeLayer

    @listenTo @model, "change:bbox", -> console.log @model.get 'bbox'

  ###
  Add the given layer to the map by creating a new OpenLayers TMS layer and 
  then appending this to the layer object. Position this layer such that it
  appears above the baselayer and any other layers
  ###
  addLayer: (layer) ->
    @map.addLayer layer._openlayersTMS = OpenLayersLayerFactory.createLayer layer
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
  Get the current bbox object for the viewport of the openlayers map
  ###
  getOpenlayersViewport: ->
    extent = @map.getExtent()
                 .transform @map.getProjectionObject(), new OpenLayers.Projection("EPSG:4326")
    left: extent.left
    bottom: extent.bottom
    right: extent.right
    top: extent.top