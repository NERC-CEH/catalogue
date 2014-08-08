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

    wms = new OpenLayers.Layer.WMS(
      'Test WMS',
      'https://localhost:8080/documents/ff1e7925-b592-44fe-8234-71adc387ef1f/onlineResources/2/wms',
      {layers:'LC.LandCoverSurfaces', transparent:true},
      {isBaseLayer:false, opacity:0.6, projection:new OpenLayers.Projection("EPSG:102100")});
    @map.addLayers [new OpenLayers.Layer.OSM(), wms]
    
    do @map.zoomToMaxExtent