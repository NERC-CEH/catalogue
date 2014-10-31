define [
  'jquery'
  'underscore'
  'backbone'
  'cs!views/OpenLayersView'
  'openlayers'
], ($, _, Backbone, OpenLayersView, OpenLayers) -> OpenLayersView.extend
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
    OpenLayersView.prototype.initialize.call this, arguments #Initialize super
    # Create the layers to draw the search results on
    @highlightedLayer = new OpenLayers.Layer.Vector "Selected Layer"
    @markerLayer      = new OpenLayers.Layer.Markers "Marker Layer"
    
    @map.addLayers [@highlightedLayer, @markerLayer]

  ###
  Position the openlayers map such that the features of the highlighted layer 
  can be seen
  ###
  zoomToHighlighted: -> @map.zoomToExtent @highlightedLayer.getDataExtent()

  ###
  Given an array of locations in the form:

    ["-9.227701 49.83726 2.687637 60.850441", "-1.50 51.51 -1.47 51.54"]

  Draw these on the map. If this method is called with null or an empty array
  then remove all the highlighted features from the map
  ###
  setHighlighted: (locations = [])->
    # Remove all the old markers
    do @highlightedLayer.removeAllFeatures
    do @markerLayer.clearMarkers

    # Loop round all the locations and set as a marker and polygon
    _.each locations, (location) =>
      vector = @readBoundingBox location
      vector.style = @highlighted

      centroid = vector.geometry.components[0].getCentroid()
      lonLat = new OpenLayers.LonLat centroid.x, centroid.y
      @markerLayer.addMarker new OpenLayers.Marker lonLat, @marker
      @highlightedLayer.addFeatures vector

  ###
  Convert the given location string into a Openlayers feature which is in the
  same projection system as that of the map
  ###
  readBoundingBox: (location) ->
    [minx,miny,maxx,maxy] = location.split ' '
    vector = @wktFactory.read """POLYGON((#{minx} #{miny}, \
                                          #{minx} #{maxy}, \
                                          #{maxx} #{maxy}, \
                                          #{maxx} #{miny}, \
                                          #{minx} #{miny}))"""
    vector.geometry.transform @epsg4326, @map.getProjectionObject()
    return vector