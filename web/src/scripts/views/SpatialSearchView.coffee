define [
  'jquery'
  'underscore'
  'backbone'
  'openlayers'
], ($, _, Backbone, OpenLayers) -> Backbone.View.extend
  events:
    "moveend" : -> console.log 'move'
  initialize: ->
    console.log 'Creating a map page ' + @model.getSearchResults().cid
    @map = new OpenLayers.Map
      div: @el
      maxExtent: new OpenLayers.Bounds(-20037508.34,-20037508.34,20037508.34,20037508.34)
      displayProjection: new OpenLayers.Projection("EPSG:3857")
      theme: null

    @map.addLayer new OpenLayers.Layer.OSM("OSM",[
      "//a.tile.openstreetmap.org/${z}/${x}/${y}.png",
      "//b.tile.openstreetmap.org/${z}/${x}/${y}.png",
      "//c.tile.openstreetmap.org/${z}/${x}/${y}.png"
    ])

    @map.zoomToExtent new OpenLayers.Bounds(-1885854.36, 6623727.12, 1245006.31, 7966572.83)

    do @createSelectedResultLayers
    @listenTo @model, 'change:results', @createSelectedResultLayers

    @map.events.register 'moveend', @map, => @model.setBBox @getOpenlayersViewport()

  ###

  ###
  createSelectedResultLayers: ->
    if @resultLayers # Remove the old search layers from the map
      _.each @resultLayers, (layer) => @map.removeLayer layer
      @resultLayers = null

    if @model.hasResults()
      console.log 'creating layers and adding to map ' + @model.getSearchResults().cid
      @resultLayers = @createSearchResultsLayers @model.getSearchResults()
      @map.addLayers @resultLayers

  ###
  Get the current bbox object for the viewport of the openlayers map
  ###
  getOpenlayersViewport: ->
    extent = @map.getExtent()
                 .transform @map.getProjectionObject(), new OpenLayers.Projection("EPSG:4326")
    bbox= [extent.left, extent.bottom, extent.right, extent.top].join ','
    return bbox

  ###
  Create a drawing layer which represents the currently displayed search results
  ###
  createSearchResultsLayers: (page) ->
    wktFactory = new OpenLayers.Format.WKT #Create the wktFactory to convert openlayers features to wkt
    drawingLayer = new OpenLayers.Layer.Vector "Vector Layer"
    markerLayer = new OpenLayers.Layer.Markers "Marker Layer"

    epsg4326 = new OpenLayers.Projection("EPSG:4326")

    updateDrawingLayer = =>
      do drawingLayer.removeAllFeatures
      do markerLayer.clearMarkers

      result = page.getSelectedResult()
      if result
        _.each page.attributes.results, (result) =>
          vector = wktFactory.read @convertToWKT result.locations
          vector.geometry.transform epsg4326, drawingLayer.map.getProjectionObject()
          
          centroid = vector.geometry.components[0].getCentroid()

          markerLayer.addMarker new OpenLayers.Marker( new OpenLayers.LonLat(
            centroid.x,
            centroid.y
          ), new OpenLayers.Icon('/static/img/marker-grey.png', {h:34, w:21}, {x:-10,y:-34}))
        
        vector = wktFactory.read @convertToWKT result.locations
        vector.geometry.transform epsg4326, drawingLayer.map.getProjectionObject()
        vector.style =
          strokeColor: '#8fca89' 
          fillColor: '#8fca89'
          fillOpacity: 0.3

        centroid = vector.geometry.components[0].getCentroid()
        drawingLayer.addFeatures vector
        markerLayer.addMarker new OpenLayers.Marker( new OpenLayers.LonLat(
          centroid.x,
          centroid.y
        ), new OpenLayers.Icon('/static/img/marker.png', {h:34, w:21}, {x:-10,y:-34}))



    page.on 'change:selected', updateDrawingLayer
    return [drawingLayer, markerLayer]

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