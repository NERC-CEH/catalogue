define [
  'underscore'
  'cs!views/editor/InputView'
  'tpl!templates/editor/Geometry.tpl'
  'cs!views/OpenLayersView'
  'openlayers'
],
(_, InputView, template, OpenLayersView, OpenLayers) ->

  geometryLayer = new OpenLayers.Layer.Vector("Geometry")

  controls = {
    create: new OpenLayers.Control.DrawFeature(
      geometryLayer,
      OpenLayers.Handler.Polygon
    )
    modify: new OpenLayers.Control.ModifyFeature(geometryLayer)
    delete: new OpenLayers.Control.SelectFeature(geometryLayer,
      onSelect: (feature) -> geometryLayer.removeFeatures(feature)
    )
  }

  view = InputView.extend

    template: template

    events:
      'change input': 'toggleControls'

    initialize: (options) ->
      InputView.prototype.initialize.call @, options
      @stopListening()
      _.bindAll(@,
        'setModel'
      )

      geometryLayer.events.on({
        'beforefeatureadded': -> false if geometryLayer.features.length > 0
        'afterfeaturemodified': @setModel,
        'featureadded': @setModel,
        'featureremoved': @setModel
      })

      @wktFactory = new OpenLayers.Format.WKT
        internalProjection: @map.baseLayer.projection,
        externalProjection: new OpenLayers.Projection('EPSG:4326')

      if @model.has 'geometry'
        geometryLayer.addFeatures @wktFactory.read(@model.get('geometry'))

    render: ->
      InputView.prototype.render.apply @

      mapView = new OpenLayersView({el: @$('.map')})
      @map = mapView.map
      @map.addLayer(geometryLayer)
      @map.addControls(_.values(controls))

    toggleControls: (event) ->
      target = event.target
      for key in _.keys(controls)
        control = controls[key]
        if target.value is key and target.checked
          control.activate()
        else
          control.deactivate()

    setModel: ->
      features = geometryLayer.features
      if features.length > 0
        if features[0].geometry.getArea() == 0 # prevent points being added
          geometryLayer.removeFeatures(features[0])
        else
          @model.set('geometry', @wktFactory.write(features[0]))
      else
        @model.unset 'geometry'

  return view
