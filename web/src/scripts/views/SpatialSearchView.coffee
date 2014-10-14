define [
  'jquery'
  'underscore'
  'backbone'
  'openlayers'
], ($, _, Backbone, OpenLayers) -> Backbone.View.extend
  highlighted:
    strokeColor: '#8fca89' 
    fillColor:   '#8fca89'
    fillOpacity: 0.3

  ###
  Define some openlayer constants
  ###
  marker:     new OpenLayers.Icon '/static/img/marker.png', {h:34, w:21}, {x:-10,y:-34}
  wktFactory: new OpenLayers.Format.WKT
  epsg4326:   new OpenLayers.Projection "EPSG:4326"

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

    # Create the layers to draw the search results on
    @drawingLayer = new OpenLayers.Layer.Vector "Vector Layer"
    @markerLayer = new OpenLayers.Layer.Markers "Marker Layer"
    @map.addLayers [backdrop, @drawingLayer, @markerLayer]

    @map.zoomToExtent new OpenLayers.Bounds(-1885854.36, 6623727.12, 1245006.31, 7966572.83)

    do @updateHighlightedRecord
    do @updateBBox
    @listenTo @model, 'change:results results-change:selected', @updateHighlightedRecord
    @listenTo @model, 'change:spatialSearch', @updateBBox

    # Create a debounced method of @updateBBox, this one will wait until the interaction
    # with the map has completed before triggering @updateBBox proper
    @delayedUpdateBBox = _.debounce @updateBBox, 500
    @map.events.register 'move', @map, => do @handleMove 
      
  ###
  An event listener for handling when the map has been moved. Only update the
  model if spatialSearching is enabled
  ###
  handleMove:->
    if @model.get 'spatialSearch'
      do @model.clearResults
      do @delayedUpdateBBox

  ###
  Update the current value of the bbox value on the model to match the 
  current viewport
  ###
  updateBBox:->
    if @model.get 'spatialSearch'
      extent = @map.getExtent()
                   .transform @map.getProjectionObject(), new OpenLayers.Projection("EPSG:4326")
      @model.setBBox [extent.left, extent.bottom, extent.right, extent.top].join ','
  
  ###
  Clear any markers or features which represent the old selected record. Then
  check to see if a record has been selected, if so populate
  ###
  updateHighlightedRecord: ->
    # Remove all the old markers
    do @drawingLayer.removeAllFeatures
    do @markerLayer.clearMarkers

    # Get the selected result
    selected = @model.get('results')?.getSelectedResult()

    # If that result is not undefined then render as a marker and polygon
    if selected
      vector = @wktFactory.read @convertToWKT selected.locations
      vector.geometry.transform @epsg4326, @drawingLayer.map.getProjectionObject()
      vector.style = @highlighted

      centroid = vector.geometry.components[0].getCentroid()
      lonLat = new OpenLayers.LonLat centroid.x, centroid.y
      @markerLayer.addMarker new OpenLayers.Marker lonLat, @marker
      @drawingLayer.addFeatures vector

  ###
  Get the locations of this search result
  ###
  convertToWKT: (location) ->
    ###
    TODO: REMOVE DIRTY HACK
    ###
    if _.isArray(location)
      location = location[0]

    [minx,miny,maxx,maxy] = location.split ' '

    return "POLYGON((#{minx} #{miny}, #{minx} #{maxy}, #{maxx} #{maxy}, #{maxx} #{miny}, #{minx} #{miny}))"