define [
  'jquery'
  'underscore'
  'cs!views/ExtentHighlightingMapView'
  'openlayers'
], ($, _, ExtentHighlightingMapView, OpenLayers) -> ExtentHighlightingMapView.extend
  restriction:
    strokeColor: '#1c1b1e'
    fillOpacity: 0

  initialize: ->
    ExtentHighlightingMapView.prototype.initialize.call this, arguments #Initialize super

    # Create the layers to draw the search results on
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

    @map.addLayer  @drawingLayer
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
      @drawingLayer.addFeatures @readWKT @model.get 'bbox'


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
    @model.set 'bbox', feature.geometry.toString()

  ###
  Set the highlighted records based upon the current search result's locations
  ###
  updateHighlightedRecord: ->
    @setHighlighted @model.getResults()?.getSelectedResult()?.locations if @model.getResults()?.getSelectedResult()?.locations?
