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
      eventListeners: 
        moveend: => @model.set "bbox", @getOpenlayersViewport()

    @map.addLayers [new OpenLayers.Layer.OSM()]
    @map.addLayers @_createSearchResultsLayers(@model.getSearchResults())
    @map.zoomToExtent new OpenLayers.Bounds(-1885854.36, 6623727.12, 1245006.31, 7966572.83)

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

  ###
  Create a drawing layer which represents the currently displayed search results
  ###
  _createSearchResultsLayers: (searchResults) ->
    wktFactory = new OpenLayers.Format.WKT #Create the wktFactory to convert openlayers features to wkt
    drawingLayer = new OpenLayers.Layer.Vector "Vector Layer"
    markerLayer = new OpenLayers.Layer.Markers "Marker Layer"

    epsg4326 = new OpenLayers.Projection("EPSG:4326")

    updateDrawingLayer =->
      do drawingLayer.removeAllFeatures
      do markerLayer.clearMarkers

      _.forEach searchResults.getResultsOnScreen(), (result) ->
        vector = wktFactory.read result.getLocations()
        vector.geometry.transform epsg4326, drawingLayer.map.getProjectionObject()
        vector.style =
          strokeColor: '#8fca89' 
          fillColor: '#8fca89'
          fillOpacity: 0.3

        drawingLayer.addFeatures vector
        markerLayer.addMarker new OpenLayers.Marker( new OpenLayers.LonLat(
          vector.geometry.bounds.right,
          vector.geometry.bounds.top
        ), new OpenLayers.Icon('/static/img/marker.png', {h:34, w:21}, {x:-10,y:-34}))


    searchResults.onscreen.on 'change', updateDrawingLayer
    return [drawingLayer, markerLayer]