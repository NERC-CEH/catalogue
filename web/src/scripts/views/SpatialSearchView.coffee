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

  restriction:
    strokeColor: '#1c1b1e'
    fillOpacity: 0

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
    @markerLayer = new OpenLayers.Layer.Markers "Marker Layer"
    @drawingLayer = new OpenLayers.Layer.Vector "Drawing Layer", style: @restriction

    @drawingControl = new OpenLayers.Control.DrawFeature @drawingLayer, 
      OpenLayers.Handler.RegularPolygon, 
      handlerOptions:
        sides: 4,
        irregular: true

    # Bind the handle drawn feature method to this class before registering it
    # as an openlayers event listener
    _.bindAll this, 'handleDrawnFeature'
    @drawingLayer.events.register "featureadded", @drawingLayer, @handleDrawnFeature

    @map.addLayers [@highlightedLayer, @markerLayer, @drawingLayer]
    @map.addControl @drawingControl

    do @updateHighlightedRecord
    @listenTo @model, 'cleared:results results-change:selected', @updateHighlightedRecord
    @listenTo @model, 'change:drawing', @updateDrawingMode
    @listenTo @model, 'change:bbox', @updateDrawingLayer

  ###
  Update the drawing layer with the restricted bounding box used for searching.
  ###
  updateDrawingLayer: ->
    do @drawingLayer.removeAllFeatures # Remove all the drawn features
    if @model.has 'bbox'               # Draw the bbox if specified
      bbox = @model.get('bbox').replace /,/g, ' '  # TODO: STANDARDISE BBOX OUTPUT
      @drawingLayer.addFeatures @readBoundingBox bbox

  ###
  Toggle the drawing control depending on weather or not the model is in 
  drawing mode
  ###
  updateDrawingMode:->
    mode = if @model.get('drawing') then 'activate' else 'deactivate'
    do @drawingControl[mode]

  ###
  Obtain the drawn bounding box from the drawing layer and register it as a new
  bounding box to search within on the model
  ###
  handleDrawnFeature: (evt) ->
    feature = evt.feature.clone() #clone the feature so we can perform a transformation
    feature.geometry.transform @map.getProjectionObject(), @epsg4326 #convert to 4326

    extent = feature.geometry.getBounds()
    viewportArr = [extent.left, extent.bottom, extent.right, extent.top]
    @model.set 'bbox', _.map(viewportArr, (num) -> num.toFixed(3)).join ','

  ###
  Clear any markers or features which represent the old selected record. Then
  check to see if a record has been selected, if so populate
  ###
  updateHighlightedRecord: ->
    # Remove all the old markers
    do @highlightedLayer.removeAllFeatures
    do @markerLayer.clearMarkers

    # Get the selected result
    selected = @model.getResults()?.getSelectedResult()

    # If that result is not undefined then render as a marker and polygon
    if selected
      _.each selected.locations, (location) =>
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