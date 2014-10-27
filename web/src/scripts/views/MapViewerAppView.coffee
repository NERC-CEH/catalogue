define [
  'jquery'
  'backbone'
  'cs!views/MapViewerMapView'
  'cs!views/LayersControlPanelView'
], ($, Backbone, MapViewerMapView, LayersControlPanelView) -> Backbone.View.extend
  el: '#mapviewer'

  initialize: ->
    do @render

  render: ->
    @mapViewerMapView = new MapViewerMapView
      model: @model
      el: @$('.openlayers')

    @layersView = new LayersControlPanelView
      collection: @model.getLayers()
      el: @$('.layers .list-group')