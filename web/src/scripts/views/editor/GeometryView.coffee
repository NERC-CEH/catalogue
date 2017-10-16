define [
  'underscore'
  'cs!views/editor/ObjectInputView'
  'tpl!templates/editor/Geometry.tpl'
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
      'change input[value=modify]': 'modifyFeature',
      'change input[value=create]': 'createFeature',

  initialize: () ->
    _.bindAll(@,
      'updateOutput',
      'createFeature',
      'modifyFeature'
    )
    do @render

    $map = @$('.map')
    mapView = new OpenLayersView({el: $map})
    map = mapView.map
    
    @geometryLayer = new OpenLayers.Layer.Vector("Geometry")
    map.addLayer(@geometryLayer)

    @drawing = new OpenLayers.Control.DrawFeature(
      @geometryLayer,
      OpenLayers.Handler.Polygon,
      handlerOptions: holeModifier: "altKey"
    )

    @modify = new OpenLayers.Control.ModifyFeature(
      @geometryLayer
    )
    
    map.addControls [@drawing, @modify]

    @drawing.events.register(
      'featureadded', @geometryLayer, @updateOutput
    )

    @wktFactory = new OpenLayers.Format.WKT
      internalProjection: map.baseLayer.projection,
      externalProjection: new OpenLayers.Projection('EPSG:4326')

  updateOutput: (obj) ->
    do @drawing.deactivate
    @model.set('geometry', @wktFactory.write(obj.feature))

  createFeature: ->
    console.log('create')
    do @drawing.activate
    do @modify.deactivate

  modifyFeature: ->
    console.log('modify')
    do @drawing.deactivate
    do @modify.activate
