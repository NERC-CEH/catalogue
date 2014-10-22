define [
  'jquery'
  'underscore'
  'backbone'
  'openlayers'
], ($, _, Backbone, OpenLayers) -> Backbone.View.extend
  initialize: ->
    @map = new OpenLayers.Map
      div: @el
      maxExtent: new OpenLayers.Bounds(-20037508.34,-20037508.34,20037508.34,20037508.34)
      displayProjection: new OpenLayers.Projection("EPSG:3857")
      theme: null


    backdrop = new OpenLayers.Layer.OSM "OSM", [
      "//a.tile.openstreetmap.org/${z}/${x}/${y}.png",
      "//b.tile.openstreetmap.org/${z}/${x}/${y}.png",
      "//c.tile.openstreetmap.org/${z}/${x}/${y}.png"
    ]

    @map.addLayers [backdrop]
    @map.zoomToExtent new OpenLayers.Bounds(-1885854.36, 6623727.12, 1245006.31, 7966572.83)

    do @refresh

  ###
  Reset the position and size of the map. Parent views should call this method 
  when the map reappears onscreen
  ###
  refresh:->
    do @map.updateSize
    @map.zoomToExtent new OpenLayers.Bounds -1885854.36, 6623727.12, 1245006.31, 7966572.83