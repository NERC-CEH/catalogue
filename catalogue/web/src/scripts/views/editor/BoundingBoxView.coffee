define [
  'underscore'
  'cs!views/editor/ObjectInputView'
  'tpl!templates/editor/BoundingBox.tpl'
  'cs!views/OpenLayersView'
  'openlayers'
],
(_, ObjectInputView, template, OpenLayersView, OpenLayers) -> ObjectInputView.extend

  template: template

  initialize: ->
    do @render

    mapView = new OpenLayersView
      el: @$('.map')

    map = mapView.map

    boundingBoxLayer = new OpenLayers.Layer.Vector "Bounding Box"
    map.addLayer boundingBoxLayer

    transform = new OpenLayers.Control.TransformFeature boundingBoxLayer,
      rotate: false
      irregular: true
    map.addControl transform

    drawing = new OpenLayers.Control.DrawFeature boundingBoxLayer,
      OpenLayers.Handler.RegularPolygon,
      handlerOptions:
        sides: 4,
        irregular: true

    _.bindAll @, 'handleDrawnFeature'
    drawing.events.register 'featureadded', boundingBoxLayer, @handleDrawnFeature
    map.addControl drawing

    west = @model.get 'westBoundLongitude'
    south = @model.get 'southBoundLatitude'
    east = @model.get 'eastBoundLongitude'
    north = @model.get 'northBoundLatitude'

    if west && south && east && north
      bounds = new OpenLayers.Bounds(west, south, east, north)
        .toGeometry()
      bounds.transform('EPSG:4326', 'EPSG:3857')
      boundingBox = new OpenLayers.Feature.Vector(bounds)

    if boundingBox
      boundingBoxLayer.addFeatures [boundingBox]
      map.zoomToExtent(boundingBoxLayer.getDataExtent())
    else
      do drawing.activate
      do transform.activate

    @listenTo @model, 'change', @render

  handleDrawnFeature: (bbox) ->
    bounds = bbox
      ?.feature
      ?.geometry
      .clone()
      .transform('EPSG:3857', 'EPSG:4326')
      .getBounds()

    @model.set
      westBoundLongitude: bounds.left.toFixed 3
      southBoundLatitude: bounds.bottom.toFixed 3
      eastBoundLongitude: bounds.right.toFixed 3
      northBoundLatitude: bounds.top.toFixed 3

    console.log 'updating'
    console.log bounds
