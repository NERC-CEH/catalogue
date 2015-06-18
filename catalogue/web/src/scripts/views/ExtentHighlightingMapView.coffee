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

  minGeoLength: 20 # The minimum size in pixels a feature will be displayed as
  wktFactory:   new OpenLayers.Format.WKT
  epsg4326:     new OpenLayers.Projection "EPSG:4326"

  initialize: ->
    OpenLayersView.prototype.initialize.call this, arguments #Initialize super

    # Create a vector layer which will render the selected features extents. If
    # they are too small to see on screen, points will be symbolized in there 
    # place
    @highlighted.pointRadius = @minGeoLength / 2
    @highlightedLayer = new OpenLayers.Layer.Vector "Selected Layer", 
      styleMap: new OpenLayers.StyleMap new OpenLayers.Style @highlighted,
        rules: [ new OpenLayers.Rule filter: @createFilterFunction() ]

    @map.addLayer @highlightedLayer


  ###
  Define an openlayers filter function which will dictate if a geometry should
  be rendered or not. The function is designed to work over features generated
  by the "setHighlighted" method
  ###
  createFilterFunction:-> new OpenLayers.Filter.Function
    evaluate: (geo) => geo.isPoint is not @isLengthVisible geo.areaRoot

  ###
  Decide if it is possible (or at least sensible) to render a feature with the 
  given length on the map. If the length is too small, favour a fixed size
  point instead.
  ###
  isLengthVisible: (length) -> (length / @map.getResolution()) > @minGeoLength

  ###
  Position the openlayers map such that the features of the highlighted layer 
  can be seen
  ###
  zoomToHighlighted: -> @map.zoomToExtent @highlightedLayer.getDataExtent()

  ###
  Given an array of locations in the form:

    ["-9.227701 49.83726 2.687637 60.850441", "-1.50 51.51 -1.47 51.54"]

  Transform these to wkt and then call setHighlighted
  ###
  setHighlightedBoxes: (boxes = []) -> @setHighlighted _.map boxes, @solr2WKT

  ###
  Given an array of locations in the form:

    ["POLYGON((1 2 ...))", "POLYGON((1 2 ...))"]

  Draw these on the map. If this method is called with null or an empty array
  then remove all the highlighted features from the map
  ###
  setHighlighted:(locations = []) ->
    # Remove all the old markers
    do @highlightedLayer.removeAllFeatures

    # Loop round all the locations and set as a marker and polygon
    _.each locations, (location) =>
      vector = @readWKT location
      # Calculate the average length of the height and width of the bounds
      bounds = vector.geometry.getBounds().toGeometry()
      vector.attributes = 
        areaRoot: Math.sqrt bounds.getArea()
        isPoint:  false

      point = new OpenLayers.Feature.Vector bounds.getCentroid()
      point.attributes = _.defaults isPoint: true, vector.attributes
      @highlightedLayer.addFeatures [vector, point]

  ###
  Transform the well known text representation into an openlayers geometry in
  the projection system of the map
  ###
  readWKT: (wkt) ->
    vector = @wktFactory.read wkt
    vector.geometry.transform @epsg4326, @map.getProjectionObject()
    return vector

  ###
  Convert the given location string into a well known text representation.
  Solr geometries can either be Points or Polygons
  ###
  solr2WKT: (location) ->
    c = location.split ' '
    if c.length is 2 then return "POINT(#{c[0]} #{c[1]})" 
    else return """POLYGON((#{c[0]} #{c[1]}, \
                            #{c[0]} #{c[3]}, \
                            #{c[2]} #{c[3]}, \
                            #{c[2]} #{c[1]}, \
                            #{c[0]} #{c[1]}))"""
