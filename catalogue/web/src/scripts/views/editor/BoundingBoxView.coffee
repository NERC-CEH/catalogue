define [
  'underscore'
  'cs!views/editor/ObjectInputView'
  'tpl!templates/editor/BoundingBox.tpl'
  'cs!views/OpenLayersView'
  'openlayers'
],
(
  _,
  ObjectInputView,
  template,
  OpenLayersView,
  OpenLayers) -> ObjectInputView.extend

  template: template

  events: ->
    _.extend {}, ObjectInputView.prototype.events,
      'click button': 'viewMap'

  initialize: (options) ->
    _.bindAll(@,
      'handleDrawnFeature',
      'handleTransformedFeature'
    )
    do @render
    @listenTo @model, 'change:westBoundLongitude', (model, value) ->
      @$('#boundingBoxWestBoundLongitude').val value
    @listenTo @model, 'change:southBoundLatitude', (model, value) ->
      @$('#boundingBoxSouthBoundLatitude').val value
    @listenTo @model, 'change:eastBoundLongitude', (model, value) ->
      @$('#boundingBoxEastBoundLongitude').val value
    @listenTo @model, 'change:northBoundLatitude', (model, value) ->
      @$('#boundingBoxNorthBoundLatitude').val value
    @listenTo @model.collection, 'visible', @viewMap
    @listenTo @model.collection, 'hidden', @closeMap

  viewMap: ->
    $map = @$('.map')
    $map.html ''
    mapView = new OpenLayersView
      el: $map
    @map = mapView.map

    @boundingBoxLayer = new OpenLayers.Layer.Vector "Bounding Box"
    @map.addLayer @boundingBoxLayer

    @transform = new OpenLayers.Control.TransformFeature @boundingBoxLayer,
      rotate: false
      irregular: true
    @transform.events.register(
      'transformcomplete', @boundingBoxLayer, @handleTransformedFeature
      )
    @map.addControl @transform

    @drawing = new OpenLayers.Control.DrawFeature @boundingBoxLayer,
      OpenLayers.Handler.RegularPolygon,
      title: 'Draw Bounding Box'
      handlerOptions:
        sides: 4,
        irregular: true
    @drawing.events.register(
      'featureadded', @boundingBoxLayer, @handleDrawnFeature
      )
    @map.addControl @drawing

    if @model.hasBoundingBox()
      do @createFeature
      @map.zoomToExtent(@boundingBoxLayer.getDataExtent())
      do @transform.activate
    else
      do mapView.refresh
      do @drawing.activate

  closeMap: ->
    do @map.destroy

  createFeature: ->
    boundingBox = @model.getBoundingBox()
    @boundingBoxLayer.addFeatures [boundingBox]
    @transform.setFeature boundingBox

  handleDrawnFeature: (obj) ->
    do @drawing.deactivate
    do @transform.activate
    @transform.setFeature obj.feature
    @handleTransformedFeature(obj)

  handleTransformedFeature: (obj) ->
    bounds = obj.feature.geometry
      .clone()
      .transform('EPSG:3857', 'EPSG:4326')
      .getBounds()

    @model.set
      westBoundLongitude: bounds.left.toFixed 3
      southBoundLatitude: bounds.bottom.toFixed 3
      eastBoundLongitude: bounds.right.toFixed 3
      northBoundLatitude: bounds.top.toFixed 3
