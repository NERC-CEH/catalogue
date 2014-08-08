define [
  'jquery'
  'underscore',
  'backbone',
  'openlayers'
], ($, _, Backbone, OpenLayers) -> Backbone.View.extend
  initialize: ->
    @map = new OpenLayers.Map
      div: @el
      maxExtent: new OpenLayers.Bounds(-20037508.34,-20037508.34,20037508.34,20037508.34)
      displayProjection: new OpenLayers.Projection("EPSG:3857")
      theme: null

    @map.addLayers [new OpenLayers.Layer.OSM()]
    
    do @map.zoomToMaxExtent