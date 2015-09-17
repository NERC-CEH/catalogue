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

  handleDrawnFeature: (bbox) ->
    bounds = bbox
      ?.feature
      ?.geometry
      .clone()
      .transform('EPSG:3857', 'EPSG:4326')
    console.log 'updating'
    console.log bounds
